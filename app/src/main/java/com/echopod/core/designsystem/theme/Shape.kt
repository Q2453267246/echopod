package com.echopod.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Quiet Paper corner radii (DesignLanguage.dc.html · 间距 · 圆角 · 动效):
// 8 小封面 · 12 封面 · 16 迷你播放器 / FAB · 20–24 卡片 · 全圆 按钮
//
// M3 components pick these up by default:
// - FloatingActionButton  -> large       (Rail FAB is 16dp, passed explicitly)
// - Card / ElevatedCard   -> medium
// - Dialog / BottomSheet  -> extraLarge
// - Button / NavigationBar indicator -> full (CircleShape), not part of Shapes
internal val EchoPodShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
