package com.echopod.domain.playback.model

/**
 * 播放器数据
 * @param episodeTitle 节目标题
 * @param podcastTitle 播客标题
 * @param speed 播放速度
 * @param progress 进度，0f..1f
 * @param remainingMinutes 剩余分钟数，>= 0
 * @param isPlaying 是否正在播放
 */
data class NowPlaying(
    val episodeTitle: String,
    val podcastTitle: String,
    val speed: Float,
    val progress: Float,          // 0f..1f
    val remainingMinutes: Int,
    val isPlaying: Boolean,
)