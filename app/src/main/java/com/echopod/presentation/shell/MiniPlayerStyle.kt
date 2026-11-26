package com.echopod.presentation.shell

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.echopod.core.adaptive.NavigationLayout

/**
 * 迷你播放器的两种尺寸规格（数值取自 Main.dc.html / FoldHome.dc.html）。
 *
 * @property height 卡片高度。
 * @property cornerRadius 卡片圆角。
 * @property coverSize 封面边长。
 * @property paddingStart 卡片左内边距。
 * @property paddingEnd 卡片右内边距。
 * @property spacing 子项间距。
 * @property progressInset 底部进度线左右缩进。
 * @property horizontalMargin 卡片与内容区左右边缘的距离。
 * @property bottomMargin 卡片与内容区底边（Compact 为导航栏顶部）的距离。
 * @property showSkipButtons 是否显示快退 15 / 快进 30。
 * @property showRemaining 副标题是否显示"剩余 N 分"。
 * @property filledPlayButton 播放/暂停是否为墨色圆形按钮。
 */
enum class MiniPlayerStyle(

    val height: Dp,
    val cornerRadius: Dp,
    val coverSize: Dp,
    val paddingStart: Dp,
    val paddingEnd: Dp,
    val spacing: Dp,
    val progressInset: Dp,
    val horizontalMargin: Dp,
    val bottomMargin: Dp,
    val showSkipButtons: Boolean,
    val showRemaining: Boolean,
    val filledPlayButton: Boolean

) {
    Compact(
        height = 64.dp,
        cornerRadius = 16.dp,
        coverSize = 44.dp,
        paddingStart = 10.dp,
        paddingEnd = 4.dp,
        spacing = 10.dp,
        progressInset = 12.dp,
        horizontalMargin = 8.dp,
        bottomMargin = 8.dp,
        showSkipButtons = false,
        showRemaining = false,
        filledPlayButton = false,
    ),
    Medium(
        height = 68.dp,
        cornerRadius = 18.dp,
        coverSize = 46.dp,
        paddingStart = 12.dp,
        paddingEnd = 8.dp,
        spacing = 12.dp,
        progressInset = 14.dp,
        horizontalMargin = 24.dp,
        bottomMargin = 16.dp,
        showSkipButtons = true,
        showRemaining = true,
        filledPlayButton = true,
    );

    /** 页面列表底部需要额外留出的高度：卡片高度 + 底边距（Compact 72dp，Medium 84dp）。 */
    val reservedHeight: Dp
        get() = height + bottomMargin
}


/**
 * 导航形态 → 迷你播放器规格。Expanded 返回 null：外壳不渲染，由双栏详情栏负责。
 *
 * 纯函数，可直接在 JVM 单测中验证。
 */
fun miniPlayerStyleFor(layout: NavigationLayout): MiniPlayerStyle? = when (layout) {
    NavigationLayout.BottomBar -> MiniPlayerStyle.Compact
    NavigationLayout.MediumRail -> MiniPlayerStyle.Medium
    NavigationLayout.ExpandedRail -> null
}