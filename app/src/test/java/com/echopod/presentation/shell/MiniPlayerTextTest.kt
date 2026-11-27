package com.echopod.presentation.shell

import org.junit.Assert.assertEquals
import org.junit.Test

class MiniPlayerTextTest {


    @Test
    fun formatSpeed_trimsTrailingZeros() {
        assertEquals("1x", formatSpeed(1.0f))
        assertEquals("1.2x", formatSpeed(1.2f))
        assertEquals("1.25x", formatSpeed(1.25f))
        assertEquals("0.75x", formatSpeed(0.75f))
        assertEquals("2x", formatSpeed(2.0f))
    }

    @Test
    fun subtitle_withoutRemaining() {
        assertEquals("夜航电台 · 1.2x", miniPlayerSubtitle("夜航电台", 1.2f, remaining = null))
    }

    @Test
    fun subtitle_withRemaining() {
        assertEquals(
            "夜航电台 · 1.2x · 剩余 23 分",
            miniPlayerSubtitle("夜航电台", 1.2f, remaining = "剩余 23 分"),
        )
    }

}