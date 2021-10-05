package com.ubertob.unlearnoop.zettai.commands

import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.domain.tooling.expectFailure
import com.ubertob.unlearnoop.zettai.domain.tooling.expectSuccess
import com.ubertob.unlearnoop.zettai.events.ListCreated
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStore
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStreamerInMemory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.single

internal class ToDoListCommandsTest {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val handler = ToDoListCommandHandler(eventStore)

    val name = randomListName()
    val user = randomUser()

    @Test
    fun `Add list fails if the user has already a list with same name`() {

        val cmd = CreateToDoList(user, name)
        val res = handler(cmd).expectSuccess()

        expectThat(res).single().isA<ListCreated>()
        eventStore(res)

        val duplicatedRes = handler(cmd).expectFailure()
        expectThat(duplicatedRes).isA<InconsistentStateError>()
    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(user, name, randomItem())
        val res = handler(cmd).expectFailure()
        expectThat(res).isA<ToDoListCommandError>()
    }

    //todo add tests for
//    is RenameToDoList -> command.execute()
//    is FreezeToDoList -> command.execute()
//    is RestoreToDoList -> command.execute()
//    is EditToDoItem -> command.execute()
//    is DeleteToDoItem -> command.execute()

}