package com.mountaincrab.bookstore.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.model.BookSource
import com.mountaincrab.bookstore.data.model.SyncStatus
import com.mountaincrab.bookstore.util.currentTimeMillis

// ─── BookEntity ↔ Firestore document ─────────────────────────────────────────
// Part of the unwired Firebase layer. Mirrors crab-do's FirestoreMappers style.
// Books live at users/{uid}/books/{bookId}.

fun BookEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "author" to author,
    "genres" to genres,
    "read" to read,
    "source" to source.name,
    "notes" to notes,
    "readAt" to readAt,
    "createdAt" to createdAt,
    "updatedAt" to FieldValue.serverTimestamp(),
    "isDeleted" to isDeleted,
)

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toBookEntity(): BookEntity = BookEntity(
    id = id,
    title = getString("title") ?: "",
    author = getString("author") ?: "Unknown",
    genres = (get("genres") as? List<String>) ?: emptyList(),
    read = getBoolean("read") ?: false,
    source = BookSource.fromName(getString("source")),
    notes = getString("notes") ?: "",
    readAt = getLong("readAt"),
    createdAt = getLong("createdAt") ?: currentTimeMillis(),
    updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: currentTimeMillis(),
    syncStatus = SyncStatus.SYNCED,
    isDeleted = getBoolean("isDeleted") ?: false,
)
