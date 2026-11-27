package com.echopod.presentation.shell

import androidx.lifecycle.SavedStateHandle
import com.echopod.data.playback.FakePlaybackRepository
import com.echopod.domain.playback.usecase.ObserveNowPlayingUseCase
import com.echopod.domain.playback.usecase.SeekPlaybackUseCase
import com.echopod.domain.playback.usecase.TogglePlaybackUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * MainShellViewModel 单测：不需要 Hilt，手动构造 Fake 仓库、UseCase 与 ViewModel。
 *
 * uiState 使用 WhileSubscribed，测试里先启动一个收集者，状态才会随上游更新。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainShellViewModelTest {

    private lateinit var repository: FakePlaybackRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakePlaybackRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        MainShellViewModel(
            savedStateHandle = savedStateHandle,
            observeNowPlaying = ObserveNowPlayingUseCase(repository),
            togglePlayback = TogglePlaybackUseCase(repository),
            seekPlayback = SeekPlaybackUseCase(repository),
        )

    @Test
    fun importDialog_hiddenByDefault_shownOnClick_hiddenOnDismiss() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertFalse(viewModel.uiState.value.isImportSheetVisible)

        viewModel.onEvent(MainShellEvent.ImportClicked)
        assertTrue(viewModel.uiState.value.isImportSheetVisible)

        viewModel.onEvent(MainShellEvent.ImportDismissed)
        assertFalse(viewModel.uiState.value.isImportSheetVisible)
    }

    @Test
    fun importDialog_restoredFromSavedStateHandle() {
        val viewModel = createViewModel(SavedStateHandle(mapOf("import_visible" to true)))

        // 初始值直接来自 SavedStateHandle，无需等待收集
        assertTrue(viewModel.uiState.value.isImportSheetVisible)
    }

    @Test
    fun nowPlaying_passedThroughFromRepository() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(repository.nowPlaying.first(), viewModel.uiState.value.nowPlaying)
    }

    @Test
    fun togglePlayback_updatesNowPlaying() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val before = viewModel.uiState.value.nowPlaying!!.isPlaying

        viewModel.onEvent(MainShellEvent.TogglePlayback)

        assertEquals(!before, viewModel.uiState.value.nowPlaying!!.isPlaying)
    }

    @Test
    fun seekBack_and_seekForward_updateProgress() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val start = viewModel.uiState.value.nowPlaying!!.progress
        viewModel.onEvent(MainShellEvent.SeekBack)
        val afterBack = viewModel.uiState.value.nowPlaying!!.progress
        assertTrue(afterBack < start)

        viewModel.onEvent(MainShellEvent.SeekForward)
        assertTrue(viewModel.uiState.value.nowPlaying!!.progress > afterBack)

    }
}
