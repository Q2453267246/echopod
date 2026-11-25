package com.echopod.domain.playback.usecase

import com.echopod.domain.playback.repository.PlaybackRepository
import javax.inject.Inject

class SeekPlaybackUseCase @Inject constructor(private val repo: PlaybackRepository) {
    suspend operator fun invoke(seconds: Int) = repo.seekBy(seconds)
}