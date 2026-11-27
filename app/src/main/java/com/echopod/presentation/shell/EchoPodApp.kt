package com.echopod.presentation.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.echopod.core.designsystem.component.NavItem
import com.echopod.presentation.navigation.EchoPodAppState
import com.echopod.presentation.navigation.EchoPodNavHost
import com.echopod.presentation.navigation.rememberEchoPodAppState


private const val MiniPlayerAnimMillis = 300

/**
 * 应用根 Composable（有状态入口），由 MainActivity 调用。
 *
 * 职责只是"连线"：
 * - 从 [EchoPodAppState] 读取导航形态与当前一级页面；
 * - 把 TopLevelDestination 映射为设计系统需要的 [NavItem]；
 * - 把点击事件转发给 AppState（切换 Tab）或 [MainShellViewModel]（导入、播放控制）；
 * - 把 EchoPodNavHost 放进无状态的 [EchoPodAppShell]。
 *
 * [viewModel] 在 NavHost 外获取，作用域为 Activity，整个外壳共用一个实例；
 * 作为参数暴露，UI 测试可以直接传入手动构造的实例（测试宿主不是 @AndroidEntryPoint）。
 */
@Composable
fun EchoPodApp(
    appState: EchoPodAppState = rememberEchoPodAppState(),
    viewModel: MainShellViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val destinations = appState.topLevelDestinations
    val items =
        destinations.map { NavItem(label = stringResource(it.labelRes), iconRes = it.iconRes) }
    // 返回栈尚未建立时为 null，indexOf 返回 -1，即暂不高亮任何导航项
    val selectedIndex = destinations.indexOf(appState.currentTopLevelDestination)
    // 两个导入入口（Compact 首页按钮、Rail FAB）发同一个事件
    val onImportClick = { viewModel.onEvent(MainShellEvent.ImportClicked) }

    EchoPodAppShell(
        navigationLayout = appState.navigationLayout,
        items = items,
        selectedIndex = selectedIndex,
        onItemClick = { index -> appState.navigateToTopLevel(destinations[index]) },
        onImportClick = onImportClick,
        miniPlayer = { layout ->
            val style = miniPlayerStyleFor(layout)
            val nowPlaying = uiState.nowPlaying
            // 先写后读：退出动画期间继续用最后一次非空值绘制
            val lastNowPlaying = remember { mutableStateOf(nowPlaying) }
            if (nowPlaying != null) lastNowPlaying.value = nowPlaying
            AnimatedVisibility(
                visible = style != null && nowPlaying != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(MiniPlayerAnimMillis) { it }) + fadeIn(tween(MiniPlayerAnimMillis)),
                exit = slideOutVertically(tween(MiniPlayerAnimMillis)) { it } + fadeOut(tween(MiniPlayerAnimMillis)),
            ) {
                val shown = lastNowPlaying.value ?: return@AnimatedVisibility
                val shownStyle = style ?: MiniPlayerStyle.Compact
                MiniPlayer(
                    nowPlaying = shown,
                    style = shownStyle,
                    onClick = { /* TODO(全屏播放器) */ },
                    onTogglePlayback = { viewModel.onEvent(MainShellEvent.TogglePlayback) },
                    onSeekBack = { viewModel.onEvent(MainShellEvent.SeekBack) },
                    onSeekForward = { viewModel.onEvent(MainShellEvent.SeekForward) },
                    modifier = Modifier
                        .windowInsetsPadding(MiniPlayerInsets)
                        .padding(horizontal = shownStyle.horizontalMargin)
                        .padding(bottom = shownStyle.bottomMargin),
                )
            }
        }
    ) {
        EchoPodNavHost(
            appState = appState,
            onImportClick = onImportClick,
            hasMiniPlayer = uiState.nowPlaying != null
        )
    }

    if (uiState.isImportSheetVisible) {
        ImportPlaceholderDialog(onDismiss = { viewModel.onEvent(MainShellEvent.ImportDismissed) })
    }
}


/**
 * 迷你播放器需要避开的系统区域：手势条 / 三键导航栏、横屏挖孔（只取左右与底部）。
 *
 * - Compact：外壳内容区已消费 navigationBars，这里自动为 0，卡片距导航栏正好 8dp；
 * - Medium：外壳只消费了起始侧，这里会加上底部手势条与结束侧（横屏三键导航）的高度；
 * - 刻意不用 safeDrawing：它包含 IME，键盘弹出时播放器会被顶上去。
 */
private val MiniPlayerInsets: WindowInsets
    @Composable get() = WindowInsets.navigationBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)