package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommand
import com.ubertob.unlearnoop.zettai.commands.ToDoListCommandHandler
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.fp.EventPersister

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
    fun getLists(user: User): List<ListName>?
    fun handle(command: ToDoListCommand): List<ToDoListEvent>?
}

interface ToDoListFetcher {

    fun get(user: User, listName: ListName): ToDoList?

    fun getAll(user: User): List<ListName>?

}

class ToDoListHub(
    val fetcher: ToDoListFetcher,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {

    override fun handle(command: ToDoListCommand): List<ToDoListEvent>? =
        commandHandler(command)?.let(persistEvents)

    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher.get(user, listName)

    override fun getLists(user: User): List<ListName>? =
        fetcher.getAll(user)

}

