import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.equals.shouldBeEqual
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * General knowledge:
 * - Throwing an exception is identical to rolling back and short-circuiting out of the transaction.
 * - With nested txns disabled, everything is treated as one transaction.
 *
 * @author Ron S
 */
class ExposedTxnTests : AnnotationSpec() {

    @Test
    fun `simple commit and rollback`() = env(nestedTxns = false) { db ->
        transaction(db) {
            insert("before commit") // PERSISTED
            commit()
            insert("in between commit and rollback")
            rollback()
            insert("after rollback") // PERSISTED
        }

        readAll() shouldBeEqual listOf(
            "before commit",
            "after rollback",
        )
    }

    @Test
    private fun `simple nested txn and outer rollback`() = env(nestedTxns = false) { db ->
        transaction(db) {
            insert("start of outer txn")
            transaction(db) {
                insert("nested txn")
            }
            rollback()
        }

        readAll() shouldBeEqual listOf()
    }

    /**
     * with nested txns enabled,
     * an outer rollback also undoes committed changes of the inner txn
     */
    @Test
    private fun `nested txn with commit and outer rollback`() = env(nestedTxns = true) { db ->
        transaction(db) {
            insert("start of outer txn")
            transaction(db) {
                insert("before commit in nested txn")
                commit()
                insert("after commit in nested txn")
            }
            rollback()
        }

        readAll() shouldBeEqual listOf()
    }

    /**
     * for some reason the already committed action inside the nested txn is rolled back
     */
    @Test
    private fun `nested txn with commit and rollback`() = env(nestedTxns = true) { db ->
        transaction(db) {
            insert("start of outer txn") // PERSISTED
            transaction(db) {
                insert("before commit in inner txn")
                commit()
                rollback()
            }
        }

        readAll() shouldBeEqual listOf(
            "start of outer txn",
        )
    }

    /**
     * with nested txn enabled,
     * an inner rollback does not affect (even uncommitted) changes of the outer txn
     */
    @Test
    private fun `simple nested txn and inner rollback with nested txns enabled`() = env(nestedTxns = true) { db ->
        transaction(db) {
            insert("start of outer txn") // PERSISTED
            transaction(db) {
                insert("nested txn")
                rollback()
                // everything after here will again be persisted
            }
        }

        readAll() shouldBeEqual listOf(
            "start of outer txn",
        )
    }

    /**
     * with nested txn disabled,
     * an inner rollback does also affect outer changes
     */
    @Test
    private fun `simple nested txn and inner rollback with nested txns disabled`() = env(nestedTxns = false) { db ->
        transaction(db) {
            insert("start of outer txn")
            transaction(db) {
                insert("nested txn")
                rollback()
            }
        }

        readAll() shouldBeEqual listOf()
    }

}