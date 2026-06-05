plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    // NOTE: the `google-services` plugin is intentionally NOT applied. Firebase
    // is written but not wired in (see CLAUDE.md → "Switching Firebase on"), so
    // the app builds and runs with no google-services.json. The plugin alias is
    // still defined in gradle/libs.versions.toml for when you switch it on.
}
