package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.domain.ListName
import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoList
import com.ubertob.unlearnoop.zettai.domain.User
import com.ubertob.unlearnoop.zettai.events.*
import com.ubertob.unlearnoop.zettai.eventsourcing.*
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError

data class ToDoListProjectionRow(val id: ToDoListId, val user: User, val active: Boolean, val list: ToDoList) {
    fun addItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items + item))

    fun removeItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - item))

    fun replaceItem(prevItem: ToDoItem, item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - prevItem + item))

    fun putOnHold(): ToDoListProjectionRow = copy(active = false)
    fun release(): ToDoListProjectionRow = copy(active = true)
    fun rename(newName: ListName): ToDoListProjectionRow = copy(list = list.copy(listName = newName))
}

interface ToDoListProjection {

    fun findAll(user: User): Outcome<OutcomeError, List<ListName>>
    fun findList(user: User, name: ListName): Outcome<OutcomeError, ToDoList?>
    fun findAllActiveListId(user: User): Outcome<OutcomeError, List<EntityId>>

    fun update()

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ToDoListProjectionRow>> =
            when (e) {
                is ListCreated -> CreateRow(
                    e.rowId(), ToDoListProjectionRow(e.id, e.owner, true, ToDoList(e.name, emptyList()))
                )
                is ItemAdded -> UpdateRow(e.rowId()) { addItem(e.item) }
                is ItemRemoved -> UpdateRow(e.rowId()) { removeItem(e.item) }
                is ItemModified -> UpdateRow(e.rowId()) { replaceItem(e.prevItem, e.item) }
                is ListPutOnHold -> UpdateRow(e.rowId()) { putOnHold() }
                is ListReleased -> UpdateRow(e.rowId()) { release() }
                is ListClosed -> DeleteRow(e.rowId())
                is ListRenamed -> UpdateRow(e.rowId()) { rename(e.newName) }
            }.toSingle()
    }
}


private fun ToDoListEvent.rowId(): RowId = RowId(id.raw.toString())

