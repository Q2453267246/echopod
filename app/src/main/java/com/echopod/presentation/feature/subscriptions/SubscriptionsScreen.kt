package com.echopod.presentation.feature.subscriptions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.echopod.R
import com.echopod.presentation.feature.common.TopLevelPlaceholder

/**
 * 订阅 —— 一级页面（占位）。
 *
 * 当前只用于验证导航外壳的切换与状态保留；
 * 真实内容实现时，按 MVVM 增加 SubscriptionsViewModel + SubscriptionsUiState，本函数改为接收 state 与事件回调。
 */
@Composable
fun SubscriptionsScreen(modifier: Modifier = Modifier) {
    TopLevelPlaceholder(
        title = stringResource(R.string.nav_subscriptions),
        modifier = modifier,
    )
}
