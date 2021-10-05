package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.domain.ToDoItem
import com.ubertob.unlearnoop.zettai.domain.ToDoStatus
import com.ubertob.unlearnoop.zettai.events.*
import com.ubertob.unlearnoop.zettai.fp.*

data class ItemProjectionRow(val item: ToDoItem, val listId: EntityId)

class ToDoItemProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ItemProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        eventFetcher,
        ::eventProjector
    ) {

    fun findWhatsNext(maxRows: Int, lists: List<EntityId>): List<ItemProjectionRow> =
        allRows().values
            .filter { it.listId in lists }
            .filter { it.item.dueDate != null && it.item.status == ToDoStatus.Todo }
            .sortedBy { it.item.dueDate }
            .take(maxRows)

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ItemProjectionRow>> =
            when (e) {
                is ListCreated -> emptyList()
                is ItemAdded -> CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id)).toSingle()
                is ItemRemoved -> DeleteRow<ItemProjectionRow>(e.itemRowId(e.item)).toSingle()
                is ItemModified -> listOf(
                    CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id)),
                    DeleteRow(e.itemRowId(e.prevItem))
                )
                is ListPutOnHold -> emptyList()
                is ListReleased -> emptyList()
                is ListClosed -> emptyList()
            }
    }
}

private fun ToDoListEvent.itemRowId(item: ToDoItem): RowId = RowId("${id}_${item.description}")
