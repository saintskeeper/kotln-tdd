package com.ubertob.unlearnoop.zettai.fp

sealed class Outcome<out E : OutcomeError, out T> {

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> f(this.value).asSuccess()
            is Failure -> this
        }

    fun <F : OutcomeError> transformFailure(f: (E) -> F): Outcome<F, T> =
        when (this) {
            is Success -> this
            is Failure -> f(this.error).asFailure()
        }

    companion object {
        fun <T, U, E : OutcomeError> lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> =
            { o -> o.transform { f(it) } }

        fun <T> tryOrFail(block: () -> T): Outcome<ThrowableError, T> =
            try {
                block().asSuccess()
            } catch (t: Throwable) {
                ThrowableError(t).asFailure()
            }

    }

}

data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()
data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()


inline fun <T, E : OutcomeError> Outcome<E, T>.recover(recoverError: (E) -> T): T =
    when (this) {
        is Success -> value
        is Failure -> recoverError(error)
    }

inline fun <T, U, E : OutcomeError> Outcome<E, T>.bind(f: (T) -> Outcome<E, U>): Outcome<E, U> =
    when (this) {
        is Success -> f(value)
        is Failure -> this
    }

inline fun <T, E : OutcomeError, F : OutcomeError> Outcome<E, T>.bindFailure(f: (E) -> Outcome<F, T>): Outcome<F, T> =
    when (this) {
        is Success -> this
        is Failure -> f(error)
    }

fun <T, U, E : OutcomeError> Outcome<E, T>.combine(other: Outcome<E, U>): Outcome<E, Pair<T, U>> =
    bind { first -> other.transform { second -> first to second } }


fun <T, E : OutcomeError> Outcome<E, Outcome<E, T>>.join(): Outcome<E, T> =
    bind { it }

fun <T, E : OutcomeError> Outcome<E, T>.failIf(predicate: (T) -> Boolean, error: (T) -> E): Outcome<E, T> =
    when (this) {
        is Success -> if (predicate(value).not()) this else error(value).asFailure()
        is Failure -> this
    }

fun <T : Any, E : OutcomeError> Outcome<E, T?>.failIfNull(error: E): Outcome<E, T> =
    when (this) {
        is Success -> if (value != null) value.asSuccess() else error.asFailure()
        is Failure -> this
    }

inline fun <T, E : OutcomeError> Outcome<E, T>.onFailure(exitBlock: (E) -> Nothing): T =
    when (this) {
        is Success<T> -> value
        is Failure<E> -> exitBlock(error)
    }


interface OutcomeError {
    val msg: String
}


fun <E : OutcomeError> E.asFailure(): Outcome<E, Nothing> = Failure(this)
fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)


fun <T : Any, E : OutcomeError> T?.failIfNull(error: E): Outcome<E, T> = this?.asSuccess() ?: error.asFailure()

data class ThrowableError(val t: Throwable) : OutcomeError {
    override val msg: String
        get() = t.message.orEmpty()
}

fun <T, ERR : OutcomeError, U> Iterable<T>.foldOutcome(
    initial: U,
    operation: (acc: U, T) -> Outcome<ERR, U>
): Outcome<ERR, U> =
    fold(initial.asSuccess() as Outcome<ERR, U>) { acc, el -> acc.bind { operation(it, el) } }


fun <E : OutcomeError, T> Iterable<Outcome<E, T>>.extractList(): Outcome<E, List<T>> =
    fold(emptyList<T>().asSuccess()) { acc: Outcome<E, Iterable<T>>, e: Outcome<E, T> ->
        acc.bind { list -> e.transform { list + it } }
    }



