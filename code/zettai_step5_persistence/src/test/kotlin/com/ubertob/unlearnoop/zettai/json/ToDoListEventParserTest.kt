package com.ubertob.unlearnoop.zettai.json

import com.ubertob.unlearnoop.zettai.domain.eventsGenerator
import com.ubertob.unlearnoop.zettai.domain.tooling.expectSuccess
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ToDoListEventParserTest {

    val eventParser = toDoListEventParser()

    @Test
    fun `convert events to and from`() {

        eventsGenerator().take(100).forEach { event ->

            val conversion = eventParser.render(event)
            val newEvent = eventParser.parse(conversion).expectSuccess()

            expectThat(newEvent).isEqualTo(event)

        }
    }
}