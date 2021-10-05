package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest
import com.ubertob.pesticide.core.DomainInterpreter
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User


interface ZettaiInterpreter : DomainInterpreter<DdtProtocol> {
    fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>): ZettaiInterpreter

    fun getToDoList(user: User, listName: ListName): ToDoList?
    fun addListItem(user: User, listName: ListName, item: ToDoItem)
}

typealias ZettaiDDT = DomainDrivenTest<ZettaiInterpreter>

fun allInterpreters() = setOf(
    DomainOnlyInterpreter(),
    HttpInterpreter()
)



