package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommand
import com.ubertob.unlearnoop.zettai.commands.ToDoListCommandHandler
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.events.ToDoListState
import com.ubertob.unlearnoop.zettai.fp.*
import com.ubertob.unlearnoop.zettai.queries.ItemProjectionRow
import com.ubertob.unlearnoop.zettai.queries.ToDoListQueryRunner


sealed class ZettaiError : OutcomeError
data class InvalidRequestError(override val msg: String) : ZettaiError()
data class ToDoListCommandError(override val msg: String) : ZettaiError()
data class InconsistentStateError(val command: ToDoListCommand, val state: ToDoListState) : ZettaiError() {
    override val msg = "Command $command cannot be applied to state $state"
}

typealias ZettaiOutcome<T> = Outcome<ZettaiError, T>

interface ZettaiHub {
    fun handle(command: ToDoListCommand): ZettaiOutcome<List<ToDoListEvent>>
    fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList>
    fun getLists(user: User): ZettaiOutcome<List<ListName>>
    fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>>
}


class ToDoListHub(
    val queryRunner: ToDoListQueryRunner,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {

    override fun handle(command: ToDoListCommand): ZettaiOutcome<List<ToDoListEvent>> =
        commandHandler(command).transform(persistEvents)

    override fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        queryRunner {
            listProjection.findList(user, listName)
                .failIfNull(InvalidRequestError("List $listName of user $user not found!"))
        }.runIt()

    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        queryRunner {
            listProjection.findAll(user)
                .failIfNull(InvalidRequestError("User $user not found!"))
                .transform { it.toList() }
        }.runIt()

    override fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>> =
        queryRunner {
            listProjection.findAllActiveListId(user)
                .failIfEmpty(InvalidRequestError("User $user not found!"))
                .transform { userLists -> itemProjection.findWhatsNext(10, userLists) }
                .transform { it.map(ItemProjectionRow::item) }
        }.runIt()
}

