package com.ubertob.unlearnoop.zettai.events

import com.ubertob.unlearnoop.zettai.db.fp.ContextReader
import com.ubertob.unlearnoop.zettai.db.jdbc.ContextError
import com.ubertob.unlearnoop.zettai.db.jdbc.ContextProvider
import com.ubertob.unlearnoop.zettai.eventsourcing.EntityId
import com.ubertob.unlearnoop.zettai.eventsourcing.EventSeq
import com.ubertob.unlearnoop.zettai.eventsourcing.EventStreamer
import com.ubertob.unlearnoop.zettai.eventsourcing.StoredEvent
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.asFailure
import com.ubertob.unlearnoop.zettai.fp.asSuccess
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference


typealias ToDoListEventStreamer<RES> = EventStreamer<RES, ToDoListEvent, UserListName>

typealias ToDoListStoredEvent = StoredEvent<ToDoListEvent>

typealias ToDoListInMemoryRef = AtomicReference<List<ToDoListStoredEvent>>

typealias InMemoryEventsReader<T> = ContextReader<ToDoListInMemoryRef, T>

data class ToDoListEventsError(override val msg: String, override val exception: Throwable?) : ContextError

class InMemoryEventsProvider() : ContextProvider<ToDoListInMemoryRef> {

    val events = AtomicReference<List<ToDoListStoredEvent>>(listOf())

    override fun <T> tryRun(reader: InMemoryEventsReader<T>): Outcome<ContextError, T> =
        try {
            reader.runWith(events).asSuccess()
        } catch (t: Throwable) {
            ToDoListEventsError("Operation failed because ${t.message}", t).asFailure()
        }
}

class EventStreamerInMemory : ToDoListEventStreamer<ToDoListInMemoryRef> {

    override fun store(newEvents: Iterable<ToDoListEvent>): InMemoryEventsReader<List<StoredEvent<ToDoListEvent>>> =
        InMemoryEventsReader { events ->
            newEvents.toSavedEvents(events.get().size.toLong()).also { ne ->
                events.updateAndGet {
                    it + ne
                }
            }
        }


    override fun fetchByEntity(entityId: ToDoListId): InMemoryEventsReader<List<ToDoListEvent>> =
        InMemoryEventsReader { events ->
            events.get()
                .map(ToDoListStoredEvent::event)
                .filter { it.id == entityId }
        }

    override fun fetchAfter(eventSeq: EventSeq): InMemoryEventsReader<List<ToDoListStoredEvent>> =
        InMemoryEventsReader { events ->
            events.get()
                .dropWhile { it.eventSeq <= eventSeq }
        }

    private fun Iterable<ToDoListEvent>.toSavedEvents(last: Long): List<StoredEvent<ToDoListEvent>> =
        this.mapIndexed { index, event ->
            ToDoListStoredEvent(EventSeq(last + index), Instant.now(), event)
        }


    override fun retrieveIdFromNaturalKey(key: UserListName): InMemoryEventsReader<EntityId?> =
        InMemoryEventsReader { events ->
            events.get()
                .map(ToDoListStoredEvent::event)
                .firstOrNull { it == ListCreated(it.id, key.user, key.listName) }
                ?.id
        }
}


