package com.ubertob.unlearnoop.zettai.db

import com.ubertob.unlearnoop.zettai.db.eventsourcing.EventStreamerTx
import com.ubertob.unlearnoop.zettai.db.jdbc.toDoListEventsTable
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.events.UserListName
import com.ubertob.unlearnoop.zettai.eventsourcing.EventStreamer
import com.ubertob.unlearnoop.zettai.json.toDoListEventParser
import org.jetbrains.exposed.sql.Transaction


typealias ToDoListEventStreamerTx = EventStreamer<Transaction, ToDoListEvent, UserListName>

fun createToDoListEventStreamerOnPg(): ToDoListEventStreamerTx =
    EventStreamerTx(toDoListEventsTable, toDoListEventParser()) {
        """
select
	entity_id 
from
	todo_list_events
where
	event_type = 'ListCreated'
	and json_data ->> 'owner' = '${it.user.name}'
	and json_data ->> 'name' = '${it.listName.name}'
        """.trimIndent()
    }