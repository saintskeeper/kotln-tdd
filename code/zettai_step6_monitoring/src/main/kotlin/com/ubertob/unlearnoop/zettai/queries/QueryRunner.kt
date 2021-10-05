package com.ubertob.unlearnoop.zettai.queries

import com.ubertob.unlearnoop.zettai.domain.QueryError
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.OutcomeError

interface QueryRunner<Self : QueryRunner<Self>> {
    operator fun <R> invoke(f: Self.() -> Outcome<OutcomeError, R>): Outcome<QueryError, R>
}