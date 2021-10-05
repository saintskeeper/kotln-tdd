package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommand
import com.ubertob.unlearnoop.zettai.db.jdbc.ContextError
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.events.ToDoListState
import com.ubertob.unlearnoop.zettai.events.UserListName
import com.ubertob.unlearnoop.zettai.fp.*
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

data class ZettaiValidationError(override val errors: List<String>) : ZettaiError(), ValidationError {

    constructor(error: String) : this(listOf(error))

    override fun combineErrors(other: ValidationError): ZettaiValidationError =
        ZettaiValidationError(errors + other.errors)

    override val msg: String = errors.joinToString()
}


typealias ZettaiOutcome<T> = Outcome<ZettaiError, T>

interface ZettaiHub {
    fun <C : ToDoListCommand> handle(command: C): ZettaiOutcome<C>
    fun getList(userListName: UserListName): ZettaiOutcome<ToDoList>
    fun getLists(user: User): ZettaiOutcome<List<ListName>>
    fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>>
}


class ToDoListHub(
    val queryRunner: ToDoListQueryRunner,
    val commandHandler: CommandHandler<ToDoListCommand, ToDoListEvent, ZettaiError>
) : ZettaiHub {

    override fun <C : ToDoListCommand> handle(command: C): ZettaiOutcome<C> =
        commandHandler(command).transform { command }

    override fun getList(userListName: UserListName): ZettaiOutcome<ToDoList> =
        queryRunner {
            listProjection.findList(userListName.user, userListName.listName)
        }.failIfNull(InvalidRequestError("List ${userListName.listName} not found!"))


    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        queryRunner {
            listProjection.findAll(user)
        }

    override fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>> =
        queryRunner {
            listProjection.findAllActiveListId(user)
                .bind { itemProjection.findWhatsNext(10, it) }
                .transform { it.map(ItemProjectionRow::item) }
        }
}

