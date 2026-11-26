package com.echopod.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.echopod.core.adaptive.NavigationLayout
import com.echopod.presentation.feature.discover.DiscoverScreen
import com.echopod.presentation.feature.home.HomeScreen
import com.echopod.presentation.feature.profile.ProfileScreen
import com.echopod.presentation.feature.subscriptions.SubscriptionsScreen
import com.echopod.presentation.shell.miniPlayerStyleFor

/** Quiet Paper 动效规范：按钮 / 选中态 200ms。一级 Tab 之间只做淡入淡出。 */
private const val TopLevelFadeMillis = 200

/**
 * 应用导航图。起始页为首页。
 *
 * 一级 Tab 之间用 200ms 淡入淡出，而不是左右滑动：
 * Tab 是平级关系，滑动会暗示前进/后退的层级。
 * 以后的二级页面（节目详情、单集详情等）在这里继续添加，并按需使用 300ms 的页面切换动画。
 *
 * 页面不自己读窗口尺寸：左右边距、迷你播放器留白、是否显示首页"导入"按钮都由这里按导航形态算好再传入。
 *
 * @param onImportClick 首页标题栏"导入"按钮的点击（仅 Compact 显示），与 Rail FAB 发同一个事件。
 * @param hasMiniPlayer 当前是否显示悬浮迷你播放器；为 true 时列表底部额外留出播放器高度。
 */
@Composable
fun EchoPodNavHost(
    appState: EchoPodAppState,
    onImportClick: () -> Unit,
    hasMiniPlayer: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val layout = appState.navigationLayout
    val horizontalPadding = when (layout) {
        NavigationLayout.BottomBar -> 16.dp
        else -> 24.dp
    }

    // 迷你播放器占位高度与 MiniPlayerStyle 同源；Expanded 返回 null → 0dp
    val bottomExtraPadding =
        if (hasMiniPlayer) miniPlayerStyleFor(layout)?.reservedHeight ?: 0.dp else 0.dp
    NavHost(
        navController = appState.navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { fadeIn(tween(TopLevelFadeMillis)) },
        exitTransition = { fadeOut(tween(TopLevelFadeMillis)) },
        popEnterTransition = { fadeIn(tween(TopLevelFadeMillis)) },
        popExitTransition = { fadeOut(tween(TopLevelFadeMillis)) },
    ) {
        composable<HomeRoute> {
            HomeScreen(
                showImportButton = layout == NavigationLayout.BottomBar,
                onImportClick = onImportClick,
                horizontalPadding = horizontalPadding,
                bottomExtraPadding = bottomExtraPadding,
            )
        }
        composable<DiscoverRoute> {
            DiscoverScreen(horizontalPadding = horizontalPadding, bottomExtraPadding = bottomExtraPadding)
        }
        composable<SubscriptionsRoute> {
            SubscriptionsScreen(horizontalPadding = horizontalPadding, bottomExtraPadding = bottomExtraPadding)
        }
        composable<ProfileRoute> {
            ProfileScreen(horizontalPadding = horizontalPadding, bottomExtraPadding = bottomExtraPadding)
        }
    }
}
