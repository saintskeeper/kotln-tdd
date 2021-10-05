package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.ubertob.unlearnoop.zettai.commands.AddToDoItem
import com.ubertob.unlearnoop.zettai.commands.CreateToDoList
import com.ubertob.unlearnoop.zettai.domain.*
import strikt.api.expectThat
import strikt.assertions.hasSize

class DomainOnlyInterpreter : ZettaiInterpreter {
    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val store: ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(store)

    private val hub = prepareToDoListHubForTests(fetcher)


    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        hub.getList(user, listName)


    override fun updateListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.handle(AddToDoItem(user, listName, item))
    }

    override fun allUserLists(user: User): List<ListName> =
        hub.getLists(user) ?: emptyList()

    override fun createList(user: User, listName: ListName) {
        hub.handle(CreateToDoList(user, listName))
    }

    override fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)
        val events = hub.handle(
            CreateToDoList(
                user,
                list
            )
        )
        events ?: throw RuntimeException("Failed to create list $listName")
        val created = items.mapNotNull {
            hub.handle(
                AddToDoItem(
                    user,
                    list,
                    ToDoItem(it)
                )
            )
        }.flatten()
        expectThat(created).hasSize(items.size)
    }

    override fun ToDoListCreator.`starts with some lists`(lists: Map<String, List<String>>) {
        lists.forEach { (listName, items) ->
            `starts with a list`(listName, items)
        }
    }

}


