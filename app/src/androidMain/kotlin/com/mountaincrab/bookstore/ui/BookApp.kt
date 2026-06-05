package com.mountaincrab.bookstore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.ui.components.BookDetailSheet
import com.mountaincrab.bookstore.ui.components.BottomTabBar
import com.mountaincrab.bookstore.ui.components.BookTab
import com.mountaincrab.bookstore.ui.read.ReadScreen
import com.mountaincrab.bookstore.ui.search.AddEditBookSheet
import com.mountaincrab.bookstore.ui.search.AddEditBookViewModel
import com.mountaincrab.bookstore.ui.search.SearchScreen
import com.mountaincrab.bookstore.ui.settings.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Top-level app shell: bottom-tab navigation (Read / Search / Settings) plus the
 * shared detail + add/edit bottom sheets. No auth gate — the app is local-only.
 */
@Composable
fun BookApp() {
    var tab by remember { mutableStateOf(BookTab.READ) }

    // Sheet state
    var detailBook by remember { mutableStateOf<BookEntity?>(null) }
    var addEditOpen by remember { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<BookEntity?>(null) }

    // Shared VM for add/edit/delete/toggle from the sheets.
    val addEditViewModel: AddEditBookViewModel = koinViewModel()

    Scaffold(
        bottomBar = { BottomTabBar(active = tab, onSelect = { tab = it }) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (tab) {
                BookTab.READ -> ReadScreen(
                    onOpenBook = { detailBook = it },
                    onAddBook = { editingBook = null; addEditOpen = true },
                )
                BookTab.SEARCH -> SearchScreen(
                    onOpenBook = { detailBook = it },
                    onAddBook = { editingBook = null; addEditOpen = true },
                )
                BookTab.SETTINGS -> SettingsScreen()
            }
        }
    }

    detailBook?.let { book ->
        BookDetailSheet(
            book = book,
            onDismiss = { detailBook = null },
            onToggleRead = {
                addEditViewModel.setRead(book.id, !book.read)
                // Reflect the change in the open sheet immediately.
                detailBook = book.copy(read = !book.read)
            },
            onEdit = {
                editingBook = book
                detailBook = null
                addEditOpen = true
            },
        )
    }

    if (addEditOpen) {
        AddEditBookSheet(
            existing = editingBook,
            onDismiss = { addEditOpen = false; editingBook = null },
            viewModel = addEditViewModel,
        )
    }
}
