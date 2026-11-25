package com.echopod.data.playback

import com.echopod.domain.playback.model.NowPlaying
import com.echopod.domain.playback.repository.PlaybackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class FakePlaybackRepository @Inject constructor() : PlaybackRepository {

    private val _nowPlaying = MutableStateFlow<NowPlaying?>(
        NowPlaying(
            episodeTitle = "在深夜写代码的人，都在想些什么",
            podcastTitle = "夜航电台",
            speed = 1.2f,
            progress = 17f / 40f,
            remainingMinutes = 23,
            isPlaying = true,
        )
    )

    override val nowPlaying: Flow<NowPlaying?> = _nowPlaying.asStateFlow()

    override suspend fun togglePlayPause() {
        _nowPlaying.update { current ->
            current?.copy(isPlaying = !current.isPlaying)
        }
    }

    override suspend fun seekBy(seconds: Int) {
        _nowPlaying.update { current ->
            current ?: return@update null
            val newProgress = (current.progress + seconds.toFloat() / FAKE_DURATION_SECONDS)
                .coerceIn(0f, 1f)
            val newRemainingMinutes = ((1f - newProgress) * FAKE_DURATION_SECONDS / 60f)
                .roundToInt()
                .coerceAtLeast(0)
            current.copy(
                progress = newProgress,
                remainingMinutes = newRemainingMinutes,
            )
        }
    }

    private companion object {
        const val FAKE_DURATION_SECONDS = 40 * 60
    }
}
