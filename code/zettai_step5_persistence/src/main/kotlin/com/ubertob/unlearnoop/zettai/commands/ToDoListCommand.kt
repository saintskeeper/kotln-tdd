package com.ubertob.unlearnoop.zettai.commands

import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.User

sealed class ToDoListCommand

data class CreateToDoList(val user: User, val name: ListName) : ToDoListCommand()
//data class RenameToDoList(val user: User, val oldName: ListName, val newName: ListName) : ToDoListCommand()
//data class FreezeToDoList(val user: User, val name: ListName, val reason: String) : ToDoListCommand()
//data class RestoreToDoList(val user: User, val name: ListName) : ToDoListCommand()

data class AddToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ToDoListCommand()
//data class EditToDoItem(val user: User, val name: ListName, val oldItem: ToDoItem, val newItem: ToDoItem) : ToDoListCommand()
//data class DeleteToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ToDoListCommand()

