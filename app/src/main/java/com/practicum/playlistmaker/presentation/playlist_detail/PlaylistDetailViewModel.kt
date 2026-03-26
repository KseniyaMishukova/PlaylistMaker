package com.practicum.playlistmaker.presentation.playlist_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractor
import com.practicum.playlistmaker.util.PlaylistShareTextFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface PlaylistDetailEffect {
    data object NavigateBackAfterPlaylistDeleted : PlaylistDetailEffect
    data class NavigateToPlayer(val track: Track) : PlaylistDetailEffect
}

sealed interface PlaylistShareResult {
    data object NoTracks : PlaylistShareResult
    data class Text(val body: String) : PlaylistShareResult
}

data class PlaylistDetailState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalMinutes: Int = 0,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModel(
    application: Application,
    private val playlistId: Long,
    private val createPlaylistInteractor: CreatePlaylistInteractor
) : AndroidViewModel(application) {

    private val _state = MutableLiveData(PlaylistDetailState())
    val state: LiveData<PlaylistDetailState> = _state

    private val _effect = MutableLiveData<PlaylistDetailEffect?>()
    val effect: LiveData<PlaylistDetailEffect?> = _effect

    fun consumeEffect() {
        _effect.value = null
    }

    init {
        viewModelScope.launch {
            createPlaylistInteractor.observePlaylistById(playlistId)
                .flatMapLatest { playlist ->
                    if (playlist == null) {
                        flowOf(
                            PlaylistDetailState(
                                playlist = null,
                                tracks = emptyList(),
                                totalMinutes = 0,
                                isLoading = false
                            )
                        )
                    } else {
                        createPlaylistInteractor.observeTracksByIds(playlist.trackIds).map { tracks ->
                            val byId = tracks.associateBy { it.trackId }
                            val ordered = playlist.trackIds.asReversed().mapNotNull { byId[it] }
                            val totalMs = ordered.sumOf { it.trackTimeMillis }
                            PlaylistDetailState(
                                playlist = playlist,
                                tracks = ordered,
                                totalMinutes = totalMinutesFromMillis(totalMs),
                                isLoading = false
                            )
                        }
                    }
                }
                .collect { _state.value = it }
        }
    }

    fun onTrackClicked(track: Track) {
        _effect.value = PlaylistDetailEffect.NavigateToPlayer(track)
    }

    fun buildShareResult(): PlaylistShareResult {
        val s = _state.value ?: return PlaylistShareResult.NoTracks
        val p = s.playlist ?: return PlaylistShareResult.NoTracks
        if (s.tracks.isEmpty()) return PlaylistShareResult.NoTracks
        val text = PlaylistShareTextFormatter.build(getApplication(), p, s.tracks)
        return PlaylistShareResult.Text(text)
    }

    fun removeTrackFromPlaylist(trackId: Long) {
        viewModelScope.launch {
            createPlaylistInteractor.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            createPlaylistInteractor.deletePlaylist(playlistId)
            _effect.postValue(PlaylistDetailEffect.NavigateBackAfterPlaylistDeleted)
        }
    }

    private fun totalMinutesFromMillis(durationSum: Long): Int {
        if (durationSum <= 0L) return 0
        return (durationSum / 60_000L).toInt()
    }
}
