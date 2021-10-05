package com.ubertob.unlearnoop.zettai.events

import com.ubertob.unlearnoop.zettai.db.fp.ContextReader
import com.ubertob.unlearnoop.zettai.db.fp.bindIfNotNull
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.eventsourcing.EventStore
import com.ubertob.unlearnoop.zettai.eventsourcing.StoredEvent

infix fun ListName.of(user: User): UserListName = UserListName(user, this)

data class UserListName(val user: User, val listName: ListName)

class ToDoListEventStore<CTX>(private val eventStreamer: ToDoListEventStreamer<CTX>) :
    EventStore<CTX, ToDoListEvent, ToDoListState, UserListName> {

    override fun retrieveById(id: ToDoListId): ContextReader<CTX, ToDoListState> =
        eventStreamer.fetchByEntity(id)
            .transform(List<ToDoListEvent>::fold)

    override fun invoke(events: List<ToDoListEvent>): ContextReader<CTX, List<StoredEvent<ToDoListEvent>>> =
        eventStreamer.store(events)

    override fun retrieveByNaturalKey(key: UserListName): ContextReader<CTX, ToDoListState?> =
        eventStreamer.retrieveIdFromNaturalKey(key)
            .bindIfNotNull { entityId -> retrieveById(entityId) }

}

