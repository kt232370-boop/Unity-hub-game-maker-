package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.UserProfile
import com.example.data.remote.UserProfileRepository
import com.example.ui.auth.PendingUnityIdDraft
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Ready(val profile: UserProfile?) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class AccountViewModel(
    private val repository: UserProfileRepository,
    private val currentUserId: String
) : ViewModel() {

    val profileState: StateFlow<ProfileUiState> = repository.observeProfile(currentUserId)
        .map<UserProfile?, ProfileUiState> { ProfileUiState.Ready(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(ProfileUiState.Error(rootError.message ?: "Failed to load Unity ID profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ProfileUiState.Loading
        )

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    fun syncSignedInProfile(draft: PendingUnityIdDraft?) {
        viewModelScope.launch {
            val result = repository.syncGoogleUserProfile(
                preferredEmail = draft?.email,
                preferredUsername = draft?.username,
                preferredFullName = draft?.fullName,
                emailCodeVerified = draft?.emailVerified == true
            )
            result.onFailure { err ->
                _syncError.value = err.localizedMessage ?: "Failed to sync Unity ID profile"
            }
        }
    }

    fun clearSyncError() {
        _syncError.value = null
    }

    private companion object {
        const val TAG = "AccountViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
