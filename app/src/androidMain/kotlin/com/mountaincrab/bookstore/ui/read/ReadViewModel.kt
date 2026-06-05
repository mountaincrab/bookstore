package com.mountaincrab.bookstore.ui.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.repository.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    val readBooks: StateFlow<List<BookEntity>> = bookRepository.observeReadBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setRead(id: String, read: Boolean) {
        viewModelScope.launch { bookRepository.setRead(id, read) }
    }
}
