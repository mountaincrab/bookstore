package com.mountaincrab.bookstore.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mountaincrab.bookstore.auth.AuthRepository
import org.koin.dsl.module

/**
 * Dormant Firebase graph. Provides FirebaseAuth, FirebaseFirestore and
 * AuthRepository for the (unwired) sync + auth layer.
 *
 * This module is intentionally NOT passed to startKoin() in
 * BookstoreApplication, so Firebase is never touched at runtime and the app
 * builds/runs with no google-services.json.
 *
 * To switch Firebase ON (see CLAUDE.md → "Switching Firebase on"):
 *   1. Add app/google-services.json + apply the google-services plugin.
 *   2. modules(appModule, firebaseModule) in BookstoreApplication.
 *   3. Enqueue BookSyncWorker from BookRepository mutations.
 *   4. Add an auth gate + Login screen to MainActivity.
 */
val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { AuthRepository(auth = get(), database = get()) }
}
