package com.echopod.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Quiet Paper V1.0 color tokens.
// Source: EchoPod-设计稿/源文件（Claude设计稿HTML）/DesignLanguage.dc.html

// ---- Light ----
internal val PaperLight = Color(0xFFF7F5F0)          // 背景
internal val CardLight = Color(0xFFFFFFFF)           // 卡片
internal val ContainerLight = Color(0xFFEFECE5)      // 容器 / 导航栏 / 搜索框
internal val InkLight = Color(0xFF1C1B19)            // 主文字
internal val InkSoftLight = Color(0xFF4A4741)        // 正文辅助（简介段落）
internal val InkMutedLight = Color(0xFF6B675F)       // 辅助文字 / 未选中图标
internal val OutlineLight = Color(0xFFE3DFD6)        // 描边 / 进度轨道
internal val BrandLight = Color(0xFF2E6B66)          // 品牌 · 雾青
internal val OnBrandLight = Color(0xFFFFFFFF)
internal val BrandContainerLight = Color(0xFFD6E7E3) // 品牌容器 / 导航选中指示器
internal val OnBrandContainerLight = Color(0xFF123A37)
internal val UpdateDotLight = Color(0xFFE4893F)      // 更新提示（仅圆点）

// ---- Dark ----
internal val PaperDark = Color(0xFF131412)
internal val CardDark = Color(0xFF22231F)
internal val ContainerDark = Color(0xFF2A2B27)
internal val InkDark = Color(0xFFECEAE4)
internal val InkSoftDark = Color(0xFFC9C5BC)
internal val InkMutedDark = Color(0xFFA3A098)
internal val OutlineDark = Color(0xFF33342F)         // 分割线
internal val OutlineStrongDark = Color(0xFF3A3B36)   // 进度轨道 / 圆形按钮描边
internal val BrandDark = Color(0xFF86C6BD)
internal val OnBrandDark = Color(0xFF0F2A27)
internal val BrandContainerDark = Color(0xFF1F3B38)
internal val OnBrandContainerDark = Color(0xFFBFE3DD)
internal val UpdateDotDark = Color(0xFFF0A060)

/**
 * Light scheme. Every container/surface role is set explicitly so that no
 * Material baseline (purple) color leaks into M3 components.
 *
 * Role mapping used by components:
 * - surfaceContainer      -> NavigationBar / NavigationRail background
 * - secondaryContainer    -> navigation selected indicator
 * - inverseSurface        -> Rail "+" import FAB and the dark "导入" button
 */
internal val EchoPodLightColors: ColorScheme = lightColorScheme(
    primary = BrandLight,
    onPrimary = OnBrandLight,
    primaryContainer = BrandContainerLight,
    onPrimaryContainer = OnBrandContainerLight,
    inversePrimary = BrandDark,
    secondary = BrandLight,
    onSecondary = OnBrandLight,
    secondaryContainer = BrandContainerLight,
    onSecondaryContainer = OnBrandContainerLight,
    tertiary = UpdateDotLight,
    onTertiary = CardLight,
    tertiaryContainer = UpdateDotLight,
    onTertiaryContainer = CardLight,
    background = PaperLight,
    onBackground = InkLight,
    surface = PaperLight,
    onSurface = InkLight,
    surfaceVariant = ContainerLight,
    onSurfaceVariant = InkMutedLight,
    surfaceTint = Color.Transparent,
    inverseSurface = InkLight,
    inverseOnSurface = PaperLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    scrim = Color.Black,
    surfaceBright = CardLight,
    surfaceDim = ContainerLight,
    surfaceContainerLowest = CardLight,
    surfaceContainerLow = CardLight,
    surfaceContainer = ContainerLight,
    surfaceContainerHigh = ContainerLight,
    surfaceContainerHighest = ContainerLight,
)

internal val EchoPodDarkColors: ColorScheme = darkColorScheme(
    primary = BrandDark,
    onPrimary = OnBrandDark,
    primaryContainer = BrandContainerDark,
    onPrimaryContainer = OnBrandContainerDark,
    inversePrimary = BrandLight,
    secondary = BrandDark,
    onSecondary = OnBrandDark,
    secondaryContainer = BrandContainerDark,
    onSecondaryContainer = OnBrandContainerDark,
    tertiary = UpdateDotDark,
    onTertiary = PaperDark,
    tertiaryContainer = UpdateDotDark,
    onTertiaryContainer = PaperDark,
    background = PaperDark,
    onBackground = InkDark,
    surface = PaperDark,
    onSurface = InkDark,
    surfaceVariant = ContainerDark,
    onSurfaceVariant = InkMutedDark,
    surfaceTint = Color.Transparent,
    inverseSurface = InkDark,
    inverseOnSurface = PaperDark,
    outline = OutlineStrongDark,
    outlineVariant = OutlineDark,
    scrim = Color.Black,
    surfaceBright = CardDark,
    surfaceDim = PaperDark,
    surfaceContainerLowest = PaperDark,
    surfaceContainerLow = CardDark,
    surfaceContainer = ContainerDark,
    surfaceContainerHigh = ContainerDark,
    surfaceContainerHighest = ContainerDark,
)
