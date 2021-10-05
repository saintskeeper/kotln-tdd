package com.ubertob.unlearnoop.zettai.webserver

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommandHandler
import com.ubertob.unlearnoop.zettai.db.createToDoListEventStreamerOnPg
import com.ubertob.unlearnoop.zettai.db.jdbc.PgDataSource
import com.ubertob.unlearnoop.zettai.db.jdbc.TransactionIsolationLevel
import com.ubertob.unlearnoop.zettai.db.jdbc.TransactionProvider
import com.ubertob.unlearnoop.zettai.db.jdbc.prepareDatabase
import com.ubertob.unlearnoop.zettai.domain.ToDoListHub
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStore
import com.ubertob.unlearnoop.zettai.queries.ToDoItemProjection
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjectionOnPg
import com.ubertob.unlearnoop.zettai.queries.ToDoListQueryRunner
import org.http4k.server.Jetty
import org.http4k.server.asServer


fun main() {

    val dataSource = prepareProductionDatabase()

    val streamer = createToDoListEventStreamerOnPg()
    val eventStore = ToDoListEventStore(streamer)
    val txProvider = TransactionProvider(dataSource, TransactionIsolationLevel.Serializable)

    val commandHandler = ToDoListCommandHandler(txProvider, eventStore)

    val fetcher = txProvider.runWith(streamer::fetchAfter)

    val queryHandler = ToDoListQueryRunner(
        ToDoListProjectionOnPg(txProvider, fetcher),
        ToDoItemProjection(fetcher)
    )

    val hub = ToDoListHub(queryHandler, commandHandler)

    Zettai(hub).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/username")

}

private fun prepareProductionDatabase(): PgDataSource {
    val dataSource = PgDataSource.create(
        host = "localhost",
        port = 6432,
        database = "zettai_db",
        dbUser = "zettai_admin",
        dbPassword = "zettai!" //you should get it from a secret repo
    )

    prepareDatabase(dataSource)
    return dataSource
}


