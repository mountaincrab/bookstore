package com.mountaincrab.bookstore.data.model

/** Where a book came from. Shown as a tag on book rows and in the detail sheet. */
enum class BookSource(val label: String) {
    LIBRARY("Library"),
    BOUGHT("Bought"),
    BORROWED("Borrowed");

    companion object {
        fun fromName(name: String?): BookSource =
            entries.firstOrNull { it.name == name } ?: BOUGHT
    }
}
