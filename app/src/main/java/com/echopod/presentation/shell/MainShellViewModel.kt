package com.echopod.presentation.shell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echopod.domain.playback.usecase.ObserveNowPlayingUseCase
import com.echopod.domain.playback.usecase.SeekPlaybackUseCase
import com.echopod.domain.playback.usecase.TogglePlaybackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用外壳的 ViewModel（作用域为 Activity，由 EchoPodApp 中的 hiltViewModel() 获取）。
 *
 * - 导入弹窗是否显示：存在 [SavedStateHandle]，Activity 重建、进程回收后都能恢复；
 * - 正在播放：来自 domain 层的 [ObserveNowPlayingUseCase]，与弹窗状态 combine 成一个 [MainShellUiState]。
 *
 * 单测不需要 Hilt，直接手动构造：`MainShellViewModel(SavedStateHandle(), ...)`。
 */
@HiltViewModel
class MainShellViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeNowPlaying: ObserveNowPlayingUseCase,
    private val togglePlayback: TogglePlaybackUseCase,
    private val seekPlayback: SeekPlaybackUseCase
) : ViewModel() {

    val uiState: StateFlow<MainShellUiState> =
        combine(
            savedStateHandle.getStateFlow(KEY_IMPORT_VISIBLE, false),
            observeNowPlaying()
        ) { isImportVisible, nowPlaying ->
            MainShellUiState(isImportSheetVisible = isImportVisible, nowPlaying = nowPlaying)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            // 初始值直接读 SavedStateHandle：进程重建后第一帧就恢复弹窗，避免先隐藏再闪现
            MainShellUiState(isImportSheetVisible = savedStateHandle[KEY_IMPORT_VISIBLE] ?: false),
        )

    fun onEvent(event: MainShellEvent) {
        when (event) {
            MainShellEvent.ImportClicked -> savedStateHandle[KEY_IMPORT_VISIBLE] = true
            MainShellEvent.ImportDismissed -> savedStateHandle[KEY_IMPORT_VISIBLE] = false
            MainShellEvent.TogglePlayback -> viewModelScope.launch { togglePlayback() }
            MainShellEvent.SeekBack -> viewModelScope.launch { seekPlayback(-15) }
            MainShellEvent.SeekForward -> viewModelScope.launch { seekPlayback(30) }
        }
    }

    private companion object {
        const val KEY_IMPORT_VISIBLE = "import_visible"
    }

}