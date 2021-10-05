package ddt.com.ubertob.unlearnoop.zettai.stories

import com.ubertob.pesticide.core.DDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.ToDoListCreator
import ddt.com.ubertob.unlearnoop.zettai.tooling.ZettaiDDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.allInterpreters

class SeeAllTheToDoListsDDT : ZettaiDDT(allInterpreters()) {

    val carol by NamedActor(::ToDoListCreator)
    val dylan by NamedActor(::ToDoListCreator)
    val emma by NamedActor(::ToDoListCreator)


    @DDT
    fun `new users have no lists`() = ddtScenario {

        withoutSetting atRise play(
            emma.`cannot see any list`()
        )
    }

    @DDT
    fun `only owners can see all their lists`() = ddtScenario {

        val expectedLists = generateSomeToDoLists()

        setting {
            carol.`starts with some lists`(expectedLists)
        } atRise play(
            carol.`can see all the lists #`(expectedLists.keys),
            emma.`cannot see any list`()
        )
    }

    @DDT
    fun `users can create new lists`() = ddtScenario {

        withoutSetting atRise play(
            dylan.`cannot see any list`(),
            dylan.`can create a new list called #`("gardening"),
            dylan.`can create a new list called #`("music"),
            dylan.`can see all the lists #`(setOf("gardening", "music"))
        )
    }

    private fun generateSomeToDoLists(): Map<String, List<String>> {
        return mapOf(
            "work" to listOf("meeting", "spreadsheet"),
            "home" to listOf("buy food"),
            "friends" to listOf("buy present", "book restaurant")
        )
    }
}
