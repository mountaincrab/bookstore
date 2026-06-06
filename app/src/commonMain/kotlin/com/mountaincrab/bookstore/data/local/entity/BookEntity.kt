package com.mountaincrab.bookstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mountaincrab.bookstore.data.model.SyncStatus
import com.mountaincrab.bookstore.util.currentTimeMillis
import com.mountaincrab.bookstore.util.randomUUID

/**
 * A book on the shelf. A book record exists only for books you've read, so there
 * is no read/unread status. [readAt] is set when the book is recorded so the Read
 * screen's "Recent" grouping can sort by it.
 */
@Entity(tableName = "books")
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
)
