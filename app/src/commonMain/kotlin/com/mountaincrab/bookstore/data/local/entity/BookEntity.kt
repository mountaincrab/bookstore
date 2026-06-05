package com.mountaincrab.bookstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mountaincrab.bookstore.data.model.BookSource
import com.mountaincrab.bookstore.data.model.SyncStatus
import com.mountaincrab.bookstore.util.currentTimeMillis
import com.mountaincrab.bookstore.util.randomUUID

/**
 * A book on the shelf. Binary read model: [read] is the only status (the design
 * deliberately dropped reading/want-to-read lifecycle states). [readAt] records
 * when it was marked read so the Read screen's "Recent" grouping can sort by it.
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String = randomUUID(),
    val title: String,
    val author: String,
    val genres: List<String> = emptyList(),
    val read: Boolean = false,
    val source: BookSource = BookSource.BOUGHT,
    val notes: String = "",
    val readAt: Long? = null,
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false,
)
