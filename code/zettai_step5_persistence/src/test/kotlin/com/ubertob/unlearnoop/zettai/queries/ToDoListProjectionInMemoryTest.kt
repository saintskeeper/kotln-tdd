package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.eventsourcing.EventSeq
import com.ubertob.unlearnoop.zettai.eventsourcing.StoredEvent
import com.ubertob.unlearnoop.zettai.fp.asSuccess
import java.time.Instant

internal class ToDoListProjectionInMemoryTest : ToDoListProjectionAbstractTest() {

    override fun buildListProjection(events: List<ToDoListEvent>): ToDoListProjection =
        ToDoListProjectionInMemory { after ->
            events.mapIndexed { i, e -> StoredEvent(EventSeq(after.progressive + i + 1), Instant.now(), e) }.asSuccess()
        }.also(ToDoListProjectionInMemory::update)

}


