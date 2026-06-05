package com.mountaincrab.bookstore.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.model.BookSource
import com.mountaincrab.bookstore.data.remote.BookSearchResult
import com.mountaincrab.bookstore.data.repository.BookRepository
import com.mountaincrab.bookstore.data.repository.BookSearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** State of the online (Open Library) search inside the Add sheet. */
data class OnlineSearchState(
    val loading: Boolean = false,
    val results: List<BookSearchResult> = emptyList(),
    val error: String? = null,
)

class AddEditBookViewModel(
    private val bookRepository: BookRepository,
    private val searchRepository: BookSearchRepository,
) : ViewModel() {

    private val _onlineSearch = MutableStateFlow(OnlineSearchState())
    val onlineSearch: StateFlow<OnlineSearchState> = _onlineSearch

    private var searchJob: Job? = null

    /** Debounced online search against Open Library. */
    fun searchOnline(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _onlineSearch.value = OnlineSearchState()
            return
        }
        searchJob = viewModelScope.launch {
            delay(350) // debounce
            _onlineSearch.value = OnlineSearchState(loading = true)
            val result = searchRepository.search(query)
            _onlineSearch.value = result.fold(
                onSuccess = { OnlineSearchState(results = it) },
                onFailure = { OnlineSearchState(error = "Couldn't reach Open Library. Add it manually below.") },
            )
        }
    }

    fun resetSearch() {
        searchJob?.cancel()
        _onlineSearch.value = OnlineSearchState()
    }

    fun addBook(title: String, author: String, genres: List<String>, read: Boolean, source: BookSource, notes: String) {
        viewModelScope.launch {
            bookRepository.addBook(title, author, genres, read, source, notes)
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch { bookRepository.updateBook(book) }
    }

    fun setRead(id: String, read: Boolean) {
        viewModelScope.launch { bookRepository.setRead(id, read) }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch { bookRepository.deleteBook(id) }
    }
}
