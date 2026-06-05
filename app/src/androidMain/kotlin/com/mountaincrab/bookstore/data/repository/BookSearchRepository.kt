package com.mountaincrab.bookstore.data.repository

import com.mountaincrab.bookstore.data.remote.BookSearchResult
import com.mountaincrab.bookstore.data.remote.BookSearchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Thin wrapper over [BookSearchService] that turns failures into a Result. */
class BookSearchRepository(
    private val service: BookSearchService,
) {
    suspend fun search(query: String): Result<List<BookSearchResult>> =
        withContext(Dispatchers.IO) {
            runCatching { service.search(query) }
        }
}
