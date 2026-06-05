package com.mountaincrab.bookstore.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A book returned from an online search (Open Library), used to seed the add form. */
data class BookSearchResult(
    val title: String,
    val author: String,
    val year: Int?,
    val genres: List<String>,
)

@Serializable
private data class OpenLibraryResponse(
    val docs: List<OpenLibraryDoc> = emptyList(),
)

@Serializable
private data class OpenLibraryDoc(
    val title: String? = null,
    @SerialName("author_name") val authorName: List<String> = emptyList(),
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    val subject: List<String> = emptyList(),
)

/**
 * Searches the Open Library Search API — https://openlibrary.org/search.json —
 * which requires no API key. (To switch to Google Books later, swap the URL and
 * add a BOOKS_API_KEY; see CLAUDE.md.)
 */
class BookSearchService(
    private val client: HttpClient,
) {
    suspend fun search(query: String): List<BookSearchResult> {
        if (query.isBlank()) return emptyList()
        val response: OpenLibraryResponse = client.get("https://openlibrary.org/search.json") {
            parameter("q", query)
            parameter("limit", 20)
            parameter("fields", "title,author_name,first_publish_year,subject")
        }.body()
        return response.docs
            .filter { !it.title.isNullOrBlank() }
            .map { doc ->
                BookSearchResult(
                    title = doc.title.orEmpty(),
                    author = doc.authorName.firstOrNull() ?: "Unknown",
                    year = doc.firstPublishYear,
                    // Open Library subjects are noisy; take the first couple as genres.
                    genres = doc.subject.take(2),
                )
            }
    }
}
