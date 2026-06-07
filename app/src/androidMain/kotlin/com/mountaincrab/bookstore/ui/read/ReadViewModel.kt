package com.mountaincrab.bookstore.ui.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.repository.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReadViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    val readBooks: StateFlow<List<BookEntity>> = bookRepository.observeBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
