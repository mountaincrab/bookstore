package com.mountaincrab.bookstore.data.repository

import android.content.Context
import android.net.Uri
import com.mountaincrab.bookstore.data.backup.BackupFile
import com.mountaincrab.bookstore.data.backup.BackupJson
import com.mountaincrab.bookstore.data.backup.toBackup
import com.mountaincrab.bookstore.data.backup.toEntity
import com.mountaincrab.bookstore.data.local.dao.BookDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Exports/imports the local library as a JSON file via Storage Access Framework
 * URIs (so no storage permission is needed — the system picker grants per-file
 * access). Restore is a non-destructive merge: each book is upserted by its
 * primary-key id, so existing books are refreshed and new ones added, while
 * books absent from the file are left untouched.
 */
class BackupRepository(
    private val bookDao: BookDao,
    private val context: Context,
) {
    /** Write every live book to [uri] as JSON. Returns the number of books written. */
    suspend fun exportTo(uri: Uri): Int = withContext(Dispatchers.IO) {
        val books = bookDao.getAllBooks()
        val payload = BackupFile(books = books.map { it.toBackup() })
        val json = BackupJson.encodeToString(BackupFile.serializer(), payload)
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        } ?: error("Couldn't open the selected file for writing.")
        books.size
    }

    /** Read a backup from [uri] and merge it into the library. Returns the number of books imported. */
    suspend fun importFrom(uri: Uri): Int = withContext(Dispatchers.IO) {
        val json = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: error("Couldn't open the selected file for reading.")

        val payload = try {
            BackupJson.decodeFromString(BackupFile.serializer(), json)
        } catch (e: Exception) {
            error("This doesn't look like a Book Shelf backup file.")
        }

        payload.books.forEach { bookDao.upsert(it.toEntity()) }
        payload.books.size
    }
}
