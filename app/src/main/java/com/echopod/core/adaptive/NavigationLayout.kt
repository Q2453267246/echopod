package com.echopod.core.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowSizeClass

/**
 * 应用外壳的导航形态。
 *
 * 只由「窗口宽度」决定，不看设备型号、也不看横竖屏：
 * 横屏手机只要宽度 ≥ 600dp 也会切到 Rail，这是期望行为。
 */
enum class NavigationLayout {
    /** Compact（< 600dp）：手机、折叠屏外屏 → 底部导航栏。 */
    BottomBar,

    /** Medium（600–839dp）：折叠屏展开竖持 → 88dp 侧边 Rail，Rail 自带背景色。 */
    MediumRail,

    /** Expanded（≥ 840dp）：折叠屏展开横持、平板 → 80dp 透明 Rail + 圆角"纸面"内容区。 */
    ExpandedRail,
}

/**
 * 窗口尺寸类别 → 导航形态的映射。
 *
 * 写成纯函数，只依赖 window-core 的 [WindowSizeClass]（纯 Kotlin/JVM 库），
 * 因此可以直接在 JVM 单元测试中验证断点，无需设备。
 */
fun navigationLayoutFor(sizeClass: WindowSizeClass): NavigationLayout = when {
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> NavigationLayout.ExpandedRail
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> NavigationLayout.MediumRail
    else -> NavigationLayout.BottomBar
}

/**
 * 读取当前窗口的尺寸类别并换算为 [NavigationLayout]。
 *
 * 折叠/展开、旋转、拖动分屏窗口时，`currentWindowAdaptiveInfo()` 会发出新值并触发重组，
 * 导航形态随之自动切换。该值是纯派生值，无需保存到 SavedState。
 */
@Composable
fun rememberNavigationLayout(): NavigationLayout {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(sizeClass) { navigationLayoutFor(sizeClass) }
}
