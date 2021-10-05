package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest
import com.ubertob.pesticide.core.DomainInterpreter
import com.ubertob.unlearnoop.zettai.domain.*


interface ZettaiInterpreter : DomainInterpreter<DdtProtocol> {
    fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>)
    fun ToDoListCreator.`starts with some lists`(lists: Map<String, List<String>>)

    fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList>
    fun updateListItem(user: User, listName: ListName, item: ToDoItem)
    fun allUserLists(user: User): ZettaiOutcome<List<ListName>>
    fun createList(user: User, listName: ListName)
    fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>>
    fun renameList(user: User, oldList: ListName, newList: ListName)
}

typealias ZettaiDDT = DomainDrivenTest<ZettaiInterpreter>

private val interpreters = setOf(
    DomainOnlyInterpreter(),
    HttpInterpreter()
)

fun allInterpreters() = interpreters



