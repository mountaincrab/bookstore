package com.mountaincrab.bookstore.data.model

/**
 * Per-row sync state. PENDING rows are local writes not yet pushed to Firestore;
 * SYNCED rows match the remote copy. Used by the (currently unwired) Firebase
 * sync layer — every local mutation marks the row PENDING so it is ready to push
 * once Firebase is switched on. See CLAUDE.md → "Switching Firebase on".
 */
enum class SyncStatus { PENDING, SYNCED }
