@file:Suppress("DANGEROUS_CHARACTERS")

package com.ubertob.unlearnoop.zettai.fp

import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoStatus
import com.ubertob.unlearnoop.zettai.domain.ZettaiValidationError
import java.time.LocalDate


fun <A, B, C, E : OutcomeError> Pair<Outcome<E, A>, Outcome<E, B>>.transform2(f: (A, B) -> C): Outcome<E, C> =
    first.combine(second).transform { f(it.first, it.second) }

fun <A, B, C> Pair<List<A>, List<B>>.map2(f: (A, B) -> C): List<C> =
    first.zip(second).map { f(it.first, it.second) }

fun <A, B, C> lift2(f: (A, B) -> C): (List<A>) -> (List<B>) -> List<C> =
    { a -> { b -> f applyOn a andOn b } }


fun <A, B, C, D> Triple<List<A>, List<B>, List<C>>.map3(f: (A, B, C) -> D): List<D> =
    f applyOn first andOn second andOn third

fun <A, B, C, D> lift3(f: (A, B, C) -> D): (List<A>) -> (List<B>) -> (List<C>) -> List<D> =
    { a -> { b -> { c -> f applyOn a andOn b andOn c } } }


infix fun <A, B, D, ER : OutcomeError> ((A, B) -> D).`!`(other: Outcome<ER, A>): Outcome<ER, (B) -> D> =
    other.transform { a -> { this(a, it) } }

infix fun <A, B, C, D, ER : OutcomeError> ((A, B, C) -> D).`!`(other: Outcome<ER, A>): Outcome<ER, (B) -> (C) -> D> =
    other.transform { a -> { b -> { this(a, b, it) } } }

infix fun <A, B, ER : OutcomeError> Outcome<ER, (A) -> B>.`*`(a: Outcome<ER, A>): Outcome<ER, B> =
    bind { a.transform(it) }


//(++) <$> ["ha","heh","hmm"] <*> ["?","!","."]
//["ha?","ha!","ha.","heh?","heh!","heh.","hmm?","hmm!","hmm."]

infix fun <A, B> List<(A) -> B>.andOn(a: List<A>): List<B> = flatMap { a.map(it) }
infix fun <A, B> List<(A) -> B>.`*`(a: List<A>): List<B> = andOn(a)


//<$> or ap fmap
@JvmName("apply2")
infix fun <A, B, C> ((A, B) -> C).applyOn(other: List<A>): List<(B) -> C> = other.map { x -> { this(x, it) } }
infix fun <A, B, C> ((A, B) -> C).`!`(other: List<A>): List<(B) -> C> = applyOn(other)


@JvmName("apply3")
infix fun <A, B, C, D> ((A, B, C) -> D).applyOn(other: List<A>): List<(B) -> (C) -> D> =
    other.map { x -> { y -> { this(x, y, it) } } }


infix fun <A, B, C, D> ((A, B, C) -> D).`!`(other: List<A>): List<(B) -> (C) -> D> = applyOn(other)

fun main() {


    val multiLang = (listOf("hello ", "ciao ", "hola ") to listOf("world", "mondo", "mundo")).map2(String::plus)
    println(multiLang.joinToString())
    //hello world, ciao mondo, hola mundo

    val applExpressions = String::plus applyOn listOf("ha", "heh", "hmm", "ah") andOn listOf("?", "!", ".")
    println(applExpressions.joinToString())
//    ha?, ha!, ha., heh?, heh!, heh., hmm?, hmm!, hmm., ah?, ah!, ah.


    val names = listOf("a", "b", "c")
    val dates = listOf(LocalDate.now(), LocalDate.now().minusDays(1))
    val statuses = listOf(ToDoStatus.Todo)


    val items = ::ToDoItem applyOn names andOn dates andOn statuses
    println(items) //6 items

    val items2 = ::ToDoItem `!` names `*` dates `*` statuses
    println(items2) //6 items

    val items3 = lift3(::ToDoItem)(names)(dates)(statuses)
    println(items3) //6 items

    val items4 = Triple(names, dates, statuses).map3(::ToDoItem)
    println(items4) //6 items

    val successItem =
        ::ToDoItem `!` "abc".asSuccess() `*` LocalDate.now().asSuccess() `*` ToDoStatus.Todo.asSuccess()
    println(successItem)

    val failedItem =
        ::ToDoItem `!` "abc".asSuccess() `*` ZettaiValidationError("No date").asFailure() `*` ZettaiValidationError(
            "No status"
        ).asFailure()
    println(failedItem)


}
