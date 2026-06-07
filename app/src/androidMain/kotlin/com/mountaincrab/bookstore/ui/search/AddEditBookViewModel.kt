package com.mountaincrab.bookstore.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.repository.AddBookResult
import com.mountaincrab.bookstore.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

sealed class AddEditEvent {
    data object Saved : AddEditEvent()
    data object AlreadyRead : AddEditEvent()
}

class AddEditBookViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _event = MutableSharedFlow<AddEditEvent>()
    val event: SharedFlow<AddEditEvent> = _event

    fun addBook(title: String, author: String, genres: List<String>, notes: String, isbn: String? = null) {
        viewModelScope.launch {
            val result = bookRepository.addBook(title, author, genres, notes, isbn)
            _event.emit(
                when (result) {
                    is AddBookResult.Added -> AddEditEvent.Saved
                    is AddBookResult.AlreadyRead -> AddEditEvent.AlreadyRead
                }
            )
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch {
            bookRepository.updateBook(book)
            _event.emit(AddEditEvent.Saved)
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch { bookRepository.deleteBook(id) }
    }
}
