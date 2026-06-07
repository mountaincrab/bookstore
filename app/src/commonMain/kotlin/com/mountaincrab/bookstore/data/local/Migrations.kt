package com.mountaincrab.bookstore.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Room migration registry.
 *
 * When bumping `@Database(version = N)`:
 *   1. Make the entity change and increment the version.
 *   2. `./gradlew :app:compileDebugKotlinAndroid` — emits app/schemas/.../N.json.
 *   3. Diff old vs new JSON, add a `Migration(old, new) { ... }` here.
 *   4. Add a migration test (see CLAUDE.md → Room migrations). A migration
 *      without a test is not done. Bumping the version with no migration will
 *      crash the app on upgrade — that is the intended safety net.
 */

/**
 * v1 → v2: drop the `read` and `source` columns. A book record now always means
 * a book you've read, so there is no read/unread status and no "where from".
 * Existing rows are kept (previously-unread books become read); `readAt` is
 * backfilled from `createdAt` where it was null so "Recent" still sorts sensibly.
 *
 * SQLite can't drop columns on older API levels, so the table is recreated to
 * match the generated v2 schema exactly.
 */
val MIGRATION_1_2 = Migration(1, 2) { connection ->
    connection.execSQL(
        "CREATE TABLE IF NOT EXISTS `books_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
            "`author` TEXT NOT NULL, `genres` TEXT NOT NULL, `notes` TEXT NOT NULL, " +
            "`readAt` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
            "`syncStatus` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))"
    )
    connection.execSQL(
        "INSERT INTO `books_new` (`id`, `title`, `author`, `genres`, `notes`, `readAt`, " +
            "`createdAt`, `updatedAt`, `syncStatus`, `isDeleted`) " +
            "SELECT `id`, `title`, `author`, `genres`, `notes`, COALESCE(`readAt`, `createdAt`), " +
            "`createdAt`, `updatedAt`, `syncStatus`, `isDeleted` FROM `books`"
    )
    connection.execSQL("DROP TABLE `books`")
    connection.execSQL("ALTER TABLE `books_new` RENAME TO `books`")
}

/**
 * v2 → v3: add the nullable [isbn] column (normalised ISBN-13) and a unique index on
 * it. SQLite treats each NULL as distinct, so multiple rows can have isbn = NULL while
 * the index still blocks two rows with the same non-null ISBN.
 */
val MIGRATION_2_3 = Migration(2, 3) { connection ->
    connection.execSQL("ALTER TABLE `books` ADD COLUMN `isbn` TEXT DEFAULT NULL")
    connection.execSQL(
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_books_isbn` ON `books` (`isbn`)"
    )
}

val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
