package com.echopod.domain.playback.usecase

import com.echopod.domain.playback.model.NowPlaying
import com.echopod.domain.playback.repository.PlaybackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNowPlayingUseCase @Inject constructor(private val repo: PlaybackRepository) {

    operator fun invoke(): Flow<NowPlaying?> = repo.nowPlaying

}