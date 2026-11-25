package com.echopod.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme

// ---- M3 NavigationRail（material3 1.3.x）内部自带的间距 ----
// 源码：Column(padding(vertical = 4.dp), verticalArrangement = spacedBy(4.dp))，
// header 之后再插入一个 8dp 的 Spacer。
// 设计稿给的是最终视觉间距，因此下面用"设计值 - M3 内置值"得到需要额外补的 padding。
// 升级 material3 后若 Rail 间距发生变化，请先核对这几个值。
private val M3RailColumnPadding = 4.dp
private val M3RailItemSpacing = 4.dp
private val M3RailHeaderToContent = M3RailItemSpacing + 8.dp + M3RailItemSpacing // = 16dp

/** 设计稿中 Rail 导航项高度（M3 默认 56dp）。 */
private val RailItemHeight = 60.dp

/** 导入 FAB：56×56dp，圆角 16dp。 */
private val ImportFabShape = RoundedCornerShape(16.dp)

/**
 * Medium / Expanded 侧边导航栏：顶部为"导入播客" FAB，下方为一级导航项。
 *
 * 设计稿：02-首页-竖向.png（Medium）、03-首页-横向.png（Expanded）。
 * 两种形态的尺寸差异见 [RailStyle]。
 *
 * 实现要点：
 * - 背景画在外层 Modifier 上，内部 NavigationRail 透明。这样外层可以加 verticalScroll：
 *   横屏手机等矮窗口（高度 < 480dp）放不下 FAB + 4 项时可以滚动，而背景仍铺满整列；
 * - windowInsets 在 M3 默认（状态栏/导航栏 + 起始侧）基础上，并入起始侧的挖孔区域，
 *   避免横屏时摄像头挖孔遮住 Rail。
 *
 * @param items 导航项，按显示顺序排列。
 * @param selectedIndex 当前选中项下标；为 -1 时没有选中项。
 * @param onItemClick 点击回调，参数为被点击项的下标。
 * @param onImportClick 点击顶部导入 FAB。
 * @param importContentDescription FAB 的无障碍描述（"导入播客"），由调用方从字符串资源传入。
 * @param style Medium 或 Expanded 样式。
 */
@Composable
fun EchoPodNavigationRail(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    onImportClick: () -> Unit,
    importContentDescription: String,
    style: RailStyle,
    modifier: Modifier = Modifier,
) {
    val railBackground = if (style.transparent) Color.Transparent else EchoPodTheme.colors.container

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .width(style.width)
            .background(railBackground)
            .verticalScroll(rememberScrollState()),
        containerColor = Color.Transparent,
        contentColor = EchoPodTheme.colors.textSecondary,
        windowInsets = NavigationRailDefaults.windowInsets
            .union(WindowInsets.displayCutout.only(WindowInsetsSides.Start)),
        header = {
            ImportFab(
                onClick = onImportClick,
                contentDescription = importContentDescription,
                modifier = Modifier.padding(
                    top = style.topPadding - M3RailColumnPadding,
                    bottom = style.fabToFirstItem - M3RailHeaderToContent,
                ),
            )
        },
    ) {
        val colors = echoPodRailItemColors()
        // 每项上下各补一半的差值，使相邻两项之间的总距离等于设计值
        val itemPadding = (style.itemSpacing - M3RailItemSpacing) / 2
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationRailItem(
                selected = selected,
                onClick = { onItemClick(index) },
                icon = { NavItemIcon(item) },
                label = { NavItemLabel(item, selected) },
                colors = colors,
                modifier = Modifier
                    .padding(vertical = itemPadding)
                    .height(RailItemHeight),
            )
        }
    }
}

/**
 * Rail 顶部的"导入播客"按钮：墨色背景、无阴影（设计稿为扁平样式）。
 */
@Composable
private fun ImportFab(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = ImportFabShape,
        containerColor = EchoPodTheme.colors.inkButton,
        contentColor = EchoPodTheme.colors.onInkButton,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = contentDescription,
            modifier = Modifier.size(NavIconSize), // 设计稿中 "+" 为 22dp
        )
    }
}

/**
 * Rail 导航项配色，与底部导航栏保持一致，全部取自 Quiet Paper 令牌。
 */
@Composable
internal fun echoPodRailItemColors(): NavigationRailItemColors {
    val c = EchoPodTheme.colors
    return NavigationRailItemDefaults.colors(
        selectedIconColor = c.onBrandContainer,
        selectedTextColor = c.textPrimary,
        indicatorColor = c.brandContainer,
        unselectedIconColor = c.textSecondary,
        unselectedTextColor = c.textSecondary,
    )
}

// ---- 预览：与 02-首页-竖向.png / 03-首页-横向.png 对比 ----

@Preview(name = "Rail · Medium · Light", heightDp = 840)
@Composable
private fun MediumRailLightPreview() {
    EchoPodTheme(darkTheme = false) {
        EchoPodNavigationRail(
            items = PreviewNavItems,
            selectedIndex = 0,
            onItemClick = {},
            onImportClick = {},
            importContentDescription = "导入播客",
            style = RailStyle.Medium,
        )
    }
}

@Preview(name = "Rail · Medium · Dark", heightDp = 840)
@Composable
private fun MediumRailDarkPreview() {
    EchoPodTheme(darkTheme = true) {
        EchoPodNavigationRail(
            items = PreviewNavItems,
            selectedIndex = 0,
            onItemClick = {},
            onImportClick = {},
            importContentDescription = "导入播客",
            style = RailStyle.Medium,
        )
    }
}

/** Expanded Rail 本身透明，预览时手动铺上整窗底色（container）。 */
@Preview(name = "Rail · Expanded · Light", heightDp = 704)
@Composable
private fun ExpandedRailLightPreview() {
    EchoPodTheme(darkTheme = false) {
        EchoPodNavigationRail(
            items = PreviewNavItems,
            selectedIndex = 2,
            onItemClick = {},
            onImportClick = {},
            importContentDescription = "导入播客",
            style = RailStyle.Expanded,
            modifier = Modifier.background(EchoPodTheme.colors.container),
        )
    }
}
