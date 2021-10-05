package integrationtesting.com.ubertob.unlearnoop.zettai.db

import com.ubertob.unlearnoop.zettai.db.fp.TxReader
import com.ubertob.unlearnoop.zettai.db.jdbc.*
import com.ubertob.unlearnoop.zettai.db.jdbc.TransactionIsolationLevel.ReadCommitted
import com.ubertob.unlearnoop.zettai.domain.*
import com.ubertob.unlearnoop.zettai.domain.tooling.digits
import com.ubertob.unlearnoop.zettai.domain.tooling.expectSuccess
import com.ubertob.unlearnoop.zettai.domain.tooling.randomString
import com.ubertob.unlearnoop.zettai.events.ToDoListId
import com.ubertob.unlearnoop.zettai.eventsourcing.EntityId
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjectionRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

fun transactionContextForTest() =
    TransactionProvider(
        pgDataSourceForTest(),
        ReadCommitted
    )


class TxContextReaderTest {

    fun writeRow(row: ToDoListProjectionRow): TxReader<Unit> = TxReader { tx ->
        toDoListProjectionTable.insertInto(tx) { newRow ->
            newRow[id] = row.id.toRowId()
            newRow[row_data] = row
        }
    }

    fun readRow(id: String): TxReader<ToDoListProjectionRow> = TxReader { tx ->
        toDoListProjectionTable.selectWhere(tx, toDoListProjectionTable.id eq id)
            .map { it[toDoListProjectionTable.row_data] }
            .single()
    }

    @Test
    fun `write and read from a table`() {

        val user = randomUser()
        val expectedList = randomToDoList()
        val listId = ToDoListId.mint()
        val row = ToDoListProjectionRow(listId, user, true, expectedList)

        val listReader: TxReader<ToDoList> =
            writeRow(row)
                .bind { readRow(listId.toRowId()) }
                .transform { r -> r.list }

        val list = transactionContextForTest().tryRun(listReader)
            .expectSuccess()

        expectThat(list).isEqualTo(expectedList)
    }

}


@Test
fun `read json field from a table`() {

    val user = randomUser()

    val insertR1 = TxReader { tx ->
        toDoListProjectionTable.insertInto(tx) { newRow ->
            newRow[id] = randomRowId()
            newRow[row_data] = ToDoListProjectionRow(ToDoListId.mint(), user, true, randomToDoList())
        }
    }
    val insertR2 = TxReader { tx ->
        toDoListProjectionTable.insertInto(tx) { newRow ->
            newRow[id] = randomRowId()
            newRow[row_data] = ToDoListProjectionRow(ToDoListId.mint(), user, true, randomToDoList())
        }
    }
    val expectedlist = randomToDoList()
    val insertR3 = TxReader { tx ->
        toDoListProjectionTable.insertInto(tx) { newRow ->
            newRow[id] = randomRowId()
            newRow[row_data] = ToDoListProjectionRow(ToDoListId.mint(), user, true, randomToDoList())
        }
    }

    fun readRow(name: ListName) =
        TxReader { tx -> readRowByListName(tx, name)!! }
            .transform(ToDoListProjectionRow::list)


    val list = transactionContextForTest().tryRun(
        insertR1
            .bind { insertR2 }
            .bind { insertR3 }
            .bind { readRow(expectedlist.listName) }
    ).expectSuccess()

    expectThat(list).isEqualTo(expectedlist)


}

private fun randomRowId() = randomString(digits, 6, 7)


private fun readRowByListName(tx: Transaction, name: ListName): ToDoListProjectionRow? =
    queryBySql(
        tx,
        toDoListProjectionTable.fields,
        """ select * from ${toDoListProjectionTable.tableName} where "row_data"->'list'->>'listName' = '${name.name}'"""
    )
        .map { it[toDoListProjectionTable.row_data] }
        .singleOrNull()


private fun EntityId.toRowId() = raw.toString()