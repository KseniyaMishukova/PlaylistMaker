package com.practicum.playlistmaker.data.player

import android.media.MediaPlayer

class MediaPlayerFactoryImpl : MediaPlayerFactory {
    override fun create(): MediaPlayer = MediaPlayer()
}
