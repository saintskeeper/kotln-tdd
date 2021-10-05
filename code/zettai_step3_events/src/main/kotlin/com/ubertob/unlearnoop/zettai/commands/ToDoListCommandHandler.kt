package com.ubertob.unlearnoop.zettai.commands

import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.ToDoListRetriever
import com.ubertob.unlearnoop.zettai.domain.ToDoListUpdatableFetcher
import com.ubertob.unlearnoop.zettai.events.*

class ToDoListCommandHandler(
    val entityRetriever: ToDoListRetriever,
    private val readModelListener: ToDoListUpdatableFetcher //temporary needed to update the read model
) : (ToDoListCommand) -> List<ToDoListEvent>? {

    override fun invoke(command: ToDoListCommand): List<ToDoListEvent>? =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): List<ToDoListEvent>? {
        val listState = entityRetriever.retrieveByName(user, name) ?: InitialState
        return when (listState) {
            InitialState -> {
                readModelListener.addListToUser(
                    user,
                    ToDoList(name, emptyList())
                )
                ListCreated(ToDoListId.mint(), user, name).toList()
            }
            is ActiveToDoList,
            is OnHoldToDoList,
            is ClosedToDoList -> null //command fail
        }
    }

    private fun AddToDoItem.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            null //cannot have 2 items with same name
                        else {
                            readModelListener.addItemToList(user, listState.name, item)
                            ItemAdded(listState.id, item).toList()
                        }
                    }
                    InitialState,
                    is OnHoldToDoList,
                    is ClosedToDoList -> null //command fail
                }
            }

    private fun ToDoListEvent.toList(): List<ToDoListEvent> = listOf(this)

}
