package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.fp.FetchStoredEvents
import com.ubertob.unlearnoop.zettai.fp.ProjectionQuery
import com.ubertob.unlearnoop.zettai.fp.QueryRunner


class ToDoListQueryRunner(eventFetcher: FetchStoredEvents<ToDoListEvent>) : QueryRunner<ToDoListQueryRunner> {
    internal val listProjection = ToDoListProjection(eventFetcher)
    internal val itemProjection = ToDoItemProjection(eventFetcher)

    override fun <R> invoke(f: ToDoListQueryRunner.() -> R): ProjectionQuery<R> =
        ProjectionQuery(setOf(listProjection, itemProjection)) { f(this) }
}




