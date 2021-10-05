package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User

data class HttpInterpreter(val env: String = "local") : ZettaiInterpreter {
    override val protocol: DdtProtocol = Http(env)
    override fun prepare(): DomainSetUp = Ready

    override fun getToDoList(user: User, listName: ListName): ToDoList =
        TODO("not implemented yet")


    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        TODO("not implemented yet")
    }

    override fun ToDoListCreator.`starts with a list`(listName: String, items: List<String>): ZettaiInterpreter =
        TODO("not implemented yet")

}

