package com.ubertob.unlearnoop.exercises.chapter4.e04_transformerror


sealed class Outcome<out E : OutcomeError, out T> {

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> f(this.value).asSuccess()
            is Failure -> this
        }

    fun <F : OutcomeError> transformError(f: (E) -> F): Outcome<F, T> = TODO()

}

data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()
data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()

fun <T, U, E : OutcomeError> Outcome<E, T>.lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> =
    { this.transform { f(it) } }


inline fun <T, E : OutcomeError> Outcome<E, T>.recover(fRec: (E) -> T): T =
    when (this) {
        is Success -> value
        is Failure -> fRec(error)
    }


inline fun <T, E : OutcomeError> Outcome<E, T>.onFailure(block: (E) -> Nothing): T =
    when (this) {
        is Success<T> -> value
        is Failure<E> -> block(error)
    }


inline fun <T, E : OutcomeError> Outcome<E, T>.failIf(predicate: (T) -> Boolean, error: E): Outcome<E, T> =
    when (this) {
        is Success<T> -> if (predicate(value)) error.asFailure() else this
        is Failure<E> -> this
    }


interface OutcomeError {
    val msg: String
}


fun <T : OutcomeError> T.asFailure(): Outcome<T, Nothing> = Failure(this)
fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)


fun main() {

    fun readFile(fileName: String): Outcome<FileError, String> = TODO()

    fun sendTextByEmail(text: String): Outcome<EmailError, Unit> = TODO()

    fun sendEmail(fileName: String): Outcome<EmailError, Unit> =
        readFile(fileName)
            .transformError { EmailError(it.msg) }
            .onFailure { return@sendEmail it.asFailure() }
            .let(::sendTextByEmail)
}


class FileError(override val msg: String) : OutcomeError
class EmailError(override val msg: String) : OutcomeError
