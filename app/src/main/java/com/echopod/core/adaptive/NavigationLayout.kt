package com.echopod.core.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass


enum class NavigationLayout { BottomBar, MediumRail, ExpandedRail }

fun navigationLayoutFor(sizeClass: WindowSizeClass): NavigationLayout = when {
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> NavigationLayout.ExpandedRail
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> NavigationLayout.MediumRail
    else -> NavigationLayout.BottomBar
}

@androidx.compose.runtime.Composable
fun rememberNavigationLayout(): NavigationLayout {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(sizeClass) { navigationLayoutFor(sizeClass) }
}