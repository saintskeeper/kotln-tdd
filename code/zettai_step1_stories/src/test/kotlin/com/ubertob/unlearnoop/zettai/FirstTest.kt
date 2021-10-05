package com.ubertob.unlearnoop.zettai

import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.math.sqrt

class FirstTest {

    @Test
    fun onePlusTwoIsThree() {
        expectThat(1 + 2).isEqualTo(3)
    }


    fun pythagoras(a: Double, b: Double): Double = sqrt(a * a + b * b)

    @Test
    fun perfectTriangles() {
        expectThat(pythagoras(3.0, 4.0)).isEqualTo(5.0)
        expectThat(pythagoras(5.0, 12.0)).isEqualTo(13.0)
        expectThat(pythagoras(88.0, 105.0)).isEqualTo(137.0)
    }


    @Test
    fun `better way to test perfectTriangles`() {
        val data = listOf(
            Triple(3, 4, 5),
            Triple(5, 12, 13),
            Triple(88, 105, 137),
            Triple(119, 120, 169),
            Triple(52, 165, 173),
            Triple(19, 180, 181)
        )

        val functionUnderTest = ::pythagoras

        expect {
            data
                .map { Triple(it.first.toDouble(), it.second.toDouble(), it.third.toDouble()) }
                .map { (a, b, c) ->
                    that(functionUnderTest(a, b)).isEqualTo(c)
                }
        }
    }


}