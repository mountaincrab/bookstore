package com.mountaincrab.bookstore.data.repository

import com.mountaincrab.bookstore.data.local.dao.BookDao
import com.mountaincrab.bookstore.data.local.entity.BookEntity
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
    fun observeBooks(): Flow<List<BookEntity>> = bookDao.observeBooks()

    suspend fun getById(id: String): BookEntity? = bookDao.getById(id)

    suspend fun addBook(
        title: String,
        author: String,
        genres: List<String>,
        notes: String,
    ): BookEntity {
        val effectiveAuthor = author.trim().ifEmpty { "Unknown" }
        val existing = bookDao.findByTitleAndAuthor(title.trim(), effectiveAuthor)
        if (existing != null) return existing

        val now = currentTimeMillis()
        val book = BookEntity(
            title = title.trim(),
            author = effectiveAuthor,
            genres = genres.map { it.trim() }.filter { it.isNotEmpty() },
            notes = notes.trim(),
            readAt = now,
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

    suspend fun deleteBook(id: String) {
        bookDao.softDelete(id)
    }
}
