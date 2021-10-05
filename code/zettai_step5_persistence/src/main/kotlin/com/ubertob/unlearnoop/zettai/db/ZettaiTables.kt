package com.ubertob.unlearnoop.zettai.db.jdbc


import com.ubertob.unlearnoop.zettai.db.eventsourcing.PgEventTable
import com.ubertob.unlearnoop.zettai.db.eventsourcing.PgLastEventTable
import com.ubertob.unlearnoop.zettai.db.eventsourcing.PgProjectionTable
import com.ubertob.unlearnoop.zettai.eventsourcing.EventSeq
import com.ubertob.unlearnoop.zettai.json.toDoListProjectionParser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource


fun resetDatabase(datasource: DataSource) {

    val db = Database.connect(datasource)

    transaction(db) {
        addLogger(StdOutSqlLogger)

        dropTables()
        prepareDb()
    }
}

fun prepareDatabase(datasource: DataSource) {

    val db = Database.connect(datasource)

    transaction(db) {
        addLogger(StdOutSqlLogger)

        prepareDb()
    }
}

private fun dropTables() {
    SchemaUtils.drop(toDoListEventsTable, toDoListProjectionTable, toDoListLastEventTable)
}

private fun Transaction.prepareDb() {
    SchemaUtils.createMissingTablesAndColumns(
        toDoListEventsTable,
        toDoListProjectionTable,
        toDoListLastEventTable
    )
    toDoListLastEventTable.insertLastEvent(EventSeq(-1)).runWith(this)
}


val toDoListEventsTable =
    PgEventTable("todo_list_events")


val toDoListProjectionTable =
    PgProjectionTable("todo_list_projection", toDoListProjectionParser)


val toDoListLastEventTable =
    PgLastEventTable("${toDoListProjectionTable.tableName}_last_processed_event")

