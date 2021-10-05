package com.ubertob.unlearnoop.zettai.commands

import com.ubertob.unlearnoop.zettai.domain.InconsistentStateError
import com.ubertob.unlearnoop.zettai.domain.ToDoListCommandError
import com.ubertob.unlearnoop.zettai.domain.ToDoListRetriever
import com.ubertob.unlearnoop.zettai.domain.ZettaiOutcome
import com.ubertob.unlearnoop.zettai.events.*
import com.ubertob.unlearnoop.zettai.fp.asFailure
import com.ubertob.unlearnoop.zettai.fp.asSuccess

typealias ToDoListCommandOutcome = ZettaiOutcome<List<ToDoListEvent>>

class ToDoListCommandHandler(
    val entityRetriever: ToDoListRetriever
) : (ToDoListCommand) -> ToDoListCommandOutcome {

    override fun invoke(command: ToDoListCommand): ToDoListCommandOutcome =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
//            is RenameToDoList -> command.execute()
//            is FreezeToDoList -> command.execute()
//            is RestoreToDoList -> command.execute()
//            is EditToDoItem -> command.execute()
//            is DeleteToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): ToDoListCommandOutcome {
        val listState = entityRetriever.retrieveByName(user, name) ?: InitialState
        return when (listState) {
            InitialState -> ListCreated(ToDoListId.mint(), user, name).toList().asSuccess()
            is ActiveToDoList,
            is OnHoldToDoList,
            is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()
        }
    }

    private fun AddToDoItem.execute(): ToDoListCommandOutcome =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            ToDoListCommandError("cannot have 2 items with same name").asFailure()
                        else {
                            ItemAdded(listState.id, item).toList().asSuccess()
                        }
                    }
                    InitialState,
                    is OnHoldToDoList,
                    is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()
                }
            } ?: ToDoListCommandError("list $name not found").asFailure()

    private fun ToDoListEvent.toList(): List<ToDoListEvent> = listOf(this)

}


