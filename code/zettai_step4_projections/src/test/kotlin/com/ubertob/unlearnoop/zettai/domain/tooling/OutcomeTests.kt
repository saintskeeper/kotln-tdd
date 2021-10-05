package com.ubertob.unlearnoop.zettai.domain.tooling

import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError
import com.ubertob.unlearnoop.zettai.fp.onFailure
import org.junit.jupiter.api.fail

fun <E : OutcomeError, T> Outcome<E, T>.expectSuccess(): T =
    onFailure { error -> fail { "$this expected success but was $error" } }


fun <E : OutcomeError, T> Outcome<E, T>.expectFailure(): E =
    onFailure { error -> return error }
        .let { fail { "Expected failure but was $it" } }