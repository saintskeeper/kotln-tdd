package com.ubertob.unlearnoop.zettai.db.jdbc

import com.ubertob.unlearnoop.zettai.db.fp.ContextReader
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError

interface ContextProvider<CTX> {
    fun <T> tryRun(reader: ContextReader<CTX, T>): Outcome<ContextError, T>

    fun <T> doRun(block: ContextWrapper<CTX>.() -> T): Outcome<ContextError, T> =
        tryRun(ContextReader { ctx -> block(ContextWrapper(ctx)) })

    fun <A, T> runWith(readerBuilder: (A) -> ContextReader<CTX, T>): (A) -> Outcome<ContextError, T> =
        { input -> tryRun(readerBuilder(input)) }
}


data class ContextWrapper<CTX>(val context: CTX) {
    operator fun <T> ContextReader<CTX, T>.unaryPlus(): T = runWith(context)
}

interface ContextError : OutcomeError {
    val exception: Throwable?
}

