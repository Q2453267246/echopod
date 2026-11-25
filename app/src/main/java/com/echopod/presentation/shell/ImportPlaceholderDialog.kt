package com.echopod.presentation.shell

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme

/**
 * 导入播客弹窗（占位）。
 *
 * 只负责展示，显示与否由 [MainShellUiState.isImportSheetVisible] 控制；
 * 真实导入流程（RSS 链接 / OPML 文件）实现时替换本组件，事件仍走 [MainShellEvent]。
 *
 * 颜色全部显式取自 [EchoPodTheme.colors]，不依赖 M3 默认配色。
 * TextButton 自带 minimumInteractiveComponentSize，触控区域不小于 48dp。
 */
@Composable
fun ImportPlaceholderDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EchoPodTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(R.string.action_import_podcast)) },
        text = {
            Text(
                text = stringResource(R.string.import_dialog_placeholder_message),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.brand),
            ) {
                Text(stringResource(R.string.action_close))
            }
        },
        containerColor = colors.card,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        tonalElevation = 0.dp, // 关闭 M3 的色调叠加，避免卡片底色被品牌色"染色"
    )
}

@Preview(name = "Import dialog · Light")
@Composable
private fun ImportPlaceholderDialogPreview() {
    EchoPodTheme(darkTheme = false) {
        ImportPlaceholderDialog(onDismiss = {})
    }
}
