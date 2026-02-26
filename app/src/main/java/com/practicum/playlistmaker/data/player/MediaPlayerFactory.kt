package com.practicum.playlistmaker.data.player

import android.media.MediaPlayer

interface MediaPlayerFactory {
    fun create(): MediaPlayer
}
