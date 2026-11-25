package com.echopod.domain.playback.usecase

import com.echopod.domain.playback.repository.PlaybackRepository
import javax.inject.Inject

class TogglePlaybackUseCase @Inject constructor(private val repo: PlaybackRepository) {
    suspend operator fun invoke() = repo.togglePlayPause()
}