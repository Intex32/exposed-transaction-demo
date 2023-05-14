import io.kotest.common.runBlocking
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.spec.style.Test
import io.kotest.matchers.equals.shouldBeEqual
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync

/**
 * General knowledge:
 * - [suspendedTransactionAsync] is always executed in new transaction
 * to prevent concurrency issues when queries execution order could be changed by CoroutineDispatcher
 * - apparently, [suspendedTransaction] does not create a savepoint; therefore nested txns are useless
 *
 * @author Ron S
 */
class ExposedSuspendTxnTests : AnnotationSpec() {

    @Test
    suspend fun `simple nested txn with inner rollback`() = env(nestedTxns = false) { db ->
        runBlocking {
            newSuspendedTransaction {
                insert("insert")
                suspendedTransaction {
                    insert("nested insert")
                    rollback()
                }
            }
        }

        readAll() shouldBeEqual listOf()
    }

    @Test
    suspend fun `nested inner commit and rollback`() = env(nestedTxns = false) { db ->
        runBlocking {
            newSuspendedTransaction {
                insert("insert")
                suspendedTransaction {
                    insert("nested insert")
                    commit()
                    insert("after commit in nested txn")
                    rollback()
                }
            }
        }

        readAll() shouldBeEqual listOf(
            "insert",
            "nested insert",
        )
    }

}