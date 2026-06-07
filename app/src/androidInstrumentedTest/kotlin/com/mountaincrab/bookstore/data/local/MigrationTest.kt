package com.mountaincrab.bookstore.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Validates [MIGRATION_1_2]: dropping the `read` and `source` columns must keep
 * existing rows (previously-unread books survive as read) and backfill `readAt`
 * from `createdAt` where it was null.
 */
class MigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath("migration-test"),
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class,
    )

    @Test
    fun migrate1To2_keepsUnreadBookAndBackfillsReadAt() {
        // Create the v1 schema and insert an "unread" book (read = 0, readAt null).
        val v1 = helper.createDatabase(1)
        v1.execSQL(
            "INSERT INTO books " +
                "(id, title, author, genres, read, source, notes, readAt, createdAt, updatedAt, syncStatus, isDeleted) " +
                "VALUES ('b1', 'Psychovertical', 'Andy Kirkpatrick', '[]', 0, 'BOUGHT', '', NULL, 1000, 1000, 'PENDING', 0)"
        )
        v1.close()

        // Migrate and validate against the exported v2 schema.
        val v2 = helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2))
        val stmt = v2.prepare("SELECT title, readAt FROM books WHERE id = 'b1'")
        assertTrue("migrated row should survive", stmt.step())
        assertEquals("Psychovertical", stmt.getText(0))
        // readAt was null at v1; the migration backfills it from createdAt.
        assertEquals(1000L, stmt.getLong(1))
        stmt.close()
        v2.close()
    }
}
