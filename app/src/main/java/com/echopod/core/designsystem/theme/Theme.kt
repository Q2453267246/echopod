package com.echopod.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * Quiet Paper theme. Dynamic color is intentionally not supported: the brand
 * accent (Mist Teal) is fixed and cover art is the main source of color.
 */
@Composable
fun EchoPodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) EchoPodDarkColors else EchoPodLightColors
    val materialColors = remember(colors) { colors.toMaterialColorScheme() }
    CompositionLocalProvider(LocalEchoPodColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = EchoPodTypography,
            shapes = EchoPodShapes,
            content = content,
        )
    }
}

/** Access point for Quiet Paper tokens: `EchoPodTheme.colors.brand`. */
object EchoPodTheme {
    val colors: EchoPodColors
        @Composable
        @ReadOnlyComposable
        get() = LocalEchoPodColors.current
}
