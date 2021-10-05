package ddt.com.ubertob.unlearnoop.zettai.stories

import com.ubertob.pesticide.core.DDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.ToDoListCreator
import ddt.com.ubertob.unlearnoop.zettai.tooling.ZettaiDDT
import ddt.com.ubertob.unlearnoop.zettai.tooling.allInterpreters
import org.junit.jupiter.api.DynamicContainer
import java.time.LocalDate
import java.util.stream.Stream

class SeeATodoListDDT : ZettaiDDT(allInterpreters()) {

    val frank by NamedActor(::ToDoListCreator)


    @DDT
    fun `List owners can see their lists`(): Stream<DynamicContainer> = ddtScenario {
        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")

        setting {
            frank.`starts with a list`(listName, foodToBuy)
        } atRise play(
            frank.`can see list # with # items`(listName, foodToBuy)
        ).wip(LocalDate.of(2100, 12, 31), "Http not implemented yet")

    }

}
