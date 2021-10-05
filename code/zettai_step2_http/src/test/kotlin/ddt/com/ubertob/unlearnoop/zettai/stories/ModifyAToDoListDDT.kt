package ddt.com.ubertob.unlearnoop.zettai.stories

import com.ubertob.pesticide.core.DDT
import com.ubertob.pesticide.core.DomainOnly
import ddt.com.ubertob.unlearnoop.zettai.tooling.ToDoListCreator
import ddt.com.ubertob.unlearnoop.zettai.tooling.ZettaiDDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.allInterpreters
import java.time.LocalDate

class ModifyAToDoListDDT : ZettaiDDT(allInterpreters()) {

    val ann by NamedActor(::ToDoListCreator)

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
        ).wip(LocalDate.of(2100, 12, 31), "Http not implemented yet", setOf(DomainOnly::class))

    }
}

