package com.echopod.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.echopod.core.adaptive.NavigationLayout
import com.echopod.core.adaptive.rememberNavigationLayout

/**
 * 应用外壳的「导航 UI 逻辑」状态持有者。
 *
 * 为什么不是 ViewModel：这里只有纯 UI 逻辑（当前在哪个 Tab、怎么切 Tab、用哪种导航形态），
 * 没有业务数据。NavController 本身就是导航状态的唯一真相，并且会随 rememberSaveable
 * 在配置变更（折叠/展开、旋转）后自动恢复；在 ViewModel 里再存一份当前 Tab 反而会产生
 * 两个数据源、出现不一致。业务相关的外壳状态（导入弹窗、迷你播放器）交给 MainShellViewModel（Step 7）。
 *
 * @property navController 应用唯一的 NavController。
 * @property navigationLayout 当前窗口宽度对应的导航形态（底部栏 / Medium Rail / Expanded Rail）。
 */
@Stable
class EchoPodAppState(
    val navController: NavHostController,
    val navigationLayout: NavigationLayout,
) {

    /** 一级页面列表，顺序即导航项显示顺序。 */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * 当前所属的一级页面；返回栈尚未建立时为 null。
     *
     * 通过 destination.hierarchy 判断：以后从「订阅」进入「节目详情」等二级页面时，
     * 只要二级页面嵌套在订阅的导航图下，「订阅」仍保持选中。
     */
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val destination = backStackEntry?.destination ?: return null
            return topLevelDestinations.firstOrNull { topLevel ->
                destination.hierarchy.any { it.hasRoute(topLevel.route::class) }
            }
        }

    /**
     * 切换到指定一级页面，使用官方「多返回栈」配置：
     * - popUpTo(起始页) + saveState：离开当前 Tab 时保存它的返回栈与页面状态（含滚动位置）；
     * - launchSingleTop：重复点击同一个 Tab 不会叠加多个相同页面；
     * - restoreState：回到某个 Tab 时恢复之前保存的状态。
     */
    fun navigateToTopLevel(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

/**
 * 创建并记住 [EchoPodAppState]。
 *
 * navigationLayout 随窗口宽度变化时会生成新的 EchoPodAppState，但 navController
 * 是同一个实例，因此导航返回栈与各 Tab 的状态不受影响。
 */
@Composable
fun rememberEchoPodAppState(
    navController: NavHostController = rememberNavController(),
    navigationLayout: NavigationLayout = rememberNavigationLayout(),
): EchoPodAppState = remember(navController, navigationLayout) {
    EchoPodAppState(navController, navigationLayout)
}
