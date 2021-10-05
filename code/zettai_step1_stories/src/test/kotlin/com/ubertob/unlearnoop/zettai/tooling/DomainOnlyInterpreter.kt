package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User

class DomainOnlyInterpreter : ZettaiInterpreter {
    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready


    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        TODO("not implemented yet")

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        TODO("not implemented yet")
    }

    override fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>): ZettaiInterpreter =
        TODO("not implemented yet")

}