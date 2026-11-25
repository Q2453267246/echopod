package com.echopod.core.adaptive

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 验证窗口宽度断点 → 导航形态的映射（AGENTS.md §4）：
 * Compact < 600dp → 底部导航栏；Medium 600–839dp → Medium Rail；Expanded ≥ 840dp → Expanded Rail。
 *
 * `computeWindowSizeClass` 会把任意 dp 宽度归入对应断点桶（与 currentWindowAdaptiveInfo 默认的 BREAKPOINTS_V1 一致），
 * 因此这里直接用真实宽度（含边界值）测试，而不是只测桶的下限。
 */
class NavigationLayoutTest {

    /** 把宽度（dp）换算为 WindowSizeClass，高度对导航形态没有影响，固定为 800dp。 */
    private fun layoutForWidth(widthDp: Float): NavigationLayout =
        navigationLayoutFor(WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp, 800f))

    @Test
    fun `width below 600dp uses bottom bar`() {
        assertEquals(NavigationLayout.BottomBar, layoutForWidth(0f))
        assertEquals(NavigationLayout.BottomBar, layoutForWidth(390f))   // 手机设计稿宽度
        assertEquals(NavigationLayout.BottomBar, layoutForWidth(599f))   // 上边界
    }

    @Test
    fun `width from 600dp to 839dp uses medium rail`() {
        assertEquals(NavigationLayout.MediumRail, layoutForWidth(600f))  // 下边界
        assertEquals(NavigationLayout.MediumRail, layoutForWidth(704f))  // 折叠屏展开竖持
        assertEquals(NavigationLayout.MediumRail, layoutForWidth(839f))  // 上边界
    }

    @Test
    fun `width from 840dp uses expanded rail`() {
        assertEquals(NavigationLayout.ExpandedRail, layoutForWidth(840f))  // 折叠屏展开横持
        assertEquals(NavigationLayout.ExpandedRail, layoutForWidth(1280f)) // 平板
    }
}
