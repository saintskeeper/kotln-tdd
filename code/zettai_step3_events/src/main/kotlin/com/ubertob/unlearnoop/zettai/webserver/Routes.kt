package com.ubertob.unlearnoop.zettai.webserver

import com.ubertob.unlearnoop.zettai.commands.AddToDoItem
import com.ubertob.unlearnoop.zettai.commands.CreateToDoList
import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.domain.ZettaiHub
import com.ubertob.unlearnoop.zettai.fp.tryOrNull
import com.ubertob.unlearnoop.zettai.ui.HtmlPage
import com.ubertob.unlearnoop.zettai.ui.renderListPage
import com.ubertob.unlearnoop.zettai.ui.renderListsPage
import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import java.time.LocalDate


class Zettai(val hub: ZettaiHub) : HttpHandler {
    override fun invoke(request: Request): Response = httpHandler(request)

    val httpHandler = routes(
        "/ping" bind Method.GET to { Response(Status.OK) },
        "/todo/{user}/{listname}" bind Method.GET to ::getTodoList,
        "/todo/{user}/{listname}" bind Method.POST to ::addNewItem,
        "/todo/{user}" bind Method.GET to ::getAllLists,
        "/todo/{user}" bind Method.POST to ::createNewList
    )

    private fun createNewList(request: Request): Response {
        val user = request.extractUser()
        return request.form("listname")
            ?.let(ListName.Companion::fromUntrusted)
            ?.let { CreateToDoList(user, it) }
            ?.let(hub::handle)
            ?.let { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}") }
            ?: Response(Status.BAD_REQUEST)
    }

    private fun addNewItem(request: Request): Response {
        val user = request.extractUser()
        val listName = request.extractListName()
        return request.extractItem()
            ?.let { AddToDoItem(user, listName, it) }
            ?.let(hub::handle)
            ?.let { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(Status.BAD_REQUEST)
    }

    private fun getTodoList(req: Request): Response {
        val user = req.extractUser()
        val listName = req.path("listname").orEmpty()
            .let(ListName.Companion::fromUntrusted)

        return listName
            ?.let { hub.getList(user, it) }
            ?.let { renderListPage(user, it) }
            ?.let(::toResponse)
            ?: Response(Status.NOT_FOUND)
    }

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(Status.OK).body(htmlPage.raw)

    private fun getAllLists(req: Request): Response {
        val user = req.extractUser()

        return hub.getLists(user)
            ?.let { renderListsPage(user, it) }
            ?.let(::toResponse)
            ?: Response(Status.BAD_REQUEST)
    }

    private fun Request.extractUser(): User = path("user").orEmpty().let(::User)
    private fun Request.extractListName(): ListName =
        path("listname").orEmpty().let(ListName.Companion::fromUntrustedOrThrow)

    private fun Request.extractItem(): ToDoItem? {
        val name = form("itemname") ?: return null
        val duedate = tryOrNull { LocalDate.parse(form("itemdue")) }
        return ToDoItem(name, duedate)
    }

}


