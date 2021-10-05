package com.ubertob.unlearnoop.zettai.json

import EntityIdConverter
import InstantConverter
import ListNameConverter
import LocalDateConverter
import UserConverter
import com.beust.klaxon.Klaxon
import com.ubertob.unlearnoop.zettai.db.eventsourcing.PgEvent
import com.ubertob.unlearnoop.zettai.domain.ZettaiOutcome
import com.ubertob.unlearnoop.zettai.domain.ZettaiParsingError
import com.ubertob.unlearnoop.zettai.events.*
import com.ubertob.unlearnoop.zettai.fp.Parser
import com.ubertob.unlearnoop.zettai.fp.asFailure
import com.ubertob.unlearnoop.zettai.fp.asSuccess
import com.ubertob.unlearnoop.zettai.fp.tryOrNull


fun toDoListEventParser(): Parser<ToDoListEvent, PgEvent> = Parser(::toPgEvent, ::toToDoListEvent)


fun toPgEvent(event: ToDoListEvent): PgEvent =
    PgEvent(
        entityId = event.id,
        eventType = event::class.simpleName.orEmpty(),
        version = 1,
        source = "event store",
        jsonString = event.toJsonString()
    )


private val klaxon = Klaxon()
    .converter(EntityIdConverter)
    .converter(UserConverter)
    .converter(ListNameConverter)
    .converter(LocalDateConverter)
    .converter(InstantConverter)


fun toToDoListEvent(pgEvent: PgEvent): ZettaiOutcome<ToDoListEvent> =
    tryOrNull {
        when (pgEvent.eventType) {
            ListCreated::class.simpleName -> klaxon.parse<ListCreated>(pgEvent.jsonString)
            ItemAdded::class.simpleName -> klaxon.parse<ItemAdded>(pgEvent.jsonString)
            ItemRemoved::class.simpleName -> klaxon.parse<ItemRemoved>(pgEvent.jsonString)
            ItemModified::class.simpleName -> klaxon.parse<ItemModified>(pgEvent.jsonString)
            ListPutOnHold::class.simpleName -> klaxon.parse<ListPutOnHold>(pgEvent.jsonString)
            ListClosed::class.simpleName -> klaxon.parse<ListClosed>(pgEvent.jsonString)
            ListReleased::class.simpleName -> klaxon.parse<ListReleased>(pgEvent.jsonString)
            ListRenamed::class.simpleName -> klaxon.parse<ListRenamed>(pgEvent.jsonString)
            else -> null
        }
    }?.asSuccess()
        ?: ZettaiParsingError("Error parsing ToDoListEvent: ${pgEvent}").asFailure()


fun ToDoListEvent.toJsonString() = when (this) {
    is ListCreated -> klaxon.toJsonString(this)
    is ItemAdded -> klaxon.toJsonString(this)
    is ItemRemoved -> klaxon.toJsonString(this)
    is ItemModified -> klaxon.toJsonString(this)
    is ListPutOnHold -> klaxon.toJsonString(this)
    is ListReleased -> klaxon.toJsonString(this)
    is ListClosed -> klaxon.toJsonString(this)
    is ListRenamed -> klaxon.toJsonString(this)
}





