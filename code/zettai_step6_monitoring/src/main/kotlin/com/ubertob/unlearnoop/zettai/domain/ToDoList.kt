package com.ubertob.unlearnoop.zettai.domain

import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.combineFailures
import com.ubertob.unlearnoop.zettai.fp.failIfNull
import com.ubertob.unlearnoop.zettai.fp.onFailure
import java.time.LocalDate

val pathElementPattern = Regex(pattern = "[A-Za-z0-9-]+")

data class ListName internal constructor(val name: String) {
    companion object {
        fun fromTrusted(name: String): ListName = ListName(name)
        fun fromUntrustedOrThrow(name: String): ListName =
            fromUntrusted(name).onFailure { error(it.msg) }
        fun fromUntrusted(name: String): ListNameOutcome =
            name.validateWith(shortName, longName, nameCharacters)
    }
}

data class ToDoList(val listName: ListName, val items: List<ToDoItem>) {
    companion object {
        fun build(
            listName: String, items: List<String>
        ): ToDoList = ToDoList(ListName.fromUntrustedOrThrow(listName), items.map() { ToDoItem(it) })
    }
}


data class ToDoItem(
    val description: String,
    val dueDate: LocalDate? = null,
    val status: ToDoStatus = ToDoStatus.Todo
) {
    fun markAsDone(): ToDoItem = copy(status = ToDoStatus.Done)
}

enum class ToDoStatus { Todo, InProgress, Done, Blocked }



typealias ListNameValidation = (String) -> Outcome<ZettaiValidationError, String>

typealias ListNameOutcome = Outcome<ZettaiValidationError, ListName>


private fun String.validateWith(vararg validations: ListNameValidation): ListNameOutcome =
    validations
        .map { it(this) }
        .combineFailures()
        .transform { ListName.fromTrusted(it) }


fun <T> T.discardUnless(predicate: T.() -> Boolean): T? = takeIf { predicate(it) }

val shortName: ListNameValidation = { name ->
    name.discardUnless { length >= 3 }
        .failIfNull(ZettaiValidationError("Name ${name} is too short"))
}

val longName: ListNameValidation = { name ->
    name.discardUnless { length <= 40 }
        .failIfNull(ZettaiValidationError("Name ${name} is too long"))
}

val nameCharacters: ListNameValidation = { name ->
    name.discardUnless { matches(pathElementPattern) }
        .failIfNull(ZettaiValidationError("Name ${name} contains illegal characters ([A-Za-z0-9-]+)"))
}