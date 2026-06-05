package com.mountaincrab.bookstore.data.local

import androidx.room.migration.Migration

/**
 * Room migration registry. Empty at schema version 1 (nothing to migrate yet).
 *
 * When bumping `@Database(version = N)`:
 *   1. Make the entity change and increment the version.
 *   2. `./gradlew :app:compileDebugKotlinAndroid` — emits app/schemas/.../N.json.
 *   3. Diff old vs new JSON, add a `Migration(old, new) { ... }` here.
 *   4. Add a migration test (see CLAUDE.md → Room migrations). A migration
 *      without a test is not done. Bumping the version with no migration will
 *      crash the app on upgrade — that is the intended safety net.
 */
val ALL_MIGRATIONS: Array<Migration> = arrayOf()
