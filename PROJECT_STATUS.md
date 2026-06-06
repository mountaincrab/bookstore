# Project status — Book Shelf

_Last updated: 2026-06-06_

A personal **reading tracker** Android app: record the books you've read, with
notes, browse them grouped by author / genre / recency, and search your library
or add new books via the **Open Library** API. Built from the **crab-do**
template (Kotlin Multiplatform + Jetpack Compose + Room + Koin).

The app **runs locally with no setup** — `./gradlew :app:assembleDebug` builds
with no `google-services.json` and no auth, against a local Room database. A
Firebase Auth + Firestore sync layer is fully written but deliberately **not
wired in** (see "Switching Firebase on" in [`CLAUDE.md`](CLAUDE.md)).

---

## What has been done

### App (design Option B, binary read/unread model)
- **Read** tab (home): grouped list of *finished* books with a **Group by**
  control — Recent / Author / Genre. Author-initial monogram tiles, genre chips,
  source tags, and a FAB to add. (`ui/read/`)
- **Search** tab: searches every book in the library; read ones show a green
  "Read" badge and a tap-to-toggle to mark read/unread. (`ui/search/SearchScreen.kt`)
- **Add / Edit** bottom sheet: **online search via Open Library** (no API key)
  to seed the form, or **manual entry** — title, author, genres, status
  (read/unread), where-from (library/bought/borrowed), notes.
  (`ui/search/AddEditBookSheet.kt`, `data/remote/BookSearchService.kt`)
- **Book detail** bottom sheet: notes, genre/source chips, mark read/unread, edit.
- **Settings** tab: theme picker — Deep Navy (default), Charcoal, Retro.
- **Theme**: ported from crab-do's design system (`ui/theme/Theme.kt`).

### Data + architecture
- **Room** local DB (`BookEntity`, `BookDao`, `AppDatabase` v1, exported schema
  committed at `app/schemas/.../1.json`). `BookRepository` is Room-only and marks
  each mutation `syncStatus = PENDING`, ready for sync once Firebase is enabled.
- **Koin** DI (`di/AppModule.kt`) — local graph only.
- **Ktor** client for Open Library search.
- **DataStore** for the theme preference.

### Firebase (written, NOT wired in)
- `auth/AuthRepository.kt`, `data/remote/FirestoreMappers.kt`,
  `data/remote/BookSyncWorker.kt`, `di/FirebaseModule.kt` all compile but are
  never invoked: the `google-services` plugin is not applied, `firebaseModule`
  is not loaded into `startKoin`, and `MainActivity` has no auth gate.
- `firestore.rules` (owner-only `users/{uid}/books/**`) is ready to deploy.

### Build, CI, docs
- Git-driven versioning (latest `vX.Y.Z` tag + branch/sha suffix), `versionCode`
  = commit count. `./gradlew -q :app:printVersionName` exposes it to CI.
- Two GitHub Actions workflows mirroring crab-do:
  - `android-build.yml` — builds a debug APK on every PR, uploads it as an artifact.
  - `release.yml` — on push to `main`: semver bump + tag via
    `mathieudutour/github-tag-action`, then publishes a GitHub Release with the
    APK via `softprops/action-gh-release`.
- `CLAUDE.md` (full guidance) and `README.md`.

### Verified
- `./gradlew :app:compileDebugKotlinAndroid` — passes (only harmless
  `statusBarColor`/`navigationBarColor` deprecation warnings).
- `./gradlew :app:assembleDebug` — passes; produces a ~33 MB debug APK and
  exports the Room schema JSON.
- `./gradlew -q :app:printVersionName` — resolves the git-derived version.

> Not run: on-device/emulator smoke test (no emulator available in the build
> environment) and instrumented Room migration tests (none needed at schema v1).

---

## What you need to do

### 1. Repository settings
- **Set the default branch to `main`** — the Release workflow triggers on push
  to `main`.
- Merge the feature branch via a PR, and give the **PR title** a Conventional
  Commits prefix (e.g. `feat: …`). On squash-merge the PR title becomes the
  `main` commit subject, which drives the semver bump and release notes. An
  unprefixed title is dropped from the changelog.

### 2. GitHub Actions secrets (all optional — builds pass without them)
| Secret | Purpose | Needed when |
|--------|---------|-------------|
| `DEBUG_KEYSTORE` | base64 of a debug `.keystore` for stable APK signing | You want consistent signatures across builds; otherwise an ephemeral key is used |
| `GOOGLE_SERVICES_JSON` | base64 Firebase config | **Inert today** (google-services plugin not applied). Kept for drop-in parity once Firebase is switched on |
| `GITHUB_TOKEN` | tagging + creating releases | Built-in — nothing to add |

To create `DEBUG_KEYSTORE`: `base64 -w0 ~/.android/debug.keystore` and paste the
output as the secret value.

### 3. To enable cloud sync later (full steps in `CLAUDE.md`)
1. Create a Firebase project; add `app/google-services.json`.
2. Uncomment `alias(libs.plugins.google.services)` in `app/build.gradle.kts`.
3. Load `firebaseModule` in `BookstoreApplication` (`modules(appModule, firebaseModule)`).
4. Enqueue `BookSyncWorker` from each `BookRepository` mutation.
5. Add an auth gate + Login screen to `MainActivity`; set
   `R.string.google_web_client_id` (currently a placeholder).
6. Deploy `firestore.rules`.

### 4. Optional
- Swap Open Library for **Google Books**: change the URL in
  `data/remote/BookSearchService.kt` and add a `BOOKS_API_KEY`.
</content>
