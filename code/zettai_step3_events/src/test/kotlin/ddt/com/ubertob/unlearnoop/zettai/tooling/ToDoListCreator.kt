package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtActor
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*

data class ToDoListCreator(override val name: String) : DdtActor<ZettaiInterpreter>() {

    val user = User(name)

    fun `find # into # list`(listName: String, itemName: String) =
        step(itemName, listName) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
            expectThat(list)
                .isNotNull()
                .itemNames
                .contains(itemName)
        }


    fun `can see list # with # items`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems.size) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
            expectThat(list)
                .isNotNull()
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `cannot see any list`() = step {
        val lists = allUserLists(user)
        expectThat(lists)
            .isEmpty()
    }

    fun `can see all the lists #`(expectedLists: Set<String>) = step(expectedLists) {
        val lists = allUserLists(user)
        expectThat(lists)
            .map(ListName::name)
            .containsExactly(expectedLists)
    }

    fun `can create a new list called #`(listName: String) = step(listName) {
        createList(user, ListName.fromUntrustedOrThrow(listName))
    }

    fun `can add # to the list #`(itemName: String, listName: String) = step(itemName, listName) {
        val item = ToDoItem(itemName)
        updateListItem(user, ListName.fromUntrustedOrThrow(listName), item)
    }

    private val Assertion.Builder<ToDoList>.itemNames: Assertion.Builder<List<String>>
        get() = get { items.map { it.description } }


}

/*
todo
 */