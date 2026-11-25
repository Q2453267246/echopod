package com.echopod.presentation.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.echopod.R
import com.echopod.core.adaptive.NavigationLayout
import com.echopod.core.designsystem.component.EchoPodNavigationBar
import com.echopod.core.designsystem.component.EchoPodNavigationRail
import com.echopod.core.designsystem.component.NavItem
import com.echopod.core.designsystem.component.RailStyle
import com.echopod.core.designsystem.theme.EchoPodTheme

/** Expanded 形态下"纸面"内容区与窗口边缘（及 Rail）之间的间距。 */
private val ExpandedSheetMargin = 8.dp

/** Expanded 形态下"纸面"内容区的圆角。 */
private val ExpandedSheetShape = RoundedCornerShape(24.dp)

/**
 * 自适应应用外壳（无状态）：根据 [navigationLayout] 摆放导航栏与内容区。
 *
 * 外壳**不读取窗口尺寸、不持有 ViewModel**，所有状态都由参数传入，
 * 因此 UI 测试可以直接指定任意导航形态，而不依赖真实设备宽度。
 *
 * 三种形态：
 * - BottomBar：内容在上，底部导航栏在下；
 * - MediumRail：左侧 88dp Rail（container 背景）+ 右侧铺满的内容区；
 * - ExpandedRail：整窗 container 底色 + 左侧 80dp 透明 Rail + 右侧圆角 24dp 的"纸面"内容区。
 *
 * **状态连续性的关键**：[content]（即 NavHost）始终处于同一个调用位置。
 * Rail 与底部导航栏都用 `if` 包裹并放在它前后，Compose 会为 `if` 块单独分组，
 * 所以窗口宽度变化（拖动分屏、折叠/展开）导致形态切换时，NavHost 不会被销毁重建。
 * 切勿改写成 `when (layout) { BottomBar -> { ...content() }; else -> { ...content() } }`，
 * 那样每个分支里的 content() 都是不同实例，切换形态会丢失页面状态。
 *
 * Insets 约定（配合页面中的 windowInsetsTopHeight / windowInsetsBottomHeight 使用）：
 * - BottomBar：底部手势条由导航栏处理，内容区标记为已消费，页面底部不再留白；
 * - Rail 形态：起始侧由 Rail 处理，内容区标记为已消费；
 * - ExpandedRail：纸面本身避开系统栏，并消费上 / 下 / 结束侧 insets，页面内不再重复留白。
 *
 * @param navigationLayout 当前导航形态。
 * @param items 导航项，按显示顺序排列。
 * @param selectedIndex 当前选中项下标；-1 表示没有选中项。
 * @param onItemClick 点击导航项，参数为下标。
 * @param onImportClick 点击 Rail 顶部的导入 FAB（仅 Rail 形态显示）。
 * @param miniPlayer 迷你播放器槽位，悬浮在内容区底部（Step 8 接入，默认为空）。
 * @param content 内容区，通常为 EchoPodNavHost。
 */
@Composable
fun EchoPodAppShell(
    navigationLayout: NavigationLayout,
    items: List<NavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
    miniPlayer: @Composable BoxScope.(NavigationLayout) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val windowBg = when (navigationLayout) {
        NavigationLayout.ExpandedRail -> EchoPodTheme.colors.container
        else -> EchoPodTheme.colors.background
    }
    Row(modifier.fillMaxSize().background(windowBg)) {
        if (navigationLayout != NavigationLayout.BottomBar) {
            EchoPodNavigationRail(
                items, selectedIndex, onItemClick, onImportClick,
                importContentDescription = stringResource(R.string.action_import_podcast),
                style = if (navigationLayout == NavigationLayout.MediumRail) RailStyle.Medium else RailStyle.Expanded,
            )
        }
        Column(Modifier.weight(1f)) {
            Box(Modifier.weight(1f).then(contentAreaModifier(navigationLayout))) {
                content()                         // ← NavHost 始终在同一个调用位置
                miniPlayer(navigationLayout)      // Compact/Medium 下悬浮在内容底部
            }
            if (navigationLayout == NavigationLayout.BottomBar) {
                EchoPodNavigationBar(items, selectedIndex, onItemClick)
            }
        }
    }
}

/**
 * 不同导航形态下内容区的修饰符。只改变 Modifier，不改变组合结构，
 * 因此切换形态时不会影响 [EchoPodAppShell] 中 content 的身份。
 */
@Composable
private fun contentAreaModifier(navigationLayout: NavigationLayout): Modifier = when (navigationLayout) {
    // 底部手势条已由 EchoPodNavigationBar 处理
    NavigationLayout.BottomBar ->
        Modifier.consumeWindowInsets(WindowInsets.navigationBars)

    // 起始侧（左侧系统栏 / 挖孔）已由 Rail 处理；内容区铺满，背景即页面底色
    NavigationLayout.MediumRail ->
        Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))

    // "纸面"：先避开上 / 下 / 结束侧系统栏，再留 8dp 外边距，最后裁成圆角并铺页面底色
    NavigationLayout.ExpandedRail ->
        Modifier
            .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom),
            )
            .padding(top = ExpandedSheetMargin, end = ExpandedSheetMargin, bottom = ExpandedSheetMargin)
            .clip(ExpandedSheetShape)
            .background(EchoPodTheme.colors.background)
}
