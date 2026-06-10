package com.mountaincrab.bookstore.data.backup

import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.model.BookSource
import com.mountaincrab.bookstore.data.model.SyncStatus
import com.mountaincrab.bookstore.util.currentTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * On-disk backup format for the local library. A dedicated DTO (rather than
 * serialising [BookEntity] directly) decouples the file format from the Room
 * schema, so future entity changes don't silently break older backup files.
 *
 * Sync/tombstone fields (`syncStatus`, `isDeleted`) are intentionally omitted:
 * cloud sync is unwired, so a restore always brings books back as live and
 * `PENDING`.
 */
@Serializable
data class BackupFile(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportedAt: Long = currentTimeMillis(),
    val books: List<BackupBook> = emptyList(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

@Serializable
data class BackupBook(
    val id: String,
    val title: String,
    val author: String,
    val genres: List<String> = emptyList(),
    val read: Boolean = false,
    val source: String = BookSource.BOUGHT.name,
    val notes: String = "",
    val readAt: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/** Shared JSON config. `ignoreUnknownKeys` keeps older files readable if the format grows. */
val BackupJson: Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

fun BookEntity.toBackup(): BackupBook = BackupBook(
    id = id,
    title = title,
    author = author,
    genres = genres,
    read = read,
    source = source.name,
    notes = notes,
    readAt = readAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/**
 * Rehydrate a backed-up book into a live entity. The original [id] is preserved
 * so restoring is an upsert-by-primary-key merge (existing books are refreshed,
 * not duplicated). Restored rows are always live and `PENDING`.
 */
fun BackupBook.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    genres = genres,
    read = read,
    source = BookSource.fromName(source),
    notes = notes,
    readAt = readAt,
    createdAt = if (createdAt > 0L) createdAt else currentTimeMillis(),
    updatedAt = if (updatedAt > 0L) updatedAt else currentTimeMillis(),
    syncStatus = SyncStatus.PENDING,
    isDeleted = false,
)
