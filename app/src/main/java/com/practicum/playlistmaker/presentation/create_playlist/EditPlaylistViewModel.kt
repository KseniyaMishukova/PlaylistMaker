package com.practicum.playlistmaker.presentation.create_playlist

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractor
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val playlistId: Long,
    createPlaylistInteractor: CreatePlaylistInteractor
) : CreatePlaylistViewModel(createPlaylistInteractor) {

    init {
        viewModelScope.launch {
            val p = createPlaylistInteractor.getPlaylistById(playlistId) ?: return@launch
            val coverUri = p.coverPath?.let { path ->
                try {
                    Uri.fromFile(java.io.File(path))
                } catch (_: Exception) {
                    null
                }
            }
            _state.value = CreatePlaylistState(
                name = p.name,
                description = p.description.orEmpty(),
                coverUri = coverUri,
                showBackDialog = false,
                pendingAction = null
            )
        }
    }

    override fun onBackPressed() {
        _state.value = _state.value?.copy(
            showBackDialog = false,
            pendingAction = PendingAction.NavigateBack
        )
    }

    override fun submitPlaylist(coverUri: Uri?) {
        val current = _state.value ?: return
        if (!current.isCreateEnabled) return
        viewModelScope.launch {
            createPlaylistInteractor.updatePlaylist(
                playlistId = playlistId,
                name = current.name.trim(),
                description = current.description.trim().takeIf { it.isNotEmpty() },
                coverUri = coverUri
            )
            _state.postValue(current.copy(pendingAction = PendingAction.NavigateBack))
        }
    }
}
