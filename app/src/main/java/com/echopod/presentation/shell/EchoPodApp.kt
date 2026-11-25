package com.echopod.presentation.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.echopod.core.designsystem.component.NavItem
import com.echopod.presentation.navigation.EchoPodAppState
import com.echopod.presentation.navigation.EchoPodNavHost
import com.echopod.presentation.navigation.rememberEchoPodAppState

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
        // TODO(Step 8)：miniPlayer = { layout -> uiState.nowPlaying?.let { MiniPlayer(...) } }，
        //  同时给 EchoPodNavHost 传 hasMiniPlayer = uiState.nowPlaying != null
    ) {
        EchoPodNavHost(
            appState = appState,
            onImportClick = onImportClick,
        )
    }

    if (uiState.isImportSheetVisible) {
        ImportPlaceholderDialog(onDismiss = { viewModel.onEvent(MainShellEvent.ImportDismissed) })
    }
}
