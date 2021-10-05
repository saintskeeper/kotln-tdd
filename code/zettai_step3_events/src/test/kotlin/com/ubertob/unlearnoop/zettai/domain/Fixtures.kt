package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.commands.ToDoListCommandHandler
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStore
import com.ubertob.unlearnoop.zettai.events.ToDoListEventStreamerInMemory
import com.ubertob.unlearnoop.zettai.webserver.Zettai


fun prepareToDoListHubForTests(fetcher: ToDoListFetcherFromMap): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore, fetcher)
    return ToDoListHub(fetcher, cmdHandler, eventStore)
}


fun prepareZettaiForTests(): Zettai {
    return Zettai(
        prepareToDoListHubForTests(
            ToDoListFetcherFromMap(
                mutableMapOf()
            )
        )
    )
}