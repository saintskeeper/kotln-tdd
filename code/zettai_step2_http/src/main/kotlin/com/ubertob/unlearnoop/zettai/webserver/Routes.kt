package com.ubertob.unlearnoop.zettai.webserver

import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.domain.ZettaiHub
import com.ubertob.unlearnoop.zettai.ui.HtmlPage
import com.ubertob.unlearnoop.zettai.ui.renderPage
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes


class Zettai(val hub: ZettaiHub) : HttpHandler {
    override fun invoke(request: Request): Response = httpHandler(request)

    val httpHandler = routes(
        "/ping" bind Method.GET to { Response(Status.OK) },
        "/todo/{user}/{listname}" bind Method.GET to ::getTodoList
    )

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(Status.OK).body(htmlPage.raw)

    private fun getTodoList(req: Request): Response {
        val user = req.path("user").orEmpty().let(::User)
        val listName = req.path("listname").orEmpty().let(ListName.Companion::fromUntrusted)

        return listName
            ?.let { hub.getList(user, it) }
            ?.let(::renderPage)
            ?.let(::toResponse)
            ?: Response(Status.BAD_REQUEST)
    }

}


