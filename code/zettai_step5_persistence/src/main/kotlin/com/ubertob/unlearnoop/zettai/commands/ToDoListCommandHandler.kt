package com.ubertob.unlearnoop.zettai.commands

import com.ubertob.unlearnoop.zettai.db.fp.ContextReader
import com.ubertob.unlearnoop.zettai.db.fp.bindIfSuccess
import com.ubertob.unlearnoop.zettai.db.jdbc.ContextProvider
import com.ubertob.unlearnoop.zettai.domain.InconsistentStateError
import com.ubertob.unlearnoop.zettai.domain.ToDoListCommandError
import com.ubertob.unlearnoop.zettai.domain.ZettaiError
import com.ubertob.unlearnoop.zettai.domain.ZettaiOutcome
import com.ubertob.unlearnoop.zettai.events.*
import com.ubertob.unlearnoop.zettai.fp.asFailure
import com.ubertob.unlearnoop.zettai.fp.asSuccess
import com.ubertob.unlearnoop.zettai.fp.join

typealias CommandOutcomeReader<CTX> = ContextReader<CTX, ToDoListCommandOutcome>

typealias ToDoListCommandOutcome = ZettaiOutcome<List<ToDoListEvent>>

class ToDoListCommandHandler<CTX>(
    private val contextProvider: ContextProvider<CTX>,
    private val eventStore: ToDoListEventStore<CTX>
) : (ToDoListCommand) -> ToDoListCommandOutcome {

    override fun invoke(command: ToDoListCommand): ToDoListCommandOutcome =
        contextProvider.tryRun(
            when (command) {
                is CreateToDoList -> command.execute()
                is AddToDoItem -> command.execute()
            }.bindIfSuccess(eventStore)
        ).join()
            .transform { storedEvents -> storedEvents.map { it.event } }
            .transformFailure { it as? ZettaiError ?: ToDoListCommandError(it.msg) }


    private fun CreateToDoList.execute(): CommandOutcomeReader<CTX> =
        eventStore.retrieveByNaturalKey(UserListName(user, name))
            .transform { listState ->
                when (listState) {
                    null -> ListCreated(ToDoListId.mint(), user, name).toList().asSuccess()
                    else -> InconsistentStateError(this, listState).asFailure()
                }
            }

    private fun AddToDoItem.execute(): CommandOutcomeReader<CTX> =
        eventStore.retrieveByNaturalKey(UserListName(user, name))
            .transform { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            ToDoListCommandError("cannot have 2 items with same name").asFailure()
                        else {
                            ItemAdded(listState.id, item).toList().asSuccess()
                        }
                    }
                    null -> ToDoListCommandError("list $name not found").asFailure()
                    else -> InconsistentStateError(this, listState).asFailure()
                }
            }


    private fun ToDoListEvent.toList(): List<ToDoListEvent> = listOf(this)

}

