package com.ubertob.unlearnoop.exercises.chapter3.e04_monoid

data class Monoid<T: Any>(val zero: T, val combine: (T, T) -> T) {

    fun Iterable<T>.fold(): T = TODO()
}