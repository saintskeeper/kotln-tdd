package com.ubertob.unlearnoop.zettai.commands

import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.events.ListCreated
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStore
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStreamerInMemory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isNull

internal class ToDoListCommandsTest {

    val fakeListener = object : ToDoListUpdatableFetcher {
        override fun addListToUser(user: User, list: ToDoList) {} //nothing
        override fun get(user: User, listName: ListName): ToDoList? = TODO("not implemented")
        override fun getAll(user: User): List<ListName>? = TODO("not implemented")
    }

    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val handler = ToDoListCommandHandler(eventStore, fakeListener)

    fun handle(cmd: ToDoListCommand): List<ToDoListEvent>? =
        handler(cmd)?.let(eventStore)


    val name = randomListName()
    val user = randomUser()

    @Test
    fun `Add list fails if the user has already a list with same name`() {

        val cmd = CreateToDoList(user, name)
        val res = handle(cmd)?.single()

        expectThat(res).isA<ListCreated>()

        val duplicatedRes = handle(cmd)
        expectThat(duplicatedRes).isNull()

    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(user, name, randomItem())
        val res = handle(cmd)

        expectThat(res).isNull()

    }

}