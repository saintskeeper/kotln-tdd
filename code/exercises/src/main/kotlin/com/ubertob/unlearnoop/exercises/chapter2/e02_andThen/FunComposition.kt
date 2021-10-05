package com.ubertob.unlearnoop.exercises.chapter2.e02_andThen

import java.net.URI

typealias User = String
typealias ListName = String
typealias ToDoList = List<String>

data class Request(
    val method: String,
    val uri: URI,
    val body: String
)

data class Response(
    val status: Int,
    val body: String
)

typealias HttpHandler = (Request) -> Response

data class Html(val raw: String)

fun extractListData(request: Request): Pair<User, ListName> = TODO()
fun fetchListContent(listId: Pair<User, ListName>): ToDoList = TODO()
fun renderHtml(list: ToDoList): Html = TODO()
fun createResponse(html: Html): Response = TODO()

//fun fetchList(request: Request): Response =
//    createResponse(
//        renderHtml(
//            fetchListContent(
//                extractListData(
//                    request
//                )
//            )
//        )
//    )

//fun fetchList(request: Request): Response =
//    request.let(::extractListData)
//        .let(::fetchListContent)
//        .let(::renderHtml)
//        .let(::createResponse)

typealias FUN<A, B> = (A) -> B

infix fun <A, B, C> FUN<A, B>.andThen(other: FUN<B, C>): FUN<A, C> = { a: A -> other(this(a)) }

val hof = ::extractListData andThen
          ::fetchListContent andThen
          ::renderHtml andThen
          ::createResponse

fun fetchList(request: Request): Response = hof(request)


infix fun <A: Any, B: Any, C: Any> FUN<A, B?>.andThenNN(other: FUN<B, C?>): FUN<A, C?> = { a: A -> this(a)?.let {  other(it) } }

val hofNN = ::extractListData andThenNN
        ::fetchListContent andThenNN
        ::renderHtml andThenNN
        ::createResponse

fun fetchListNN(request: Request): Response = hofNN(request) ?: Response(404, "Not found")
