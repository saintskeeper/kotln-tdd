package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.ubertob.unlearnoop.zettai.domain.*

class DomainOnlyInterpreter : ZettaiInterpreter {
    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val store: ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(store)

    private val hub = ToDoListHub(fetcher)


    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        hub.getList(user, listName) ?: throw ListNotPresentException(user, listName)


    override fun updateListItem(user: User, listName: ListName, item: ToDoItem) {
        getToDoList(user, listName)?.run {
            val newList = copy(items = items.filterNot { it.description == item.description } + item)
            fetcher.addListToUser(user, newList)
        }
    }

    override fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>) {
        val newList = ToDoList.build(listName, items)
        fetcher.addListToUser(user, newList)
    }


    data class ListNotPresentException(val user: User, val listName: ListName) : Throwable()

}