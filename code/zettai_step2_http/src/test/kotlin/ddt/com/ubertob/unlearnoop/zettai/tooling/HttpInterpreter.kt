package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.*
import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.ui.HtmlPage
import com.ubertob.unlearnoop.zettai.ui.toIsoLocalDate
import com.ubertob.unlearnoop.zettai.ui.toStatus
import com.ubertob.unlearnoop.zettai.webserver.Zettai
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Duration

data class HttpInterpreter(val env: String = "local") : ZettaiInterpreter {

    private val fetcher = ToDoListFetcherFromMap(mutableMapOf())
    private val hub = ToDoListHub(fetcher)

    val zettaiPort = 8000 //different from the one in main
    val server = Zettai(hub).asServer(Jetty(zettaiPort))

    val client = JettyClient()

    override val protocol: DdtProtocol = Http(env)

    override fun prepare(): DomainSetUp {
        if (verifyStarted(Duration.ZERO) == Ready)
            return Ready
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


    override fun getToDoList(user: User, listName: ListName): ToDoList {

        val response = callZettai(Method.GET, todoListUrl(user, listName))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return ToDoList(listName, items)
    }


    override fun updateListItem(user: User, listName: ListName, item: ToDoItem) {
        TODO("not implemented yet")
    }

    override fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>) {
        fetcher.addListToUser(
            user,
            ToDoList(ListName.fromUntrustedOrThrow(listName), items.map { ToDoItem(it) })
        )
    }


    private fun todoListUrl(user: User, listName: ListName) =
        "todo/${user.name}/${listName.name}"

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


    private fun callZettai(method: Method, path: String): Response =
        client(log(Request(method, "http://localhost:$zettaiPort/$path")))

    fun <T> log(something: T): T {
        println("--- $something")
        return something
    }

    private fun HtmlPage.parse(): Document = Jsoup.parse(raw)

}

