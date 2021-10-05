package ddt.com.ubertob.unlearnoop.zettai.stories

import com.ubertob.pesticide.core.DDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.ToDoListCreator
import ddt.com.ubertob.unlearnoop.zettai.tooling.ZettaiDDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.allInterpreters

class ModifyAToDoListDDT : ZettaiDDT(allInterpreters()) {

    val ann by NamedActor(::ToDoListCreator)
    val ben by NamedActor(::ToDoListCreator)

    @DDT
    fun `users can create a new list`() = ddtScenario {
        withoutSetting atRise play(
            ann.`can create a new list called #`("myfirstlist"),
            ann.`can see list # with # items`(
                "myfirstlist", emptyList()
            )
        )
    }

    @DDT
    fun `the list owner can add new items`() = ddtScenario {
        setting {
            ann.`starts with a list`("diy", emptyList())
        } atRise play(
            ann.`can add # to the list #`("paint the shelf", "diy"),
            ann.`can add # to the list #`("fix the gate", "diy"),
            ann.`can add # to the list #`("change the lock", "diy"),
            ann.`can see list # with # items`(
                "diy", listOf(
                    "fix the gate", "paint the shelf", "change the lock"
                )
            )
        )
    }

    @DDT
    fun `the list owner can rename a list`() = ddtScenario {
        setting {
            ben.`starts with a list`("shopping", emptyList())
        } atRise play(
            ben.`can add # to the list #`("carrots", "shopping"),
            ben.`can rename the list # as #`(
                origListName = "shopping",
                newListName = "grocery"
            ),
            ben.`can add # to the list #`("potatoes", "grocery"),
            ben.`can see list # with # items`(
                "grocery", listOf("carrots", "potatoes")
            )
        )
    }
}