package com.echopod.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Quiet Paper theme. Dynamic color is intentionally not supported: the brand
 * accent (Mist Teal) is fixed and cover art is the main source of color.
 */
@Composable
fun EchoPodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EchoPodDarkColors else EchoPodLightColors,
        typography = EchoPodTypography,
        shapes = EchoPodShapes,
        content = content,
    )
}
