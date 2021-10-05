package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest
import com.ubertob.pesticide.core.DomainInterpreter
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User


interface ZettaiInterpreter : DomainInterpreter<DdtProtocol> {
    fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>)
    fun ToDoListCreator.`starts with some lists`(lists: Map<String, List<String>>)

    fun getToDoList(user: User, listName: ListName): ToDoList?
    fun updateListItem(user: User, listName: ListName, item: ToDoItem)
    fun allUserLists(user: User): List<ListName>
    fun createList(user: User, listName: ListName)
}

typealias ZettaiDDT = DomainDrivenTest<ZettaiInterpreter>

private val interpreters = setOf(
    DomainOnlyInterpreter(),
    HttpInterpreter()
)

fun allInterpreters() = interpreters



