package com.echopod.di

import com.echopod.data.playback.FakePlaybackRepository
import com.echopod.domain.playback.repository.PlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {
    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: FakePlaybackRepository): PlaybackRepository
}