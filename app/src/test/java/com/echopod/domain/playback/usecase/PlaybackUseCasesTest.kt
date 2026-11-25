package com.echopod.domain.playback.usecase

import com.echopod.data.playback.FakePlaybackRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/** 播放相关 UseCase 的单测：直接用 Fake 仓库，不需要 Hilt。 */
class PlaybackUseCasesTest {

    private lateinit var repository: FakePlaybackRepository

    @Before
    fun setUp() {
        repository = FakePlaybackRepository()
    }

    @Test
    fun observeNowPlaying_emitsRepositoryValue() = runTest {
        val nowPlaying = ObserveNowPlayingUseCase(repository)().first()

        assertNotNull(nowPlaying)
        assertEquals("夜航电台", nowPlaying!!.podcastTitle)
    }

    @Test
    fun togglePlayback_flipsIsPlaying() = runTest {
        val before = repository.nowPlaying.first()!!.isPlaying

        TogglePlaybackUseCase(repository)()

        assertEquals(!before, repository.nowPlaying.first()!!.isPlaying)
    }

    @Test
    fun seekPlayback_forwardIncreasesProgressAndClampsToOne() = runTest {
        val seek = SeekPlaybackUseCase(repository)
        val before = repository.nowPlaying.first()!!.progress

        seek(30)
        assertEquals(before + 30f / (40 * 60), repository.nowPlaying.first()!!.progress, 1e-4f)

        seek(10 * 60 * 60) // 远超时长，进度应被限制在 1
        val end = repository.nowPlaying.first()!!
        assertEquals(1f, end.progress, 0f)
        assertEquals(0, end.remainingMinutes)
    }

    @Test
    fun seekPlayback_backwardClampsToZero() = runTest {
        SeekPlaybackUseCase(repository)(-10 * 60 * 60)

        assertEquals(0f, repository.nowPlaying.first()!!.progress, 0f)
    }
}
