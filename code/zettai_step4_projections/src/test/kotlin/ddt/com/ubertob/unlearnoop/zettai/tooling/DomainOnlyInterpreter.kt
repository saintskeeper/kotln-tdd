package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.ubertob.unlearnoop.zettai.commands.AddToDoItem
import com.ubertob.unlearnoop.zettai.commands.CreateToDoList
import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.domain.tooling.expectSuccess
import strikt.api.expectThat
import strikt.assertions.hasSize

class DomainOnlyInterpreter : ZettaiInterpreter {
    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready


    private val hub = prepareToDoListHubForTests()


    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        hub.getList(user, listName)


    override fun updateListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.handle(AddToDoItem(user, listName, item))
    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> =
        hub.getLists(user)

    override fun createList(user: User, listName: ListName) {
        hub.handle(CreateToDoList(user, listName))
    }

    override fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>> =
        hub.whatsNext(user)

    override fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)
        hub.handle(
            CreateToDoList(
                user,
                list
            )
        ).expectSuccess()

        val events = items.map {
            hub.handle(
                AddToDoItem(
                    user,
                    list,
                    ToDoItem(it)
                )
            ).expectSuccess()
        }.flatten()

        expectThat(events).hasSize(items.size)
    }

    override fun ToDoListCreator.`starts with some lists`(lists: Map<String, List<String>>) {
        lists.forEach { (listName, items) ->
            `starts with a list`(listName, items)
        }
    }

}


