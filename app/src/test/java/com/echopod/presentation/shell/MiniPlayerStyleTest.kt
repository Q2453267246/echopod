package com.echopod.presentation.shell

import androidx.compose.ui.unit.dp
import com.echopod.core.adaptive.NavigationLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiniPlayerStyleTest {

    @Test
    fun styleFor_mapsEachNavigationLayout() {
        assertEquals(MiniPlayerStyle.Compact, miniPlayerStyleFor(NavigationLayout.BottomBar))
        assertEquals(MiniPlayerStyle.Medium, miniPlayerStyleFor(NavigationLayout.MediumRail))
        assertNull(miniPlayerStyleFor(NavigationLayout.ExpandedRail))
    }

    @Test
    fun reservedHeight_matchesDesign() {
        assertEquals(72.dp, MiniPlayerStyle.Compact.reservedHeight) // 64 + 8
        assertEquals(84.dp, MiniPlayerStyle.Medium.reservedHeight)  // 68 + 16
    }

}