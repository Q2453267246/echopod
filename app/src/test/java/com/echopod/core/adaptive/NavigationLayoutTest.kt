package com.echopod.core.adaptive

import androidx.window.core.layout.WindowSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationLayoutTest {
    @Test
    fun compact() = assertEquals(
        NavigationLayout.BottomBar,
        navigationLayoutFor(WindowSizeClass(minWidthDp = 0, minHeightDp = 0)),
    )

    @Test
    fun medium() = assertEquals(
        NavigationLayout.MediumRail,
        navigationLayoutFor(WindowSizeClass(minWidthDp = 600, minHeightDp = 0)),
    )

    @Test
    fun expanded() = assertEquals(
        NavigationLayout.ExpandedRail,
        navigationLayoutFor(WindowSizeClass(minWidthDp = 840, minHeightDp = 0)),
    )
}
