package ddt.com.ubertob.unlearnoop.zettai.stories

import com.ubertob.pesticide.core.DDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.ToDoListCreator
import ddt.com.ubertob.unlearnoop.zettai.tooling.ZettaiDDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.allInterpreters
import java.time.LocalDate

class SeeWhatsNextDDT : ZettaiDDT(allInterpreters()) {

    val alice by NamedActor(::ToDoListCreator)


    @DDT
    fun `What's next show the items in order of urgency`() = ddtScenario {
        val gardenList = "gardening"
        val gardenTasks = listOf("mulching", "trim hedge")
        val partyList = "party"
        val partyTasks = listOf("cake", "decoration")

        setting {
            alice.`starts with some lists`(mapOf(gardenList to gardenTasks, partyList to partyTasks))
        } atRise play(
            alice.`can see that # is the next task to do`(""),
            alice.`can add # to the list # with due date #`("prepare dress", partyList, LocalDate.now().plusDays(3)),
            alice.`can add # to the list # with due date #`("buy present", partyList, LocalDate.now().plusDays(2)),
            alice.`can add # to the list # with due date #`("go party", partyList, LocalDate.now().plusDays(4)),
            alice.`can see that # is the next task to do`("buy present"),
            alice.`can add # to the list # with due date #`("water plants", gardenList, LocalDate.now().plusDays(1)),
            alice.`can see that # is the next task to do`("water plants")
        )

    }

}
