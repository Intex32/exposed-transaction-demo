import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

private object TestTable : Table("TxnTestTable") {
    val text = text("text")
}

private fun <T> txn(block: Transaction.() -> T): T = transaction(statement = block)

class SomeException : RuntimeException()

private fun provideDataSource() = HikariConfig().apply {
    TODO()
}.let(::HikariDataSource)

fun env(nestedTxns: Boolean, block: (db: Database) -> Unit) =
    env(config = DatabaseConfig {
        useNestedTransactions = nestedTxns
    }, block)

fun env(config: DatabaseConfig, block: (db: Database) -> Unit) {
    provideDataSource().use { dataSource ->
        val db = Database.connect(databaseConfig = config, datasource = dataSource)
        transaction(db) { // reset table
            SchemaUtils.drop(TestTable)
            SchemaUtils.create(TestTable)
        }
        block(db)
    }
}

/**
 * Saves a row with [text] as only column to [TestTable].
 */
fun Transaction.insert(text: String) = TestTable.insert { row ->
    row[this.text] = text
}

/**
 * Reads all rows from [TestTable].
 */
fun readAll(): List<String> = transaction {
    TestTable
        .selectAll()
        .map { row -> row[TestTable.text] }
}
