package com.echopod.presentation.feature.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme

/** 占位列表的条目数量：足够长，便于验证滚动位置是否保留。 */
private const val PlaceholderItemCount = 50

/**
 * 一级页面的临时占位内容：大标题 + 50 项列表。
 *
 * 目的不是展示内容，而是用来人工验收导航外壳：
 * 在某个 Tab 滚动到中间 → 切换 Tab / 折叠展开 / 旋转 → 回来后应仍停在原位置。
 * rememberLazyListState() 是 saveable 的，配合 NavHost 的 saveState / restoreState 即可保留。
 *
 * Insets 说明：列表首尾各放一个 Spacer，高度分别为 windowInsetsTopHeight / windowInsetsBottomHeight。
 * 这两个修饰符会扣除外壳已经消费的 insets（见 EchoPodAppShell）：
 * - Compact：顶部留出状态栏；底部手势条已由导航栏处理 → 0；
 * - Medium：顶部留出状态栏，底部留出手势条；
 * - Expanded：纸面已避开系统栏 → 都为 0。
 * 列表内容可以滚动到状态栏后面，符合 edge-to-edge。迷你播放器的留白在 Step 10 补充。
 *
 * @param title 页面标题（如"首页"）。
 */
@Composable
fun TopLevelPlaceholder(
    title: String,
    horizontalPadding: Dp = 16.dp,
    bottomExtraPadding: Dp = 0.dp,
    titleAction: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 16.dp + bottomExtraPadding),
    ) {
        item(key = "top-inset") {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        }
        item(key = "title") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = EchoPodTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                titleAction?.invoke()
            }
        }
        items(count = PlaceholderItemCount, key = { it }) { index ->
            Text(
                text = stringResource(R.string.placeholder_item, title, index + 1),
                style = MaterialTheme.typography.bodyLarge,
                color = EchoPodTheme.colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 14.dp),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                color = EchoPodTheme.colors.divider,
            )
        }
        item(key = "bottom-inset") {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Preview(
    name = "Placeholder · Light",
    widthDp = 390,
    heightDp = 600,
    showBackground = true,
    backgroundColor = 0xFFF7F5F0
)
@Composable
private fun TopLevelPlaceholderPreview() {
    EchoPodTheme(darkTheme = false) {
        TopLevelPlaceholder(title = "订阅")
    }
}
