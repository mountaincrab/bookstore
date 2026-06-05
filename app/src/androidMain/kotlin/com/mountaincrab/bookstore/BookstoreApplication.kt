package com.mountaincrab.bookstore

import android.app.Application
import com.mountaincrab.bookstore.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BookstoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Local-only graph. Firebase is written but not wired in: `firebaseModule`
        // is intentionally NOT loaded here, so the app runs with no
        // google-services.json and no auth. See CLAUDE.md → "Switching Firebase on".
        startKoin {
            androidContext(this@BookstoreApplication)
            modules(appModule)
        }
    }
}
