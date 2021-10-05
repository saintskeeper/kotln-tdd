package com.ubertob.unlearnoop.zettai.ui

import com.ubertob.unlearnoop.zettai.domain.randomItems
import com.ubertob.unlearnoop.zettai.domain.randomListName
import com.ubertob.unlearnoop.zettai.domain.randomToDoList
import com.ubertob.unlearnoop.zettai.domain.randomUser
import com.ubertob.unlearnoop.zettai.domain.tooling.expectFailure
import com.ubertob.unlearnoop.zettai.domain.tooling.expectSuccess
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

internal class TemplateEngineTest {

    @Test
    fun `replace simple strings`() {

        val data = mapOf("name" tag "Uberto", "surname" tag "Barbini")

        val actual = renderTemplate("Dear {name} {surname}...", data).expectSuccess()

        expectThat(actual).isEqualTo("Dear Uberto Barbini...")

    }

    @Test
    fun `replace string occuring more than once`() {

        val data = mapOf("name" tag "Uberto", "surname" tag "Barbini")

        val actual = renderTemplate("Dear {name} {surname}... Thank you {name} {surname}", data).expectSuccess()

        expectThat(actual).isEqualTo("Dear Uberto Barbini... Thank you Uberto Barbini")

    }

    @Test
    fun `replace multiple lines`() {

        val template = """
            Dear {title} {surname},
            we would like to bring to your attention these task due soon:
            {tasks}  {id} - {taskname} which is due by {due}{/tasks}
            Let me repeat them:
            {tasks} {id} {/tasks}
            Thank you very much {name}.
        """.trimIndent()


        val tasks = listOf(
            mapOf("id" tag "1", "taskname" tag "buy the paint", "due" tag "today"),
            mapOf("id" tag "2", "taskname" tag "paint the wall", "due" tag "tomorrow")
        )
        val data = mapOf("title" tag "Mr", "name" tag "Uberto", "surname" tag "Barbini", "tasks" tag tasks)

        val actual = renderTemplate(template, data).expectSuccess()

        val expected = """Dear Mr Barbini,
              |we would like to bring to your attention these task due soon:
              |  1 - buy the paint which is due by today
              |  2 - paint the wall which is due by tomorrow
              |Let me repeat them:
              | 1 
              | 2 
              |Thank you very much Uberto.""".trimMargin()

        expectThat(actual).isEqualTo(expected)

    }


    @Test
    fun `error if something is not replaced`() {

        val data = mapOf("surname" tag "Barbini")

        val error = renderTemplate("Dear {title} {name} {surname}...", data).expectFailure()

        expectThat(error.msg).isEqualTo("Mappings missing for tags: {title}, {name}")

    }

    @Test
    fun `check not replaced tag in multiple lines`() {

        val template = """
            Dear {title} {surname},
            we would like to bring to your attention these task due soon:
            {tasks}  {id} - {taskname} which is due by {due}{/tasks}
            Thank you very much {name}.
        """.trimIndent()


        val tasks = listOf(
            mapOf("id" tag "1"),
            mapOf("id" tag "2", "taskname" tag "paint the wall")
        )
        val data = mapOf("title" tag "Mr", "name" tag "Uberto", "surname" tag "Barbini", "tasks" tag tasks)

        val actual = renderTemplate(template, data).expectFailure()

        expectThat(actual.msg).isEqualTo("Mappings missing for tags: {taskname}, {due}")

    }

    @Test
    fun `render single list page`() {

        val html = renderListPage(randomUser(), randomToDoList()).expectSuccess()

        expectThat(Jsoup.parse(html.raw)).isNotNull()

    }

    @Test
    fun `render user lists page`() {

        val html = renderListsPage(randomUser(), listOf(randomListName(), randomListName())).expectSuccess()

        expectThat(Jsoup.parse(html.raw)).isNotNull()

    }

    @Test
    fun `render whatsnext page`() {

        val html = renderWhatsNextPage(randomUser(), randomItems()).expectSuccess()

        expectThat(Jsoup.parse(html.raw)).isNotNull()

    }

}

