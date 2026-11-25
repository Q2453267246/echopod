package com.echopod.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Quiet Paper V1.0 color tokens.
 * Source: EchoPod-设计稿/源文件（Claude设计稿HTML）/DesignLanguage.dc.html
 *
 * UI code must read colors from `EchoPodTheme.colors`, never from
 * `MaterialTheme.colorScheme`. The Material scheme is derived from these tokens
 * only so that M3 internals (ripples, cursors, dialogs) never fall back to the
 * baseline purple palette.
 */
@Immutable
data class EchoPodColors(
    /** 页面背景（暖白纸面 / 深墨）。 */
    val background: Color,
    /** 卡片、迷你播放器。 */
    val card: Color,
    /** 容器：底部导航栏、Rail、搜索框、筛选标签。 */
    val container: Color,
    /** 主文字、选中导航标签。 */
    val textPrimary: Color,
    /** 正文辅助：节目简介段落。 */
    val textBody: Color,
    /** 辅助文字、未选中导航图标与标签。 */
    val textSecondary: Color,
    /** 进度轨道、圆形按钮描边。 */
    val outline: Color,
    /** 分割线、卡片描边。 */
    val divider: Color,
    /** 品牌 · 雾青：进度条、播放按钮、链接。 */
    val brand: Color,
    /** 品牌色上的图标 / 文字。 */
    val onBrand: Color,
    /** 品牌容器：导航选中指示器、"已订阅"按钮。 */
    val brandContainer: Color,
    /** 品牌容器上的图标 / 文字：导航选中图标。 */
    val onBrandContainer: Color,
    /** 墨色强调按钮：Rail 顶部导入 FAB、首页"导入"按钮、选中筛选标签。 */
    val inkButton: Color,
    /** 墨色强调按钮上的图标 / 文字。 */
    val onInkButton: Color,
    /** 更新提示，仅用于圆点。 */
    val updateDot: Color,
    val isDark: Boolean,
)

val EchoPodLightColors = EchoPodColors(
    background = Color(0xFFF7F5F0),
    card = Color(0xFFFFFFFF),
    container = Color(0xFFEFECE5),
    textPrimary = Color(0xFF1C1B19),
    textBody = Color(0xFF4A4741),
    textSecondary = Color(0xFF6B675F),
    outline = Color(0xFFE3DFD6),
    divider = Color(0xFFE3DFD6),
    brand = Color(0xFF2E6B66),
    onBrand = Color(0xFFFFFFFF),
    brandContainer = Color(0xFFD6E7E3),
    onBrandContainer = Color(0xFF123A37),
    inkButton = Color(0xFF1C1B19),
    onInkButton = Color(0xFFF7F5F0),
    updateDot = Color(0xFFE4893F),
    isDark = false,
)

val EchoPodDarkColors = EchoPodColors(
    background = Color(0xFF131412),
    card = Color(0xFF22231F),
    container = Color(0xFF2A2B27),
    textPrimary = Color(0xFFECEAE4),
    textBody = Color(0xFFC9C5BC),
    textSecondary = Color(0xFFA3A098),
    outline = Color(0xFF3A3B36),
    divider = Color(0xFF33342F),
    brand = Color(0xFF86C6BD),
    onBrand = Color(0xFF0F2A27),
    brandContainer = Color(0xFF1F3B38),
    onBrandContainer = Color(0xFFBFE3DD),
    inkButton = Color(0xFFECEAE4),
    onInkButton = Color(0xFF131412),
    updateDot = Color(0xFFF0A060),
    isDark = true,
)

internal val LocalEchoPodColors = staticCompositionLocalOf { EchoPodLightColors }

/** Derives the Material scheme from Quiet Paper tokens (single source of truth). */
internal fun EchoPodColors.toMaterialColorScheme(): ColorScheme {
    val scheme = if (isDark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandContainer,
        onPrimaryContainer = onBrandContainer,
        secondary = brand,
        onSecondary = onBrand,
        secondaryContainer = brandContainer,
        onSecondaryContainer = onBrandContainer,
        tertiary = updateDot,
        onTertiary = background,
        tertiaryContainer = updateDot,
        onTertiaryContainer = background,
        background = background,
        onBackground = textPrimary,
        surface = background,
        onSurface = textPrimary,
        surfaceVariant = container,
        onSurfaceVariant = textSecondary,
        surfaceTint = Color.Transparent,
        inverseSurface = inkButton,
        inverseOnSurface = onInkButton,
        outline = outline,
        outlineVariant = divider,
        surfaceBright = card,
        surfaceDim = if (isDark) background else container,
        surfaceContainerLowest = if (isDark) background else card,
        surfaceContainerLow = card,
        surfaceContainer = container,
        surfaceContainerHigh = container,
        surfaceContainerHighest = container,
    )
}
