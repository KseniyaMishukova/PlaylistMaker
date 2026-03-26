package com.practicum.playlistmaker.presentation.create_playlist

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractor
import kotlinx.coroutines.launch

data class CreatePlaylistState(
    val name: String = "",
    val description: String = "",
    val coverUri: Uri? = null,
    val showBackDialog: Boolean = false,
    val pendingAction: PendingAction? = null
) {
    val isCreateEnabled: Boolean get() = name.isNotBlank()
    val hasUnsavedData: Boolean get() = name.isNotBlank() || description.isNotBlank() || coverUri != null
}

sealed class PendingAction {
    data object NavigateBack : PendingAction()
    data class PlaylistCreated(val playlistName: String) : PendingAction()
}

open class CreatePlaylistViewModel(
    protected val createPlaylistInteractor: CreatePlaylistInteractor
) : ViewModel() {

    protected val _state = MutableLiveData(CreatePlaylistState())
    val state: LiveData<CreatePlaylistState> = _state

    fun setName(name: String) {
        _state.value = _state.value?.copy(name = name)
    }

    fun setDescription(description: String) {
        _state.value = _state.value?.copy(description = description)
    }

    fun setCoverUri(uri: Uri?) {
        _state.value = _state.value?.copy(coverUri = uri)
    }

    open fun onBackPressed() {
        val current = _state.value ?: return
        if (current.hasUnsavedData) {
            _state.value = current.copy(showBackDialog = true)
        } else {
            _state.value = current.copy(pendingAction = PendingAction.NavigateBack)
        }
    }

    fun dismissBackDialog() {
        _state.value = _state.value?.copy(showBackDialog = false)
    }

    fun confirmBack() {
        _state.value = _state.value?.copy(showBackDialog = false, pendingAction = PendingAction.NavigateBack)
    }

    open fun submitPlaylist(coverUri: Uri?) {
        val current = _state.value ?: return
        if (!current.isCreateEnabled) return

        viewModelScope.launch {
            createPlaylistInteractor.createPlaylist(
                name = current.name.trim(),
                description = current.description.trim().takeIf { it.isNotEmpty() },
                coverUri = coverUri
            )
            _state.postValue(current.copy(pendingAction = PendingAction.PlaylistCreated(current.name.trim())))
        }
    }

    fun clearPendingAction() {
        _state.value = _state.value?.copy(pendingAction = null)
    }
}
