package com.ubertob.unlearnoop.zettai.db.eventsourcing

import com.ubertob.unlearnoop.zettai.eventsourcing.EntityId

data class PgEvent(
    val entityId: EntityId,
    val eventType: String,
    val jsonString: String,
    val version: Int,
    val source: String
)



