package com.mountaincrab.bookstore.data.remote

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mountaincrab.bookstore.data.local.dao.BookDao
import com.mountaincrab.bookstore.data.model.SyncStatus
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Push-then-pull Firestore sync for the `books` collection, mirroring crab-do's
 * SyncWorker pattern (push PENDING rows, then pull remote deltas by updatedAt).
 *
 * NOT wired in today: nothing enqueues this worker and the Firebase singles it
 * injects are not in the active Koin graph (they live in the dormant
 * `firebaseModule`). To switch on: load firebaseModule in startKoin and enqueue
 * this worker from BookRepository mutations. See CLAUDE.md.
 */
class BookSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val bookDao: BookDao by inject()
    private val firestore: FirebaseFirestore by inject()
    private val auth: FirebaseAuth by inject()

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()
        return try {
            pushPending(userId)
            pullRemote(userId)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun pushPending(userId: String) {
        val booksRef = firestore.collection("users").document(userId).collection("books")
        bookDao.getUnsynced().forEach { book ->
            booksRef.document(book.id)
                .set(book.toFirestoreMap(), SetOptions.merge())
                .await()
            bookDao.markSynced(book.id)
        }
    }

    private suspend fun pullRemote(userId: String) {
        val snapshot = firestore.collection("users").document(userId)
            .collection("books").get().await()
        snapshot.documents.forEach { doc ->
            val remote = doc.toBookEntity()
            val existing = bookDao.getById(remote.id)
            // Never clobber a local PENDING write that hasn't been pushed yet.
            if (existing?.syncStatus == SyncStatus.PENDING) return@forEach
            if (existing == null || remote.updatedAt >= existing.updatedAt) {
                bookDao.upsert(remote)
            }
        }
    }
}
