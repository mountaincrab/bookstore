package com.mountaincrab.bookstore.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = prefs.themeName
        .map { AppTheme.fromName(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.DEEP_NAVY)

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { prefs.setThemeName(theme.name) }
    }
}
