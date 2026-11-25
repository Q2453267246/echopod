package com.echopod.domain.playback.repository

import com.echopod.domain.playback.model.NowPlaying
import kotlinx.coroutines.flow.Flow

interface PlaybackRepository {
    val nowPlaying: Flow<NowPlaying?>
    suspend fun togglePlayPause()
    suspend fun seekBy(seconds: Int)
}