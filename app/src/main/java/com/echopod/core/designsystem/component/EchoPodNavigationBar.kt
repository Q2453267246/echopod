package com.echopod.core.designsystem.component

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme

/**
 * Compact（< 600dp）底部导航栏。
 *
 * 设计稿：PNG/01-手机/01-首页.png，HTML：Main.dc.html
 * - 容器高 80dp（M3 NavigationBar 默认值），背景 container 色，4 项等分；
 * - 选中指示器 64×32dp 全圆角（M3 默认值），brandContainer 色；
 * - 图标 22dp，文字 12sp。
 *
 * 系统手势条 inset 由 NavigationBar 默认的 windowInsets 处理，
 * 调用方**不要**再额外加 navigationBarsPadding()。
 *
 * @param items 导航项，按显示顺序排列。
 * @param selectedIndex 当前选中项下标；为 -1 时没有选中项（例如处于非一级页面的路由）。
 * @param onItemClick 点击回调，参数为被点击项的下标。
 */
@Composable
fun EchoPodNavigationBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = EchoPodTheme.colors.container,
        contentColor = EchoPodTheme.colors.textSecondary,
        // 关闭 tonal elevation，避免 M3 在背景上叠加一层色调
        tonalElevation = 0.dp,
    ) {
        val colors = echoPodNavItemColors()
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(index) },
                icon = { NavItemIcon(item) },
                label = { NavItemLabel(item, selected) },
                colors = colors,
            )
        }
    }
}

/**
 * 底部导航项配色，全部取自 Quiet Paper 令牌（不使用 MaterialTheme.colorScheme）：
 * - 选中：图标 onBrandContainer，文字 textPrimary，指示器 brandContainer；
 * - 未选中：图标与文字 textSecondary。
 */
@Composable
internal fun echoPodNavItemColors(): NavigationBarItemColors {
    val c = EchoPodTheme.colors
    return NavigationBarItemDefaults.colors(
        selectedIconColor = c.onBrandContainer,
        selectedTextColor = c.textPrimary,
        indicatorColor = c.brandContainer,
        unselectedIconColor = c.textSecondary,
        unselectedTextColor = c.textSecondary,
    )
}

// ---- 预览：与 01-首页.png / 14-首页-深色.png 对比 ----

/** 预览用的四个一级导航项。 */
internal val PreviewNavItems = listOf(
    NavItem("首页", R.drawable.ic_nav_home),
    NavItem("发现", R.drawable.ic_nav_discover),
    NavItem("订阅", R.drawable.ic_nav_subscriptions),
    NavItem("我的", R.drawable.ic_nav_profile),
)

@Preview(name = "NavigationBar · Light", widthDp = 390)
@Composable
private fun EchoPodNavigationBarLightPreview() {
    EchoPodTheme(darkTheme = false) {
        EchoPodNavigationBar(items = PreviewNavItems, selectedIndex = 0, onItemClick = {})
    }
}

@Preview(name = "NavigationBar · Dark", widthDp = 390)
@Composable
private fun EchoPodNavigationBarDarkPreview() {
    EchoPodTheme(darkTheme = true) {
        EchoPodNavigationBar(items = PreviewNavItems, selectedIndex = 0, onItemClick = {})
    }
}
