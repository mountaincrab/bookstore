# Book Shelf

A personal **reading tracker** for Android — record the books you've read, jot
notes, and browse your shelf grouped by author, genre, or recency. Search your
own library or add new books via the [Open Library](https://openlibrary.org) API.

Built with Kotlin Multiplatform + Jetpack Compose + Room + Koin, from the
[crab-do](https://github.com/mountaincrab/crab-do) template.

## Screens

- **Read** — your finished books, grouped by Recent / Author / Genre.
- **Search** — search every book on your shelf; read ones carry a "Read" badge
  and a tap-to-toggle. The **+** button adds a book (online search or manual).
- **Settings** — theme (Deep Navy / Charcoal / Retro).

## Run locally

No setup required — the app runs against a local Room database with **no auth and
no Firebase**.

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and the Android SDK.

## Cloud sync

A Firebase Auth + Firestore sync layer is **written but not wired in**, so the app
is fully usable offline today. See [`CLAUDE.md`](CLAUDE.md) →
"Switching Firebase on" for the steps to enable cloud sync later.

## Releases

Pushing to `main` runs the `Release` workflow, which derives the next semantic
version from [Conventional Commits](https://www.conventionalcommits.org/), tags
it, and publishes a GitHub Release with the built APK. Use Conventional Commit
prefixes (`feat:`, `fix:`, …) — including on squash-merge **PR titles**.
