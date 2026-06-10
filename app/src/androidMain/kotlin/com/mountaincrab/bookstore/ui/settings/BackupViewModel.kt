package com.mountaincrab.bookstore.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountaincrab.bookstore.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the backup/restore actions in Settings. */
sealed interface BackupUiState {
    data object Idle : BackupUiState
    data object Working : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

class BackupViewModel(
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun export(uri: Uri) {
        _state.value = BackupUiState.Working
        viewModelScope.launch {
            _state.value = try {
                val count = backupRepository.exportTo(uri)
                BackupUiState.Success("Backed up $count ${books(count)}.")
            } catch (e: Exception) {
                BackupUiState.Error(e.message ?: "Backup failed.")
            }
        }
    }

    fun import(uri: Uri) {
        _state.value = BackupUiState.Working
        viewModelScope.launch {
            _state.value = try {
                val count = backupRepository.importFrom(uri)
                BackupUiState.Success("Restored $count ${books(count)}.")
            } catch (e: Exception) {
                BackupUiState.Error(e.message ?: "Restore failed.")
            }
        }
    }

    fun clearMessage() {
        _state.value = BackupUiState.Idle
    }

    private fun books(count: Int) = if (count == 1) "book" else "books"
}
