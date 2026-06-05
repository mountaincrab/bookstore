# Book Shelf — Claude guidance

A personal **reading tracker** Android app: record the books you've read, with
notes, grouped by author / genre / recency, plus search (your local library +
online "add" via the Open Library API). Built from the **crab-do** template
(Kotlin Multiplatform + Jetpack Compose + Room + Koin), with a Firebase/Firestore
sync layer that is **written but not wired in** — the app runs locally with no
auth against a local Room DB.

## Project structure

Single Android module (`app/`). No web app, no cloud functions.

| Path | What it is |
|------|-----------|
| `app/src/commonMain/` | Shared Room entities/DAOs, `AppDatabase`, models, KMP `util` (`expect`) |
| `app/src/androidMain/` | Compose UI, repositories, DI, the (unwired) Firebase layer, `util` (`actual`) |
| `app/src/androidInstrumentedTest/` | Room migration tests (run on a device/emulator) |
| `.github/workflows/` | `android-build.yml` (PR) + `release.yml` (semver + GitHub Release) |
| `firestore.rules` | Owner-only rules for `users/{uid}/books/**` (deploy when Firebase is on) |

## Stack

Kotlin Multiplatform (Android target only), Jetpack Compose + Material 3, Room
(local DB, `BundledSQLiteDriver`), Koin (DI), DataStore (theme preference), Ktor
client (Open Library search), Firebase Auth + Firestore (present but unwired).

## Build

```bash
./gradlew :app:assembleDebug                 # full debug APK (no Firebase setup needed)
./gradlew :app:compileDebugKotlinAndroid     # faster compile-only check
./gradlew -q :app:printVersionName           # git-derived version
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The build needs **no `google-services.json`** — see "Switching Firebase on".

## Commit messages (required: Conventional Commits)

Every commit/PR **must** use [Conventional Commits](https://www.conventionalcommits.org/).
The `Release` workflow feeds commit history through `mathieudutour/github-tag-action`,
which both **bumps the semver tag** and **generates the GitHub Release notes** from
commit subjects. An unprefixed commit is silently dropped from the changelog.

| Prefix | Bump |
|--------|------|
| `feat:` | minor |
| `fix:` | patch |
| `feat!:` / `BREAKING CHANGE:` footer | major |
| `ci:` `chore:` `docs:` `refactor:` `perf:` `test:` `style:` | patch |

**Squash-merge caveat:** the PR title becomes the `main` commit subject, so the
**PR title** is what needs the Conventional Commits prefix.

## Versioning (git-driven)

`app/build.gradle.kts` computes `versionName`/`versionCode` from git at build
time (latest `vX.Y.Z` tag + branch/sha suffix on non-main builds), so no
version-bump commit is ever needed. `versionCode` = total commit count.

## UI

- `ui/BookApp.kt` — top-level shell: bottom tabs (Read / Search / Settings) +
  the shared detail and add/edit bottom sheets. No auth gate.
- `ui/read/` — **Read** home: grouped list of read books with a Group-by control
  (Recent / Author / Genre) + FAB.
- `ui/search/` — **Search** over all books (Read badge + tap-to-toggle), and the
  `AddEditBookSheet` (online Open Library search → form, or manual entry).
- `ui/settings/` — theme picker.
- `ui/theme/Theme.kt` — Deep Navy (default), Charcoal, Retro. Each theme needs an
  entry in the `AppTheme` enum, a `ColorScheme`, an `AppPalette`, the
  `paletteFor`/`BookTheme` `when` blocks, and the swatch in `SettingsScreen.kt`.
- `ui/components/` — reusable atoms (`BookRow`, `ReadToggle`, `ReadBadge`,
  `MonogramTile`, `GenreChip`, `SourceTag`, `BottomTabBar`, sheets).

## Data model

```
users/{uid}/books/{bookId}     ← (Firestore, when enabled)
```

**Book fields** (`BookEntity` / Firestore): `id`, `title`, `author`,
`genres` (`List<String>`), `read` (Boolean), `source` (`LIBRARY|BOUGHT|BORROWED`),
`notes`, `readAt` (millis, for "Recent" ordering), `createdAt`, `updatedAt`,
`syncStatus` (`PENDING|SYNCED`), `isDeleted` (soft delete).

The read model is **binary** — a book is read or unread (no reading/want-to-read
states), by design.

## Room migrations (required for every version bump)

`exportSchema = true`, KSP `room.schemaLocation = $projectDir/schemas`. Each
`@Database(version = N)` build emits `app/schemas/.../N.json` — **commit these**.

When bumping `version`:
1. Change the entity, increment `version`.
2. `./gradlew :app:compileDebugKotlinAndroid` — emits the new schema JSON.
3. Diff old vs new, add a `Migration(old, new) { ... }` to `ALL_MIGRATIONS` in
   `data/local/Migrations.kt`.
4. **Add a migration test** in
   `app/src/androidInstrumentedTest/.../MigrationTest.kt` (create database at the
   old version, run `runMigrationsAndValidate`, assert data survives). A migration
   without a test is not done.

The DB uses `fallbackToDestructiveMigrationOnDowngrade` only — a version bump with
no migration **crashes on upgrade** by design (the local DB is the only copy of
`PENDING` writes). Don't reintroduce destructive fallback for upgrades.

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.mountaincrab.bookstore.data.local.MigrationTest
```

## Repository / mutation pattern

`BookRepository` is Room-only today. Each mutation sets `syncStatus = PENDING`
(ready to push) but does **not** enqueue sync work, because Firebase is unwired.
When you switch Firebase on, add an `enqueueSyncWork()` call (enqueueing
`BookSyncWorker`) to each mutation — exactly like crab-do's repositories.

## Online search

`data/remote/BookSearchService.kt` queries the **Open Library Search API**
(`https://openlibrary.org/search.json`), which needs **no API key**. To switch to
Google Books later, change the URL and add a `BOOKS_API_KEY`.

## Switching Firebase on

Firebase is fully written but deliberately inert:
- The `google-services` plugin is **not applied** (`app/build.gradle.kts`).
- `di/FirebaseModule.kt` (`firebaseModule`) is **not** passed to `startKoin`.
- `MainActivity` has **no auth gate**.
- `auth/AuthRepository.kt`, `data/remote/FirestoreMappers.kt`,
  `data/remote/BookSyncWorker.kt` compile but are never invoked.

To enable cloud sync + auth:
1. Create a Firebase project; download `google-services.json` into `app/`.
2. Apply the plugin: uncomment `alias(libs.plugins.google.services)` in
   `app/build.gradle.kts`.
3. Load the module: `modules(appModule, firebaseModule)` in
   `BookstoreApplication`.
4. Enqueue `BookSyncWorker` from each `BookRepository` mutation.
5. Add an auth gate + Login screen to `MainActivity`; set
   `R.string.google_web_client_id`.
6. Deploy `firestore.rules`.

## CI

- `android-build.yml` — on PRs: builds the debug APK, uploads it as an artifact.
- `release.yml` — on push to `main`: tags via `github-tag-action`, then builds
  and publishes a GitHub Release with the APK. The `GOOGLE_SERVICES_JSON` step is
  inert until the plugin is applied; it's kept for drop-in parity.

GitHub Actions secrets (all optional; builds pass without them):
`DEBUG_KEYSTORE` (stable signing), `GOOGLE_SERVICES_JSON` (Firebase, once wired
in), `GITHUB_TOKEN` (built-in).
