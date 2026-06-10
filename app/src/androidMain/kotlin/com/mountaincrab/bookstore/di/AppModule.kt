package com.mountaincrab.bookstore.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mountaincrab.bookstore.data.local.ALL_MIGRATIONS
import com.mountaincrab.bookstore.data.local.AppDatabase
import com.mountaincrab.bookstore.data.remote.BookSearchService
import com.mountaincrab.bookstore.data.repository.BackupRepository
import com.mountaincrab.bookstore.data.repository.BookRepository
import com.mountaincrab.bookstore.data.repository.BookSearchRepository
import com.mountaincrab.bookstore.preferences.UserPreferencesRepository
import com.mountaincrab.bookstore.ui.read.ReadViewModel
import com.mountaincrab.bookstore.ui.settings.BackupViewModel
import com.mountaincrab.bookstore.ui.search.AddEditBookViewModel
import com.mountaincrab.bookstore.ui.search.SearchViewModel
import com.mountaincrab.bookstore.ui.theme.ThemeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Active runtime graph. Local-only: Room + Open Library search + theme prefs.
 * Firebase is deliberately absent here — its singles live in the dormant
 * `firebaseModule`, which is not passed to startKoin (see CLAUDE.md).
 */
val appModule = module {
    // Room database (local source of truth)
    single {
        Room.databaseBuilder<AppDatabase>(
            context = androidContext(),
            name = "bookshelf_db",
        )
            .setDriver(BundledSQLiteDriver())
            .addMigrations(*ALL_MIGRATIONS)
            // Wipe only on downgrade. Upgrades MUST have a migration in
            // ALL_MIGRATIONS or Room will crash — the intended safety net so we
            // never silently drop user data.
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }
    single { get<AppDatabase>().bookDao() }

    // Preferences (DataStore)
    single { UserPreferencesRepository(androidContext()) }

    // Ktor client for Open Library search
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { BookSearchService(client = get()) }

    // Repositories
    single { BookRepository(bookDao = get()) }
    single { BookSearchRepository(service = get()) }
    single { BackupRepository(bookDao = get(), context = androidContext()) }

    // ViewModels
    viewModel { ReadViewModel(bookRepository = get()) }
    viewModel { SearchViewModel(bookRepository = get()) }
    viewModel { AddEditBookViewModel(bookRepository = get(), searchRepository = get()) }
    viewModel { ThemeViewModel(prefs = get()) }
    viewModel { BackupViewModel(backupRepository = get()) }
}
