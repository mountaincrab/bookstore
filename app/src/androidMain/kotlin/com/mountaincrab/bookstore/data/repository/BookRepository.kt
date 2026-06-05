package com.mountaincrab.bookstore.data.repository

import com.mountaincrab.bookstore.data.local.dao.BookDao
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.model.BookSource
import com.mountaincrab.bookstore.data.model.SyncStatus
import com.mountaincrab.bookstore.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow

/**
 * Local-only book store backed by Room. Every mutation marks the row
 * `syncStatus = PENDING` so it is ready to push the moment Firebase is switched
 * on, but no sync work is enqueued today (the Firebase layer is unwired — see
 * CLAUDE.md). When wiring Firebase in, call `enqueueSyncWork()` from each
 * mutation, exactly like crab-do's repositories.
 */
class BookRepository(
    private val bookDao: BookDao,
) {
    fun observeReadBooks(): Flow<List<BookEntity>> = bookDao.observeReadBooks()

    fun observeAllBooks(): Flow<List<BookEntity>> = bookDao.observeAllBooks()

    suspend fun getById(id: String): BookEntity? = bookDao.getById(id)

    suspend fun addBook(
        title: String,
        author: String,
        genres: List<String>,
        read: Boolean,
        source: BookSource,
        notes: String,
    ): BookEntity {
        val now = currentTimeMillis()
        val book = BookEntity(
            title = title.trim(),
            author = author.trim().ifEmpty { "Unknown" },
            genres = genres.map { it.trim() }.filter { it.isNotEmpty() },
            read = read,
            source = source,
            notes = notes.trim(),
            readAt = if (read) now else null,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        bookDao.upsert(book)
        return book
    }

    suspend fun updateBook(book: BookEntity) {
        bookDao.upsert(
            book.copy(updatedAt = currentTimeMillis(), syncStatus = SyncStatus.PENDING)
        )
    }

    suspend fun setRead(id: String, read: Boolean) {
        bookDao.setRead(id, read, readAt = if (read) currentTimeMillis() else null)
    }

    suspend fun deleteBook(id: String) {
        bookDao.softDelete(id)
    }
}
