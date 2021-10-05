package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.db.eventsourcing.PgProjection
import com.ubertob.unlearnoop.zettai.db.jdbc.ContextProvider
import com.ubertob.unlearnoop.zettai.db.jdbc.toDoListLastEventTable
import com.ubertob.unlearnoop.zettai.db.jdbc.toDoListProjectionTable
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.eventsourcing.EntityId
import com.ubertob.unlearnoop.zettai.eventsourcing.FetchStoredEvents
import com.ubertob.unlearnoop.zettai.eventsourcing.Projection
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError
import org.jetbrains.exposed.sql.Transaction

class ToDoListProjectionOnPg(
    txProvider: ContextProvider<Transaction>,
    readEvents: FetchStoredEvents<ToDoListEvent>
) : ToDoListProjection,
    Projection<Transaction, ToDoListProjectionRow, ToDoListEvent> by PgProjection(
        txProvider,
        readEvents,
        ToDoListProjection.Companion::eventProjector,
        projectionTable = toDoListProjectionTable,
        lastEventTable = toDoListLastEventTable
    ) {

    override fun findAll(user: User): Outcome<OutcomeError, List<ListName>> =
        contextProvider.tryRun(
            toDoListProjectionTable.selectRowsByJson(findAllByUserSQL(user))
        ).transform { it.map { row -> row.list.listName } }


    override fun findList(user: User, name: ListName): Outcome<OutcomeError, ToDoList?> =
        contextProvider.tryRun(
            toDoListProjectionTable.selectRowsByJson(findByNameSQL(user, name))
        ).transform { it.firstOrNull()?.list }


    override fun findAllActiveListId(user: User): Outcome<OutcomeError, List<EntityId>> =
        contextProvider.tryRun(
            toDoListProjectionTable.selectRowsByJson(findAllActiveSQL(user))
        ).transform { it.map { it.id } }

    private fun findAllByUserSQL(user: User): String =
        """
            select
                *
            from
                ${toDoListProjectionTable.tableName}
            where
                row_data ->> 'user' = '${user.name}'
        """.trimIndent()

    private fun findByNameSQL(user: User, listName: ListName): String =
        """
            select
                *
            from
                ${toDoListProjectionTable.tableName}
            where
                row_data ->> 'user' = '${user.name}'
	            and row_data ->'list'->>'listName' = '${listName.name}' 
        """.trimIndent()

    private fun findAllActiveSQL(user: User): String =
        """
            select
                *
            from
                ${toDoListProjectionTable.tableName}
            where
                row_data ->> 'user' = '${user.name}'
            	and row_data ->>'active' = 'true'
        """.trimIndent()
}

