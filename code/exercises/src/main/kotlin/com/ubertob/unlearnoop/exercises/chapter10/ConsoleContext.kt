package com.ubertob.unlearnoop.exercises.chapter10

import java.io.BufferedReader
import java.io.InputStreamReader

class ConsoleContext {
    fun println(msg: String) {
        println(msg)
    }

    val reader = BufferedReader(InputStreamReader(System.`in`))


    fun readln(): String = reader.readLine()
}

fun main() {

}