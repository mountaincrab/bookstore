package com.mountaincrab.bookstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    /** Read books for the Home/Read screen. Newest-read first; UI re-groups. */
    @Query("SELECT * FROM books WHERE read = 1 AND isDeleted = 0 ORDER BY COALESCE(readAt, updatedAt) DESC")
    fun observeReadBooks(): Flow<List<BookEntity>>

    /** Every book, for the Search screen. */
    @Query("SELECT * FROM books WHERE isDeleted = 0 ORDER BY title COLLATE NOCASE")
    fun observeAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(book: BookEntity)

    @Query("UPDATE books SET read = :read, readAt = :readAt, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun setRead(id: String, read: Boolean, readAt: Long?, updatedAt: Long = currentTimeMillis())

    @Query("UPDATE books SET isDeleted = 1, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long = currentTimeMillis())

    // ── Sync helpers (used by the unwired Firebase layer) ────────────────────
    @Query("SELECT * FROM books WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsynced(): List<BookEntity>

    @Query("UPDATE books SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)
}
