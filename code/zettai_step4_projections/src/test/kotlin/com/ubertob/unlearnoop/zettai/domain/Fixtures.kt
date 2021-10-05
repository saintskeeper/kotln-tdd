package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommandHandler
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStore
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStreamerInMemory
import com.ubertob.unlearnoop.zettai.queries.ToDoListQueryRunner
import com.ubertob.unlearnoop.zettai.webserver.Zettai


fun prepareToDoListHubForTests(): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore)
    val queryRunner = ToDoListQueryRunner(streamer::fetchAfter)
    return ToDoListHub(queryRunner, cmdHandler, eventStore)
}


fun prepareZettaiForTests(): Zettai {
    return Zettai(
        prepareToDoListHubForTests()
    )
}