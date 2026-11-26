package com.echopod.presentation.shell

import kotlin.math.roundToInt

/** 播放倍速显示：1.0 → "1x"，1.2 → "1.2x"，1.25 → "1.25x"，0.75 → "0.75x"。与系统区域无关。 */
internal fun formatSpeed(speed:Float): String {
    val hundredths = (speed * 100).roundToInt()
    val whole = hundredths / 100
    val fraction = hundredths % 100
    return when {
        fraction == 0 -> "${whole}x"
        fraction % 10 == 0 -> "$whole.${fraction / 10}x"
        else -> "$whole.${fraction.toString().padStart(2, '0')}x"
    }
}

/**
 * 副标题："夜航电台 · 1.2x"；传入 [remaining] 时追加为 "夜航电台 · 1.2x · 剩余 23 分"。 Expanded 状态，会显示剩余时间，Compact 时候，只需要显示名字和倍速即可
 *
 * @param remaining 已经本地化好的"剩余 N 分"文案（由 Composable 用 stringResource 生成）；null 表示不显示。
 */
internal fun miniPlayerSubtitle(podcastTitle: String, speed: Float, remaining: String?): String =
    listOfNotNull(podcastTitle, formatSpeed(speed), remaining).joinToString(" · ")