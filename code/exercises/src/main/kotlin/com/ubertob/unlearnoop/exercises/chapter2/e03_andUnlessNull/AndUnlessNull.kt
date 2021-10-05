package com.ubertob.unlearnoop.exercises.chapter2.e03_andUnlessNull

typealias FUN<A, B> = (A) -> B

infix fun <A: Any, B: Any, C: Any> FUN<A, B?>.andUnlessNull(
    other: FUN<B, C?>): FUN<A, C?> = TODO()

