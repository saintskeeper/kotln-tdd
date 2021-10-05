package com.ubertob.unlearnoop.zettai.db.fp

import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError
import org.jetbrains.exposed.sql.Transaction


data class ContextReader<CTX, out T>(val runWith: (CTX) -> T) {

    fun <U> transform(f: (T) -> U): ContextReader<CTX, U> = ContextReader { ctx -> f(runWith(ctx)) }

    fun <U> bind(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U> =
        ContextReader { ctx -> f(runWith(ctx)).runWith(ctx) }

    fun <U, V> fish(f: (T, U) -> V, other: ContextReader<CTX, U>): ContextReader<CTX, V> =
        ContextReader { ctx -> f(runWith(ctx), other.runWith(ctx)) }

}

infix fun <CTX, T> ContextReader<CTX, T>.combine(other: ContextReader<CTX, T>): ContextReader<CTX, T> =
    bind { other }

fun <CTX, T> ContextReader<CTX, ContextReader<CTX, T>>.join(): ContextReader<CTX, T> =
    bind { it }


fun <T> id(x: T): T = x

fun <CTX> createContext(): ContextReader<CTX, CTX> = ContextReader(::id)

fun <CTX, T : Any, U> ContextReader<CTX, T?>.bindIfNotNull(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U?> =
    ContextReader { t -> runWith(t)?.let { f(it).runWith(t) } }

fun <CTX, E : OutcomeError, T, U> ContextReader<CTX, Outcome<E, T>>.bindIfSuccess(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, Outcome<E, U>> =
    ContextReader { t ->
        runWith(t).transform { f(it).runWith(t) }
    }


typealias TxReader<T> = ContextReader<Transaction, T>
