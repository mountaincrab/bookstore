package com.mountaincrab.bookstore.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.repository.BookRepository
import kotlinx.coroutines.launch

class AddEditBookViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    fun addBook(title: String, author: String, genres: List<String>, notes: String) {
        viewModelScope.launch {
            bookRepository.addBook(title, author, genres, notes)
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch { bookRepository.updateBook(book) }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch { bookRepository.deleteBook(id) }
    }
}
