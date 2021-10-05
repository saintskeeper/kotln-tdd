package com.ubertob.unlearnoop.exercises.chapter4.e05_outcometry

import java.time.LocalDate


sealed class Outcome<out E : OutcomeError, out T> {

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> f(this.value).asSuccess()
            is Failure -> this
        }

}

data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()
data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()

fun <T> tryAndCatch(block: () -> T): Outcome<ThrowableError, T> = TODO()


inline fun <T, E : OutcomeError> Outcome<E, T>.recover(fRec: (E) -> T): T =
    when (this) {
        is Success -> value
        is Failure -> fRec(error)
    }


interface OutcomeError {
    val msg: String
}

data class ThrowableError(val t: Throwable) : OutcomeError {
    override val msg: String = t.message.orEmpty()
}

fun <T : OutcomeError> T.asFailure(): Outcome<T, Nothing> = Failure(this)
fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)


fun main() {

    fun todayGreetings(dateString: String) =
        tryAndCatch { LocalDate.parse(dateString) }
            .transform { "Today is $it" }
            .recover { "Error parsing the date ${it.msg}" }


    println(todayGreetings("12/3/2020"))  //Error...

    println(todayGreetings("2020-03-12")) //Today is...

}