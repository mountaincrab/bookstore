package com.mountaincrab.bookstore.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val results: StateFlow<List<BookEntity>> =
        combine(bookRepository.observeAllBooks(), _query) { books, q ->
            val t = q.trim().lowercase()
            if (t.isEmpty()) books
            else books.filter {
                (it.title + " " + it.author + " " + it.genres.joinToString(" ")).lowercase().contains(t)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(value: String) { _query.value = value }

    fun setRead(id: String, read: Boolean) {
        viewModelScope.launch { bookRepository.setRead(id, read) }
    }
}
