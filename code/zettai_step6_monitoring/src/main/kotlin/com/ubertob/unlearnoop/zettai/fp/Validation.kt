package com.ubertob.unlearnoop.zettai.fp


interface ValidationError : OutcomeError {

    val errors: List<String>

    fun combineErrors(other: ValidationError): ValidationError

}

typealias Validation<T> = Outcome<ValidationError, T>


fun <T : Any, VE : ValidationError> List<Outcome<VE, T>>.combineFailures(): Outcome<VE, T> =
    reduce { acc, r ->
        when (r) {
            is Success<*> -> acc
            is Failure<VE> -> when (acc) {
                is Success<*> -> r
                is Failure<VE> -> acc.error.combineErrors(r.error).asFailure() as Outcome<VE, T>
            }
        }
    }
