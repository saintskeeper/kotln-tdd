package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.events.ToDoListId
import com.ubertob.unlearnoop.zettai.events.ToDoListState
import com.ubertob.unlearnoop.zettai.fp.EntityRetriever

interface ToDoListRetriever : EntityRetriever<ToDoListState, ToDoListEvent> {

    fun retrieveByName(user: User, listName: ListName): ToDoListState?

    override fun retrieveById(id: ToDoListId): ToDoListState?

}