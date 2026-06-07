package com.mountaincrab.bookstore.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mountaincrab.bookstore.data.model.SyncStatus
import com.mountaincrab.bookstore.util.currentTimeMillis
import com.mountaincrab.bookstore.util.randomUUID

/**
 * A book on the shelf. A book record exists only for books you've read, so there
 * is no read/unread status. [readAt] is set when the book is recorded so the Read
 * screen's "Recent" grouping can sort by it.
 *
 * [isbn] is stored as a normalised ISBN-13 string when available (from Open Library
 * or entered manually). The unique index allows multiple NULLs (SQLite treats each
 * NULL as distinct) while preventing two records with the same known ISBN.
 */
@Entity(
    tableName = "books",
    indices = [Index(value = ["isbn"], unique = true)],
)
data class BookEntity(
    @PrimaryKey val id: String = randomUUID(),
    val title: String,
    val author: String,
    val genres: List<String> = emptyList(),
    val notes: String = "",
    val readAt: Long? = null,
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false,
    val isbn: String? = null,
)
