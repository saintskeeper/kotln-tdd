package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtActor
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.User
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotNull

data class ToDoListCreator(override val name: String) : DdtActor<ZettaiInterpreter>() {

    val user = User(name)

    fun `can add # to the list #`(itemName: String, listName: String) = step(itemName, listName) {
        val item = ToDoItem(itemName)
        addListItem(user, ListName(listName), item)
    }

    fun `can see list # with # items`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems.size) {
            val list = getToDoList(user, ListName(listName))
            expectThat(list)
                .isNotNull()
                .get { items.map { it.description } }
                .containsExactlyInAnyOrder(expectedItems)
        }
}