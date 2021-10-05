package com.ubertob.unlearnoop.zettai.events

import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoListRetriever
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.fp.EventPersister


class ToDoListEventStore(private val eventStreamer: ToDoListEventStreamer) : ToDoListRetriever,
    EventPersister<ToDoListEvent> {

    override fun retrieveById(id: ToDoListId): ToDoListState? =
        eventStreamer(id)
            ?.fold()

    override fun retrieveByName(user: User, listName: ListName): ToDoListState? =
        eventStreamer.retrieveIdFromName(user, listName)
            ?.let(::retrieveById)

    override fun invoke(events: List<ToDoListEvent>): List<ToDoListEvent> =
        eventStreamer.store(events)

}

