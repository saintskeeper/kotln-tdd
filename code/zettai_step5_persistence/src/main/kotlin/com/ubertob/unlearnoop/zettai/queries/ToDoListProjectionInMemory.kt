package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.events.ToDoListId
import com.ubertob.unlearnoop.zettai.eventsourcing.ConcurrentMapProjection
import com.ubertob.unlearnoop.zettai.eventsourcing.EntityId
import com.ubertob.unlearnoop.zettai.eventsourcing.FetchStoredEvents
import com.ubertob.unlearnoop.zettai.eventsourcing.InMemoryProjection
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError

class ToDoListProjectionInMemory(eventFetcher: FetchStoredEvents<ToDoListEvent>) : ToDoListProjection,
    InMemoryProjection<ToDoListProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        InMemoryProjectionProvider(),
        eventFetcher,
        ToDoListProjection.Companion::eventProjector
    ) {

    override fun findAll(user: User): Outcome<OutcomeError, List<ListName>> =
        allRows().transform {
            it.values
                .filter { it.user == user }
                .map { it.list.listName }
        }

    override fun findList(user: User, name: ListName): Outcome<OutcomeError, ToDoList?> =
        allRows().transform {
            it.values
                .firstOrNull { it.user == user && it.list.listName == name }
                ?.list
        }

    override fun findAllActiveListId(user: User): Outcome<OutcomeError, List<EntityId>> =
        allRows().transform {
            it.filter { it.value.user == user && it.value.active }
                .map { ToDoListId.fromRowId(it.key) }
        }

}
