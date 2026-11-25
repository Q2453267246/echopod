package com.echopod.presentation.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.echopod.R
import com.echopod.presentation.feature.common.TopLevelPlaceholder

/**
 * 我的 —— 一级页面（占位）。
 *
 * 当前只用于验证导航外壳的切换与状态保留；
 * 真实内容实现时，按 MVVM 增加 ProfileViewModel + ProfileUiState，本函数改为接收 state 与事件回调。
 */
@Composable
fun ProfileScreen(
    horizontalPadding: Dp = 16.dp,
    bottomExtraPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    TopLevelPlaceholder(
        title = stringResource(R.string.nav_profile),
        horizontalPadding = horizontalPadding,
        bottomExtraPadding = bottomExtraPadding,
        modifier = modifier,
    )
}
