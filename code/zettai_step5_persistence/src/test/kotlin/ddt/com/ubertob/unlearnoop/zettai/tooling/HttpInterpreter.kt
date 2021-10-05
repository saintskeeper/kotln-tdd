package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.*
import com.ubertob.unlearnoop.zettai.db.jdbc.resetDatabase
import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.fp.asSuccess
import com.ubertob.unlearnoop.zettai.ui.HtmlPage
import com.ubertob.unlearnoop.zettai.ui.toIsoLocalDate
import com.ubertob.unlearnoop.zettai.ui.toStatus
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.Form
import org.http4k.core.body.toBody
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Duration

data class HttpInterpreter(val env: String = "local") : ZettaiInterpreter {

    val zettaiPort = 8000 //different from the one in main
    val server = prepareZettaiOnTestDatabase().asServer(Jetty(zettaiPort))

    val client = JettyClient()

    override val protocol: DdtProtocol = Http(env)
    override fun prepare(): DomainSetUp {

        if (verifyStarted(Duration.ZERO) == Ready)
            return Ready

        resetDatabase(pgDataSourceForTest())

        server.start()
        registerShutdownHook {
            server.stop()
        }

        return verifyStarted(Duration.ofSeconds(2))
    }


    private fun registerShutdownHook(hookToExecute: () -> Unit) {
        Runtime.getRuntime().addShutdownHook(Thread {
            val out = System.out
            try {
                hookToExecute()
            } finally {
                System.setOut(out)
            }
        })
    }

    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> {

        val response = callZettai(Method.GET, todoListUrl(user, listName))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return ToDoList(listName, items).asSuccess()
    }


    override fun updateListItem(user: User, listName: ListName, item: ToDoItem) {
        val response = submitToZettai(
            todoListUrl(user, listName),
            listOf("itemname" to item.description, "itemdue" to item.dueDate?.toString())
        )

        expectThat(response.status).isEqualTo(Status.SEE_OTHER)

    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> {
        val response = callZettai(Method.GET, allUserListsUrl(user))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val names = extractListNamesFromPage(html)

        return names.map { name -> ListName.fromTrusted(name) }.asSuccess()
    }

    override fun createList(user: User, listName: ListName) {
        val response = submitToZettai(allUserListsUrl(user), newListForm(listName))

        expectThat(response.status).isEqualTo(Status.SEE_OTHER)  //redirect same page
    }

    override fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>> {
        val response = callZettai(Method.GET, whatsNextUrl(user))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return items.asSuccess()
    }

    private fun newListForm(listName: ListName): Form = listOf("listname" to listName.name)

    override fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>) {

        createListWithItems(listName, items)
    }

    private fun ToDoListCreator.createListWithItems(
        listName: String,
        items: List<String>
    ) {
        createList(user, ListName.fromTrusted(listName))

        val list = ListName.fromTrusted(listName)

        items.forEach {
            updateListItem(user, list, ToDoItem(it))
        }
    }

    override fun ToDoListCreator.`starts with some lists`(lists: Map<String, List<String>>) {

        lists.forEach { (listName, items) ->
            createListWithItems(listName, items)
        }
    }


    private fun todoListUrl(user: User, listName: ListName) =
        "todo/${user.name}/${listName.name}"

    private fun allUserListsUrl(user: User) =
        "todo/${user.name}"

    private fun whatsNextUrl(user: User) =
        "whatsnext/${user.name}"

    private fun extractItemsFromPage(html: HtmlPage): List<ToDoItem> {
        return html.parse()
            .select("tr")
            .filter { it.select("td").size == 3 }
            .map {
                Triple(
                    it.select("td")[0].text().orEmpty(),
                    it.select("td")[1].text().toIsoLocalDate(),
                    it.select("td")[2].text().orEmpty().toStatus()
                )
            }
            .map { (name, date, status) ->
                ToDoItem(name, date, status)
            }
    }

    private fun extractListNamesFromPage(html: HtmlPage): List<String> {
        return html.parse()
            .select("tr")
            .mapNotNull {
                it.select("td").firstOrNull()?.text()
            }
    }


    private fun verifyStarted(timeout: Duration): DomainSetUp {
        val begin = System.currentTimeMillis()
        while (true) {
            val r = callZettai(Method.GET, "ping").status
            if (r == Status.OK)
                return Ready
            if (elapsed(begin) >= timeout)
                return NotReady("timeout $timeout exceeded")
            Thread.sleep(10)
        }
    }

    private fun elapsed(since: Long): Duration =
        Duration.ofMillis(System.currentTimeMillis() - since)


    private fun submitToZettai(path: String, webForm: Form): Response =
        client(log(Request(Method.POST, "http://localhost:$zettaiPort/$path").body(webForm.toBody())))

    private fun callZettai(method: Method, path: String): Response =
        client(log(Request(method, "http://localhost:$zettaiPort/$path")))

    fun <T> log(something: T): T {
        println("--- $something")
        return something
    }

    private fun HtmlPage.parse(): Document = Jsoup.parse(raw)

}

