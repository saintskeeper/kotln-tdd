package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommandHandler
import com.ubertob.unlearnoop.zettai.db.createToDoListEventStreamerOnPg
import com.ubertob.unlearnoop.zettai.db.jdbc.PgDataSource
import com.ubertob.unlearnoop.zettai.db.jdbc.TransactionIsolationLevel
import com.ubertob.unlearnoop.zettai.db.jdbc.TransactionProvider
import com.ubertob.unlearnoop.zettai.events.EventStreamerInMemory
import com.ubertob.unlearnoop.zettai.events.InMemoryEventsProvider
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStore
import com.ubertob.unlearnoop.zettai.eventsourcing.EventSeq
import com.ubertob.unlearnoop.zettai.queries.ToDoItemProjection
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjectionInMemory
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjectionOnPg
import com.ubertob.unlearnoop.zettai.queries.ToDoListQueryRunner
import com.ubertob.unlearnoop.zettai.webserver.Zettai


fun prepareToDoListHubInMemory(): ToDoListHub {
    val streamer = EventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val inMemoryEvents = InMemoryEventsProvider()
    val cmdHandler = ToDoListCommandHandler(inMemoryEvents, eventStore)
    val fetcher = { lastEvent: EventSeq ->
        inMemoryEvents.tryRun(streamer.fetchAfter(lastEvent))
    }
    val queryRunner = ToDoListQueryRunner(
        ToDoListProjectionInMemory(fetcher),
        ToDoItemProjection(fetcher)
    )

    return ToDoListHub(queryRunner, cmdHandler)
}


fun prepareZettaiOnTestDatabase(): Zettai {
    val dataSource = pgDataSourceForTest()

    val streamer = createToDoListEventStreamerOnPg()
    val eventStore = ToDoListEventStore(streamer)
    val txProvider = TransactionProvider(dataSource, TransactionIsolationLevel.Serializable)

    val commandHandler = ToDoListCommandHandler(txProvider, eventStore)

    val fetcher = { lastEvent: EventSeq ->
        txProvider.tryRun(streamer.fetchAfter(lastEvent))
    }

    val queryHandler = ToDoListQueryRunner(ToDoListProjectionOnPg(txProvider, fetcher), ToDoItemProjection(fetcher))

    return Zettai(ToDoListHub(queryHandler, commandHandler))
}

fun pgDataSourceForTest(): PgDataSource =
    PgDataSource.create(
        host = "localhost",
        port = 6433,
        database = "zettai_db_test",
        dbUser = "zettai_test",
        dbPassword = "test123"
    )

