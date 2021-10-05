package com.ubertob.unlearnoop.exercises.chapter3.e01_recursion

fun Int.collatz() = collatzR(listOf(), this)

tailrec fun collatzR(acc: List<Int>, x: Int): List<Int> = when {
    x == 1 -> TODO()
    x % 2 == 0 -> TODO()
    else -> TODO()
}