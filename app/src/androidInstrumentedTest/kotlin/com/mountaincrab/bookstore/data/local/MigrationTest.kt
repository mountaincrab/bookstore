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

    @Test
    fun migrate2To3_addsIsbnColumnAndUniqueIndex() {
        // Create the v2 schema with two books.
        val v2 = helper.createDatabase(2)
        v2.execSQL(
            "INSERT INTO books (id, title, author, genres, notes, readAt, createdAt, updatedAt, syncStatus, isDeleted) " +
                "VALUES ('b1', 'Into Thin Air', 'Jon Krakauer', '[]', '', 1000, 1000, 1000, 'PENDING', 0)"
        )
        v2.execSQL(
            "INSERT INTO books (id, title, author, genres, notes, readAt, createdAt, updatedAt, syncStatus, isDeleted) " +
                "VALUES ('b2', 'Climbing Beyond', 'Pearson, James', '[]', '', 2000, 2000, 2000, 'PENDING', 0)"
        )
        v2.close()

        // Migrate to v3 and validate against the AppDatabase v3 annotations.
        val v3 = helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3))

        // Both rows survive with isbn = NULL.
        val stmt = v3.prepare("SELECT id, isbn FROM books ORDER BY createdAt")
        assertTrue(stmt.step())
        assertEquals("b1", stmt.getText(0))
        assertTrue("isbn should be null for migrated row", stmt.isNull(1))
        assertTrue(stmt.step())
        assertEquals("b2", stmt.getText(0))
        assertTrue("isbn should be null for migrated row", stmt.isNull(1))
        stmt.close()

        // The unique index allows two NULLs (already proven by the two rows above)
        // but must reject a duplicate non-null ISBN.
        v3.execSQL(
            "INSERT INTO books (id, title, author, genres, notes, readAt, createdAt, updatedAt, syncStatus, isDeleted, isbn) " +
                "VALUES ('b3', 'Eiger Dreams', 'Jon Krakauer', '[]', '', 3000, 3000, 3000, 'PENDING', 0, '9780385494786')"
        )
        try {
            v3.execSQL(
                "INSERT INTO books (id, title, author, genres, notes, readAt, createdAt, updatedAt, syncStatus, isDeleted, isbn) " +
                    "VALUES ('b4', 'Duplicate ISBN', 'Someone', '[]', '', 4000, 4000, 4000, 'PENDING', 0, '9780385494786')"
            )
            assertTrue("duplicate non-null ISBN should have thrown", false)
        } catch (_: Exception) {
            // expected — unique constraint violation
        }

        v3.close()
    }
}
