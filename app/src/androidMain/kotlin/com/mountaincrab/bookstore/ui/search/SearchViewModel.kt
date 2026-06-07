package com.mountaincrab.bookstore.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.remote.BookSearchResult
import com.mountaincrab.bookstore.data.repository.BookRepository
import com.mountaincrab.bookstore.data.repository.BookSearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State of the online (Open Library) lookup on the Search screen. */
data class OnlineSearchState(
    val loading: Boolean = false,
    val results: List<BookSearchResult> = emptyList(),
    val error: String? = null,
    /** True once a search has been run, so the UI can show an empty/idle state. */
    val searched: Boolean = false,
)

/**
 * Drives the Search screen: an online title/author lookup against Open Library.
 * Searching is explicit (triggered by the Search button), not as-you-type.
 */
class SearchViewModel(
    private val searchRepository: BookSearchRepository,
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _author = MutableStateFlow("")
    val author: StateFlow<String> = _author

    private val _state = MutableStateFlow(OnlineSearchState())
    val state: StateFlow<OnlineSearchState> = _state

    /** Local shelf — used to mark already-read books in search results. */
    val readBooks: StateFlow<List<BookEntity>> = bookRepository.observeBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    fun onTitleChange(value: String) { _title.value = value }
    fun onAuthorChange(value: String) { _author.value = value }

    val canSearch: Boolean get() = _title.value.isNotBlank() || _author.value.isNotBlank()

    fun search() {
        val title = _title.value
        val author = _author.value
        if (title.isBlank() && author.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = OnlineSearchState(loading = true, searched = true)
            _state.value = searchRepository.search(title, author).fold(
                onSuccess = { OnlineSearchState(results = it, searched = true) },
                onFailure = { OnlineSearchState(error = "Couldn't reach Open Library. Try again.", searched = true) },
            )
        }
    }

    fun clear() {
        searchJob?.cancel()
        _title.value = ""
        _author.value = ""
        _state.value = OnlineSearchState()
    }
}
