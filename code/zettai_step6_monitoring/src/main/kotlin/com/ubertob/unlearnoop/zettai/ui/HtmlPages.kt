package com.ubertob.unlearnoop.zettai.ui

import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.ifNotNullOrEmpty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HtmlPage(val raw: String)


fun renderListPage(
    user: User,
    toDoList: ToDoList
): Outcome<TemplateError, HtmlPage> =
    mapOf(
        "user" tag user.name,
        "listname" tag toDoList.listName.name,
        "items" tag toDoList.items.toTagMaps()
    ).renderHtml("/html/single_list_page.html")

fun renderListsPage(
    user: User,
    listNames: List<ListName>
): Outcome<TemplateError, HtmlPage> =
    mapOf(
        "user" tag user.name,
        "listnames" tag listNames.map { mapOf("listname" tag it.name) },
    ).renderHtml("/html/user_lists_page.html")


fun renderWhatsNextPage(
    user: User,
    items: List<ToDoItem>
): Outcome<TemplateError, HtmlPage> = mapOf(
    "user" tag user.name,
    "items" tag items.toTagMaps()
).renderHtml("/html/whatsnew_page.html")

fun TagMap.renderHtml(fileName: String) =
    renderTemplatefromResources(fileName, this)
        .transform(::HtmlPage)

private fun List<ToDoItem>.toTagMaps(): List<TagMap> = map {
    mapOf(
        "description" tag it.description,
        "dueDate" tag it.dueDate?.toIsoString().orEmpty(),
        "status" tag it.status.toString()
    )
}


fun LocalDate.toIsoString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun String?.toIsoLocalDate(): LocalDate? =
    ifNotNullOrEmpty { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }

fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)
