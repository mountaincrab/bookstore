package com.mountaincrab.bookstore.data.repository

import com.mountaincrab.bookstore.data.local.dao.BookDao
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.model.SyncStatus
import com.mountaincrab.bookstore.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow

sealed class AddBookResult {
    data class Added(val book: BookEntity) : AddBookResult()
    data class AlreadyRead(val book: BookEntity) : AddBookResult()
}

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

    /**
     * Add a book to the shelf. Returns [AddBookResult.AlreadyRead] (without inserting)
     * if a non-deleted record with the same ISBN (preferred) or the same title+author
     * already exists. Returns [AddBookResult.Added] on a successful insert.
     */
    suspend fun addBook(
        title: String,
        author: String,
        genres: List<String>,
        notes: String,
        isbn: String? = null,
    ): AddBookResult {
        val effectiveAuthor = author.trim().ifEmpty { "Unknown" }

        // ISBN check first — exact, reliable, index-backed.
        if (isbn != null) {
            val byIsbn = bookDao.findByIsbn(isbn)
            if (byIsbn != null) return AddBookResult.AlreadyRead(byIsbn)
        }

        // Fall back to case-insensitive title+author matching for manual entries
        // or results without an ISBN.
        val byTitleAuthor = bookDao.findByTitleAndAuthor(title.trim(), effectiveAuthor)
        if (byTitleAuthor != null) return AddBookResult.AlreadyRead(byTitleAuthor)

        val now = currentTimeMillis()
        val book = BookEntity(
            title = title.trim(),
            author = effectiveAuthor,
            genres = genres.map { it.trim() }.filter { it.isNotEmpty() },
            notes = notes.trim(),
            isbn = isbn,
            readAt = now,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        bookDao.upsert(book)
        return AddBookResult.Added(book)
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
