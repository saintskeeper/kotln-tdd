package com.ubertob.unlearnoop.zettai.webserver

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommandHandler
import com.ubertob.unlearnoop.zettai.domain.ToDoListHub
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStore
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStreamerInMemory
import com.ubertob.unlearnoop.zettai.queries.ToDoListQueryRunner
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val commandHandler = ToDoListCommandHandler(eventStore)
    val queryHandler = ToDoListQueryRunner(streamer::fetchAfter)

    val hub = ToDoListHub(queryHandler, commandHandler, eventStore)

    Zettai(hub).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto")

}


