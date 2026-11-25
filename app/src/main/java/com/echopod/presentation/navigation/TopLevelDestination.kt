package com.echopod.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.echopod.R
import kotlinx.serialization.Serializable

// ---- 一级页面的类型安全路由（Navigation Compose 2.8+）----
// 使用 @Serializable data object：无参数页面不需要字符串路由，编译期即可检查拼写。

/** 首页路由。 */
@Serializable
data object HomeRoute

/** 发现路由。 */
@Serializable
data object DiscoverRoute

/** 订阅路由。 */
@Serializable
data object SubscriptionsRoute

/** 我的路由。 */
@Serializable
data object ProfileRoute

/**
 * 四个一级页面（底部导航栏 / Rail 中的导航项），枚举顺序即显示顺序。
 *
 * 它引用了 Android 资源 ID（@StringRes / @DrawableRes），属于 presentation 层，
 * **不要**放进 domain 层。
 *
 * @property route 对应的类型安全路由对象。
 * @property labelRes 导航项文案。
 * @property iconRes 导航项图标（24dp 线性 Vector Drawable）。
 */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    HOME(HomeRoute, R.string.nav_home, R.drawable.ic_nav_home),
    DISCOVER(DiscoverRoute, R.string.nav_discover, R.drawable.ic_nav_discover),
    SUBSCRIPTIONS(SubscriptionsRoute, R.string.nav_subscriptions, R.drawable.ic_nav_subscriptions),
    PROFILE(ProfileRoute, R.string.nav_profile, R.drawable.ic_nav_profile),
}
