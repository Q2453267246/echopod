package com.echopod.presentation.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
 * - 把点击事件转发给 AppState（切换 Tab）；
 * - 把 EchoPodNavHost 放进无状态的 [EchoPodAppShell]。
 *
 * Step 7 将在这里接入 MainShellViewModel（导入弹窗状态），
 * Step 8 接入迷你播放器（miniPlayer 槽位）。
 */
@Composable
fun EchoPodApp(
    appState: EchoPodAppState = rememberEchoPodAppState(),
) {
    val destinations = appState.topLevelDestinations
    val items = destinations.map { NavItem(label = stringResource(it.labelRes), iconRes = it.iconRes) }
    // 返回栈尚未建立时为 null，indexOf 返回 -1，即暂不高亮任何导航项
    val selectedIndex = destinations.indexOf(appState.currentTopLevelDestination)

    EchoPodAppShell(
        navigationLayout = appState.navigationLayout,
        items = items,
        selectedIndex = selectedIndex,
        onItemClick = { index -> appState.navigateToTopLevel(destinations[index]) },
        // TODO(Step 7): 改为 viewModel.onEvent(MainShellEvent.ImportClicked)，显示导入弹窗
        onImportClick = {},
    ) {
        EchoPodNavHost(appState = appState)
    }
}
