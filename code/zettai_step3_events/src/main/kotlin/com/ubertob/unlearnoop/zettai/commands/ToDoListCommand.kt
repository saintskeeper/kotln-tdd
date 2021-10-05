package com.ubertob.unlearnoop.zettai.commands

import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.User

sealed class ToDoListCommand

data class CreateToDoList(val user: User, val name: ListName) : ToDoListCommand()
//data class RenameToDoList(val user: User, val oldName: ListName, val newName: ListName) : ZettaiCommand()
//data class FreezeToDoList(val user: User, val id: ToDoListId, val reason: String) : ZettaiCommand()
//data class RestoreToDoList(val user: User, val id: ToDoListId) : ZettaiCommand()

data class AddToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ToDoListCommand()
//data class EditToDoItem(val user: User, val name: ListName, val oldItem: ToDoItem, val newItem: ToDoItem) : ZettaiCommand()
//data class DeleteToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ZettaiCommand()
