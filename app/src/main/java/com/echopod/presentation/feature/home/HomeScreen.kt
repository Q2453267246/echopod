package com.echopod.presentation.feature.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme
import com.echopod.presentation.feature.common.TopLevelPlaceholder

/**
 * 首页 —— 一级页面（占位）。
 *
 * 当前只用于验证导航外壳的切换与状态保留；
 * 真实内容实现时，按 MVVM 增加 HomeViewModel + HomeUiState，本函数改为接收 state 与事件回调。
 */
@Composable
fun HomeScreen(
    showImportButton: Boolean,
    onImportClick: () -> Unit,
    horizontalPadding: Dp = 16.dp,
    bottomExtraPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    TopLevelPlaceholder(
        title = stringResource(R.string.nav_home),
        horizontalPadding = horizontalPadding,
        bottomExtraPadding = bottomExtraPadding,
        titleAction = if (showImportButton) {
            {
                Button(
                    onClick = onImportClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoPodTheme.colors.inkButton,
                        contentColor = EchoPodTheme.colors.onInkButton,
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_import),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        } else {
            null
        },
        modifier = modifier,
    )
}