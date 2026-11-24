package com.echopod.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Quiet Paper type scale (DesignLanguage.dc.html · 字体层级).
// The design uses Inter + Noto Sans SC; on Android the spec allows falling back to
// the system font (Roboto + 思源黑体), so no font files are bundled.

private val EchoPodFontFamily = FontFamily.Default

private fun style(
    size: Int,
    weight: FontWeight,
    lineHeight: Int,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = EchoPodFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

/**
 * | Role              | Design usage            | Spec          |
 * |-------------------|-------------------------|---------------|
 * | headlineMedium    | 页面标题 "EchoPod"       | 28 / 700      |
 * | headlineSmall     | 播放器 / 节目详情标题     | 22 / 700      |
 * | titleLarge        | 区块标题 "继续收听"       | 18 / 600      |
 * | titleMedium       | 内容标题（单集标题）       | 16 / 600      |
 * | titleSmall        | 小标题、搜索框文字         | 14 / 600      |
 * | bodyLarge         | 正文                    | 15 / 400      |
 * | bodyMedium        | 辅助信息                 | 13 / 400      |
 * | bodySmall         | 次级辅助                 | 12 / 400      |
 * | labelLarge        | 按钮                    | 15 / 600      |
 * | labelMedium       | 导航标签、筛选标签         | 12 / 500      |
 * | labelSmall        | 角标                    | 11 / 500      |
 *
 * Selected navigation labels use labelMedium with FontWeight.SemiBold.
 */
internal val EchoPodTypography = Typography(
    displayLarge = style(size = 56, weight = FontWeight.Bold, lineHeight = 62, letterSpacing = -1.5),
    displayMedium = style(size = 44, weight = FontWeight.Bold, lineHeight = 52, letterSpacing = -1.0),
    displaySmall = style(size = 36, weight = FontWeight.Bold, lineHeight = 44, letterSpacing = -0.8),
    headlineLarge = style(size = 32, weight = FontWeight.Bold, lineHeight = 40, letterSpacing = -0.8),
    headlineMedium = style(size = 28, weight = FontWeight.Bold, lineHeight = 36, letterSpacing = -0.6),
    headlineSmall = style(size = 22, weight = FontWeight.Bold, lineHeight = 28),
    titleLarge = style(size = 18, weight = FontWeight.SemiBold, lineHeight = 26),
    titleMedium = style(size = 16, weight = FontWeight.SemiBold, lineHeight = 22),
    titleSmall = style(size = 14, weight = FontWeight.SemiBold, lineHeight = 20),
    bodyLarge = style(size = 15, weight = FontWeight.Normal, lineHeight = 24),
    bodyMedium = style(size = 13, weight = FontWeight.Normal, lineHeight = 20),
    bodySmall = style(size = 12, weight = FontWeight.Normal, lineHeight = 18),
    labelLarge = style(size = 15, weight = FontWeight.SemiBold, lineHeight = 20),
    labelMedium = style(size = 12, weight = FontWeight.Medium, lineHeight = 16),
    labelSmall = style(size = 11, weight = FontWeight.Medium, lineHeight = 14),
)
