package com.ubertob.unlearnoop.zettai.webserver

import com.ubertob.unlearnoop.zettai.commands.AddToDoItem
import com.ubertob.unlearnoop.zettai.commands.CreateToDoList
import com.ubertob.unlearnoop.zettai.commands.RenameToDoList
import com.ubertob.unlearnoop.zettai.commands.ToDoListCommand
import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.events.UserListName
import com.ubertob.unlearnoop.zettai.fp.*
import com.ubertob.unlearnoop.zettai.ui.*
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.Status.Companion.UNPROCESSABLE_ENTITY
import org.http4k.core.body.form
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.static
import java.time.LocalDate


class Zettai(val hub: ZettaiHub) : HttpHandler {
    override fun invoke(request: Request): Response = httpHandler(request)

    val httpHandler = routes(
        "/" bind GET to ::homePage,
        "/ping" bind GET to { Response(OK).body("pong") },
        "/todo/{user}/{listname}" bind GET to ::getTodoList,
        "/todo/{user}/{listname}" bind POST to ::addNewItem,
        "/todo/{user}" bind GET to ::getAllLists,
        "/todo/{user}" bind POST to ::createNewList,
        "/todo/{user}/{listname}/rename" bind POST to ::renameList,
        "/whatsnext/{user}" bind GET to ::whatsNext,
        "/static" bind static(Classpath("/static"))
    )

    private fun homePage(request: Request) =
        renderTemplatefromResources("/html/home.html", emptyMap()).transform { Response(OK).body(it) }.recover {
            Response(
                INTERNAL_SERVER_ERROR
            ).body("Ooops... something went wrong!")
        }

    private fun createNewList(request: Request): Response =
        executeCommand(
            ::CreateToDoList
                    `!` request.extractUser()
                    `*` request.extractListNameFromForm("listname")
        ).transform { allListsPath(it.user) }
            .transform { Response(SEE_OTHER).header("Location", it) }
            .recover { Response(UNPROCESSABLE_ENTITY).body(it.msg) }

    private fun renameList(request: Request): Response =
        executeCommand(
            ::RenameToDoList
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractNewListName()
        )
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.newName)) }
            .recover { Response(UNPROCESSABLE_ENTITY).body(it.msg) }


    private fun addNewItem(request: Request): Response =
        executeCommand(
            ::AddToDoItem
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractItem()
        )
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.name)) }
            .recover { Response(UNPROCESSABLE_ENTITY).body(it.msg) }


    private fun getTodoList(req: Request): Response =
        executeQuery(
            ::UserListName
                    `!` req.extractUser()
                    `*` req.extractListName(),
            hub::getList
        )
            .bind { (ul, list) -> renderListPage(ul.user, list) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }

    private fun getAllLists(req: Request): Response =
        executeQuery(
            req.extractUser(),
            hub::getLists
        )
            .bind { (user, items) -> renderListsPage(user, items) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }


    private fun whatsNext(req: Request): Response =
        executeQuery(
            req.extractUser(),
            hub::whatsNext
        )
            .bind { (user, items) -> renderWhatsNextPage(user, items) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(OK).body(htmlPage.raw)

    private fun Request.extractUser(): ZettaiOutcome<User> =
        path("user")
            .failIfNull(InvalidRequestError("User not present"))
            .transform(::User)

    private fun Request.extractListName(): ZettaiOutcome<ListName> =
        path("listname")
            .failIfNull(InvalidRequestError("Invalid list name in path: $this"))
            .bind { ListName.fromUntrusted(it) }

    private fun Request.extractNewListName(): ZettaiOutcome<ListName> =
        form("newListName")
            .failIfNull(ZettaiValidationError("missing listname in form"))
            .bind(ListName.Companion::fromUntrusted)


    private fun Request.extractItem(): ZettaiOutcome<ToDoItem> {
        val duedate = tryOrNull { LocalDate.parse(form("itemdue")) }
        return form("itemname")
            .failIfNull(InvalidRequestError("User not present"))
            .transform { ToDoItem(it, duedate) }
    }


    private fun Request.extractListNameFromForm(formName: String) =
        form(formName)
            .failIfNull(ZettaiValidationError("missing listname in form"))
            .bind { ListName.fromUntrusted(it) }

    private fun allListsPath(user: User) = "/todo/${user.name}"

    private fun todoListPath(
        user: User,
        newListName: ListName
    ) = "/todo/${user.name}/${newListName.name}"


    private fun <C : ToDoListCommand> executeCommand(command: ZettaiOutcome<C>): ZettaiOutcome<C> =
        command.bind(hub::handle)

    private fun <QP, QR> executeQuery(
        queryParams: ZettaiOutcome<QP>,
        query: (QP) -> ZettaiOutcome<QR>
    ): ZettaiOutcome<Pair<QP, QR>> =
        queryParams.bind { qp -> query(qp).transform { qp to it } }

}




