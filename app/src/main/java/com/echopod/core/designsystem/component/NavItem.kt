package com.echopod.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 导航项的纯 UI 数据。
 *
 * 设计系统组件只认识"文字 + 图标"，不认识路由或页面，
 * 由 presentation 层把 TopLevelDestination 映射成 NavItem 再传入。
 *
 * @property label 已解析好的文案（如"首页"），同时作为无障碍朗读标签。
 * @property iconRes 24dp 线性图标的 Vector Drawable 资源 ID。
 */
@Immutable
data class NavItem(
    val label: String,
    @param:DrawableRes val iconRes: Int,
)

/** 设计稿中导航图标的显示尺寸：24 视口的图标按 22dp 绘制。 */
internal val NavIconSize = 22.dp

/**
 * 导航项图标。
 *
 * contentDescription 传 null：导航项的文字标签已经提供无障碍朗读，图标不重复朗读。
 * 颜色由 NavigationBarItem / NavigationRailItem 的 colors 通过 LocalContentColor 提供。
 */
@Composable
internal fun NavItemIcon(item: NavItem) {
    Icon(
        painter = painterResource(item.iconRes),
        contentDescription = null,
        modifier = Modifier.size(NavIconSize),
    )
}

/**
 * 导航项文字标签：12sp，选中 SemiBold(600)，未选中 Medium(500)。
 * 颜色同样由导航项的 colors 提供，这里不指定颜色。
 */
@Composable
internal fun NavItemLabel(item: NavItem, selected: Boolean) {
    Text(
        text = item.label,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        ),
        maxLines = 1,
    )
}
