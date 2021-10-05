package com.ubertob.unlearnoop.zettai.db.jdbc

import com.ubertob.unlearnoop.zettai.db.fp.ContextReader
import com.ubertob.unlearnoop.zettai.fp.Outcome
import com.ubertob.unlearnoop.zettai.fp.asFailure
import com.ubertob.unlearnoop.zettai.fp.asSuccess
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.statements.jdbc.iterate
import org.jetbrains.exposed.sql.transactions.inTopLevelTransaction
import java.sql.ResultSet
import javax.sql.DataSource

data class TransactionError(override val msg: String, override val exception: Throwable?) : ContextError

data class TransactionProvider(
    private val dataSource: DataSource,
    val isolationLevel: TransactionIsolationLevel,
    val maxAttempts: Int = 10
) : ContextProvider<Transaction> {

    override fun <T> tryRun(reader: ContextReader<Transaction, T>): Outcome<ContextError, T> =
        inTopLevelTransaction(
            db = Database.connect(dataSource),
            transactionIsolation = isolationLevel.jdbcLevel,
            repetitionAttempts = maxAttempts
        ) {
            addLogger(StdOutSqlLogger)

            try {
                reader.runWith(this).asSuccess()
            } catch (t: Throwable) {
                rollback()
                TransactionError("Transaction rolled back because ${t.message}", t).asFailure()
            }
        }
}


fun Table.selectWhere(
    tx: Transaction,
    condition: Op<Boolean>?,
    orderByCond: Column<*>? = null
): List<ResultRow> =
    tx.exec(
        Query(this, condition).apply {
            orderByCond?.let { orderBy(it) }
        }
    )?.iterate { ResultRow.create(this, this@selectWhere.realFields) }
        ?: emptyList()


fun queryBySql(tx: Transaction, fields: List<Expression<*>>, sql: String): List<ResultRow> =
    tx.exec(SqlQuery(sql))
        ?.iterate { ResultRow.create(this, fields) }
        ?: emptyList()


fun <T, Self : Table> Self.insertIntoWithReturn(
    tx: Transaction,
    postExecution: InsertStatement<Number>.() -> T,
    block: Self.(InsertStatement<Number>) -> Unit
): T =
    InsertStatement<Number>(this).apply {
        block(this)
        execute(tx)
    }.let { postExecution(it) }


fun <Self : Table> Self.insertInto(
    tx: Transaction,
    block: Self.(InsertStatement<Number>) -> Unit
) {
    insertIntoWithReturn(tx, {}, block)
}

fun Table.updateWhere(
    tx: Transaction,
    where: Op<Boolean>? = null,
    block: Table.(UpdateStatement) -> Unit
) {
    UpdateStatement(targetsSet = this, limit = null, where = where).apply {
        block(this)
        execute(tx)
    }
}


data class SqlQuery(val sql: String) :
    Statement<ResultSet>(StatementType.SELECT, emptyList()) {
    override fun prepareSQL(transaction: Transaction): String = sql

    override fun PreparedStatementApi.executeInternal(transaction: Transaction): ResultSet {
        val fetchSize = transaction.db.defaultFetchSize
        if (fetchSize != null) {
            this.fetchSize = fetchSize
        }
        return executeQuery()
    }

    override fun arguments() = emptyList<List<Pair<IColumnType, Any?>>>()
}