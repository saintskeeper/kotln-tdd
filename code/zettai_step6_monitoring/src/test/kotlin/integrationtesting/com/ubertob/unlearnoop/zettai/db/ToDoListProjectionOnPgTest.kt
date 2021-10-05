package integrationtesting.com.ubertob.unlearnoop.zettai.db

import com.ubertob.unlearnoop.zettai.db.createToDoListEventStreamerOnPg
import com.ubertob.unlearnoop.zettai.db.jdbc.TransactionIsolationLevel
import com.ubertob.unlearnoop.zettai.db.jdbc.TransactionProvider
import com.ubertob.unlearnoop.zettai.domain.pgDataSourceForTest
import com.ubertob.unlearnoop.zettai.domain.tooling.expectSuccess
import com.ubertob.unlearnoop.zettai.events.ToDoListEvent
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjection
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjectionAbstractTest
import com.ubertob.unlearnoop.zettai.queries.ToDoListProjectionOnPg

internal class ToDoListProjectionOnPgTest : ToDoListProjectionAbstractTest() {

    val dataSource = pgDataSourceForTest()
    val txProvider = TransactionProvider(dataSource, TransactionIsolationLevel.ReadCommitted)
    val streamer = createToDoListEventStreamerOnPg()
    val projection = ToDoListProjectionOnPg(txProvider) { txProvider.tryRun(streamer.fetchAfter(it)) }

    override fun buildListProjection(events: List<ToDoListEvent>): ToDoListProjection =
        projection.apply {
            txProvider.tryRun(streamer.store(events)).expectSuccess()
            update()
        }
}