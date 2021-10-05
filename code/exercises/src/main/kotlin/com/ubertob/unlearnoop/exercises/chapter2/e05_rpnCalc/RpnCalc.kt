package com.ubertob.unlearnoop.exercises.chapter2.e05_rpnCalc

import com.ubertob.unlearnoop.exercises.chapter2.e04_funStack.FunStack
import java.util.*

object RpnCalc {

    val stack = FunStack<Double>()

    fun calc(cmd: String): Double {
        val elements = cmd.split(" ")

        return elements.fold(0.0, RpnCalc::rpn)
    }

    private fun rpn(acc: Double, elem: String): Double = TODO()

    private fun operation(op: String, x: Double, y: Double): Double {
        val res = when (op) {
            "+" -> x + y
            "-" -> x - y
            "*" -> x * y
            "/" -> x / y
            else -> throw Exception( "Unknown operation $op")
        }
        return res
    }


}