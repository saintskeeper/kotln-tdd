package com.ubertob.unlearnoop.zettai.json

import EntityIdConverter
import InstantConverter
import ListNameConverter
import LocalDateConverter
import UserConverter
import com.beust.klaxon.Klaxon
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.Parser
import com.ubertob.unlearnoop.zettai.fp.ThrowableError
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjectionRow


val toDoListProjectionParser = Parser(
    parse = ::readProjectionRow,
    render = ::writeProjectionRow
)

fun writeProjectionRow(row: ToDoListProjectionRow): String = klaxon.toJsonString(row)

fun readProjectionRow(json: String): Outcome<ThrowableError, ToDoListProjectionRow> =
    Outcome.tryOrFail { klaxon.parse(json) ?: error("Empty row $json") }

private val klaxon = Klaxon()
    .converter(EntityIdConverter)
    .converter(UserConverter)
    .converter(ListNameConverter)
    .converter(LocalDateConverter)
    .converter(InstantConverter)