package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.domain.tooling.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class ToDoListTest {

    val validCharset = uppercase + lowercase + digits + "-"
    val invalidCharset = " !@#$%^&*()_+={}[]|:;'<>,./?\u2202\u2203\u2204\u2205"

    @Test
    fun `Valid names are alphanum+hiphen between 3 and 40 chars length`() {

        stringsGenerator(validCharset, 3, 40)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it).expectSuccess()).isEqualTo(ListName.fromTrusted(it))
            }
    }

    @Test
    fun `Name cannot be too short`() {
        stringsGenerator(validCharset, 0, 2)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it).expectFailure().msg).contains("is too short")
            }
    }

    @Test
    fun `Names longer than 40 chars are not valid`() {

        stringsGenerator(validCharset, 41, 200)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it).expectFailure().msg).contains("is too long")
            }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {

        stringsGenerator(validCharset, 3, 40)
            .map { substituteRandomChar(invalidCharset, it) }
            .take(1000).forEach {
                expectThat(ListName.fromUntrusted(it).expectFailure().msg).contains("contains illegal characters")
            }
    }

    fun `multiple failures are reported`() {

        expectThat(ListName.fromUntrusted("/").expectFailure().msg).contains("contains illegal characters")
            .contains("is too short")
    }
}
