package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.domain.QueryError
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError
import com.ubertob.unlearnoop.zettai.fp.asFailure

class ToDoListQueryRunner(
    val listProjection: ToDoListProjection,
    val itemProjection: ToDoItemProjection
) : QueryRunner<ToDoListQueryRunner> {

    override fun <R> invoke(f: ToDoListQueryRunner.() -> Outcome<OutcomeError, R>): Outcome<QueryError, R> =
        try {
            listProjection.update()
            itemProjection.update()
            f(this).transformFailure { QueryError(it.msg) }
        } catch (t: Throwable) {
            QueryError("Projection query failed ${t.message}", t).asFailure()
        }
}



