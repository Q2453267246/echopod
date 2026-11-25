package com.echopod.presentation.shell

import androidx.compose.runtime.Immutable
import com.echopod.domain.playback.model.NowPlaying

/**
 * 应用外壳的 UI 状态（不可变）。
 *
 * 必须是 data class：StateFlow 依靠 equals 去重，内容相同的新实例不会触发多余的重组；
 * 单测也可以直接用 assertEquals 比较整个状态。
 *
 * @property isImportSheetVisible 导入弹窗是否显示（保存在 SavedStateHandle，折叠/展开、进程回收后仍在）。
 * @property nowPlaying 正在播放的单集；null 表示没有播放内容，不显示迷你播放器。
 */
@Immutable
data class MainShellUiState(
    val isImportSheetVisible: Boolean = false,
    val nowPlaying: NowPlaying? = null,
)

/** 外壳可接收的用户事件，统一通过 [MainShellViewModel.onEvent] 分发。 */
sealed interface MainShellEvent {
    /** 点击导入入口（Compact 首页按钮或 Rail FAB）。 */
    data object ImportClicked : MainShellEvent

    /** 关闭导入弹窗。 */
    data object ImportDismissed : MainShellEvent

    /** 迷你播放器：播放 / 暂停。 */
    data object TogglePlayback : MainShellEvent

    /** 迷你播放器：快退 15 秒。 */
    data object SeekBack : MainShellEvent

    /** 迷你播放器：快进 30 秒。 */
    data object SeekForward : MainShellEvent
}
