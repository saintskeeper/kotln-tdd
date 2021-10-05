package com.ubertob.unlearnoop.zettai.fp

import java.util.*


data class EntityId(val raw: UUID) {
    companion object {
        fun mint() = EntityId(UUID.randomUUID())
    }
}


interface EntityEvent {
    val id: EntityId
}

interface EntityState<in E : EntityEvent> {
    fun combine(event: E): EntityState<E>
}

interface EntityRetriever<out S : EntityState<E>, in E : EntityEvent> {
    fun retrieveById(id: EntityId): S?
}

typealias EventStreamer<E> = (EntityId) -> List<E>?
typealias EventPersister<E> = (Iterable<E>) -> List<E>