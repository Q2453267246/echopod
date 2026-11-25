package com.echopod.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.echopod.presentation.feature.discover.DiscoverScreen
import com.echopod.presentation.feature.home.HomeScreen
import com.echopod.presentation.feature.profile.ProfileScreen
import com.echopod.presentation.feature.subscriptions.SubscriptionsScreen

/** Quiet Paper 动效规范：按钮 / 选中态 200ms。一级 Tab 之间只做淡入淡出。 */
private const val TopLevelFadeMillis = 200

/**
 * 应用导航图。起始页为首页。
 *
 * 一级 Tab 之间用 200ms 淡入淡出，而不是左右滑动：
 * Tab 是平级关系，滑动会暗示前进/后退的层级。
 * 以后的二级页面（节目详情、单集详情等）在这里继续添加，并按需使用 300ms 的页面切换动画。
 */
@Composable
fun EchoPodNavHost(
    appState: EchoPodAppState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = appState.navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { fadeIn(tween(TopLevelFadeMillis)) },
        exitTransition = { fadeOut(tween(TopLevelFadeMillis)) },
        popEnterTransition = { fadeIn(tween(TopLevelFadeMillis)) },
        popExitTransition = { fadeOut(tween(TopLevelFadeMillis)) },
    ) {
        composable<HomeRoute> { HomeScreen() }
        composable<DiscoverRoute> { DiscoverScreen() }
        composable<SubscriptionsRoute> { SubscriptionsScreen() }
        composable<ProfileRoute> { ProfileScreen() }
    }
}
