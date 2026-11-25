package com.echopod.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 侧边导航栏（Rail）的两种样式，数值均为设计稿中的**最终视觉值**。
 * 与 M3 NavigationRail 内部默认间距的换算在 EchoPodNavigationRail 中完成。
 *
 * @property width Rail 宽度。
 * @property topPadding Rail 顶部（不含状态栏 inset）到导入 FAB 顶部的距离。
 * @property fabToFirstItem 导入 FAB 底部到第一个导航项顶部的距离。
 * @property itemSpacing 相邻导航项之间的距离。
 * @property transparent Rail 是否无背景（透出整窗底色）。
 */
enum class RailStyle(
    val width: Dp,
    val topPadding: Dp,
    val fabToFirstItem: Dp,
    val itemSpacing: Dp,
    val transparent: Boolean,
) {
    /**
     * Medium（600–839dp，展开竖持）。设计稿：02-首页-竖向.png，FoldHome.dc.html
     * 宽 88dp，padding-top 28dp，FAB margin-bottom 20dp + gap 12dp，Rail 自带 container 背景。
     */
    Medium(
        width = 88.dp,
        topPadding = 28.dp,
        fabToFirstItem = 32.dp,
        itemSpacing = 12.dp,
        transparent = false,
    ),

    /**
     * Expanded（≥ 840dp，展开横持 / 平板）。设计稿：03-首页-横向.png，FoldHomeLandscape.dc.html
     * 宽 80dp，padding-top 20dp，FAB margin-bottom 16dp + gap 8dp，Rail 透明，整窗底色为 container。
     */
    Expanded(
        width = 80.dp,
        topPadding = 20.dp,
        fabToFirstItem = 24.dp,
        itemSpacing = 8.dp,
        transparent = true,
    ),
}
