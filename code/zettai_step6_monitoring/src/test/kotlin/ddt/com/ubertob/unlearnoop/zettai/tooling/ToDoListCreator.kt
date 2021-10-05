package ddt.com.ubertob.unlearnoop.zettai.tooling

import com.ubertob.pesticide.core.DdtActor
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.domain.tooling.expectSuccess
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*
import java.time.LocalDate

data class ToDoListCreator(override val name: String) : DdtActor<ZettaiInterpreter>() {

    val user = User(name)

    fun `find # into # list`(listName: String, itemName: String) =
        step(itemName, listName) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName)).expectSuccess()
            expectThat(list)
                .itemNames
                .contains(itemName)
        }


    fun `can see list # with # items`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems.size) {

            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName)).expectSuccess()
            expectThat(list)
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `cannot see any list`() = step {
        val lists = allUserLists(user).expectSuccess()
        expectThat(lists)
            .isEmpty()
    }

    fun `can see all the lists #`(expectedLists: Set<String>) = step(expectedLists) {
        val lists = allUserLists(user).expectSuccess()
        expectThat(lists)
            .map(ListName::name)
            .containsExactlyInAnyOrder(expectedLists)
    }

    fun `can create a new list called #`(listName: String) = step(listName) {
        createList(user, ListName.fromUntrustedOrThrow(listName))
    }

    fun `can add # to the list #`(itemName: String, listName: String) = step(itemName, listName) {
        val item = ToDoItem(itemName)
        updateListItem(user, ListName.fromUntrustedOrThrow(listName), item)
    }

    fun `can see that # is the next task to do`(itemName: String) = step(itemName) {

        val items = whatsNext(user).expectSuccess()

        expectThat(items.firstOrNull()?.description.orEmpty()).isEqualTo(itemName)
    }

    fun `can add # to the list # with due date #`(itemName: String, listName: String, dueDate: LocalDate) =
        step(itemName, listName, dueDate) {
            val item = ToDoItem(itemName, dueDate)
            updateListItem(user, ListName.fromUntrustedOrThrow(listName), item)
        }

    fun `can rename the list # as #`(origListName: String, newListName: String) =
        step(origListName, newListName) {
            renameList(user, ListName.fromUntrustedOrThrow(origListName), ListName.fromUntrustedOrThrow(newListName))
        }

    private val Assertion.Builder<ToDoList>.itemNames: Assertion.Builder<List<String>>
        get() = get { items.map { it.description } }


}
