package com.ubertob.unlearnoop.zettai.eventsourcing

import com.ubertob.unlearnoop.zettai.db.fp.ContextReader
import com.ubertob.unlearnoop.zettai.db.fp.combine
import com.ubertob.unlearnoop.zettai.db.jdbc.ContextProvider
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError
import com.ubertob.unlearnoop.zettai.fp.recover


typealias FetchStoredEvents<E> = (EventSeq) -> Outcome<OutcomeError, List<StoredEvent<E>>>
typealias ProjectEvents<R, E> = (E) -> List<DeltaRow<R>>

interface Projection<CTX, R : Any, E : EntityEvent> {

    val contextProvider: ContextProvider<CTX>

    val eventProjector: ProjectEvents<R, E>

    val eventFetcher: FetchStoredEvents<E>

    fun update() {
        contextProvider.doRun {
            eventFetcher(+lastProjectedEvent())
                .transform {
                    it.onEach { storedEvent ->
                        +applyDelta(eventProjector(storedEvent.event))
                    }.lastOrNull()?.apply {
                        +updateLastProjectedEvent(eventSeq)
                    }
                }
        }.recover {
            println("Error during update! $it")
        }
    }

    private fun applyDelta(deltas: List<DeltaRow<R>>): ContextReader<CTX, Unit> =
        deltas.fold(ContextReader {}) { reader, delta ->
            reader combine when (delta) {
                is CreateRow -> saveRow(delta.rowId, delta.row)
                is DeleteRow -> deleteRow(delta.rowId)
                is UpdateRow -> updateRow(delta.rowId) { oldRow -> delta.updateRow(oldRow) }
            }
        }


    fun readRow(rowId: RowId): ContextReader<CTX, R?>

    fun saveRow(rowId: RowId, row: R): ContextReader<CTX, Unit>

    fun deleteRow(rowId: RowId): ContextReader<CTX, Unit>

    fun updateRow(rowId: RowId, updateFn: (R) -> R): ContextReader<CTX, Unit>

    fun lastProjectedEvent(): ContextReader<CTX, EventSeq>

    fun updateLastProjectedEvent(eventSeq: EventSeq): ContextReader<CTX, Unit>

}


data class RowId(val id: String)

sealed class DeltaRow<R : Any>

data class CreateRow<R : Any>(val rowId: RowId, val row: R) : DeltaRow<R>()
data class DeleteRow<R : Any>(val rowId: RowId) : DeltaRow<R>()
data class UpdateRow<R : Any>(val rowId: RowId, val updateRow: R.() -> R) : DeltaRow<R>()

fun <T : Any> DeltaRow<T>.toSingle(): List<DeltaRow<T>> = listOf(this)