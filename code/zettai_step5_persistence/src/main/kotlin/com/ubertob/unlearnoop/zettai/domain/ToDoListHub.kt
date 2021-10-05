package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommand
import com.ubertob.unlearnoop.zettai.db.jdbc.ContextError
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.events.ToDoListState
import com.ubertob.unlearnoop.zettai.fp.CommandHandler
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError
import com.ubertob.unlearnoop.zettai.fp.bind
import com.ubertob.unlearnoop.zettai.queries.ItemProjectionRow
import com.ubertob.unlearnoop.zettai.queries.ToDoListQueryRunner


sealed class ZettaiError : OutcomeError
data class InvalidRequestError(override val msg: String) : ZettaiError()
data class ToDoListCommandError(override val msg: String) : ZettaiError()
data class InconsistentStateError(val command: ToDoListCommand, val state: ToDoListState) : ZettaiError() {
    override val msg = "Command $command cannot be applied to state $state"
}

data class QueryError(override val msg: String, override val exception: Throwable? = null) : ZettaiError(),
    ContextError


data class ZettaiParsingError(override val msg: String) : ZettaiError()

typealias ZettaiOutcome<T> = Outcome<ZettaiError, T>

interface ZettaiHub {
    fun handle(command: ToDoListCommand): ZettaiOutcome<List<ToDoListEvent>>
    fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList?>
    fun getLists(user: User): ZettaiOutcome<List<ListName>>
    fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>>
}


class ToDoListHub(
    val queryRunner: ToDoListQueryRunner,
    val commandHandler: CommandHandler<ToDoListCommand, ToDoListEvent, ZettaiError>
) : ZettaiHub {

    override fun handle(command: ToDoListCommand): ZettaiOutcome<List<ToDoListEvent>> =
        commandHandler(command)

    override fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList?> =
        queryRunner {
            listProjection.findList(user, listName)
        }

    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        queryRunner {
            listProjection.findAll(user)
        }

    override fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>> =
        queryRunner {
            listProjection.findAllActiveListId(user)
                .bind { itemProjection.findWhatsNext(10, it) }
                .transform { it.map(ItemProjectionRow::item) }
        }
}

