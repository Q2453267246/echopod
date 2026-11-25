# 第 1 周开发步骤（2026-11-30 周一 → 12-04 周五）

> 所属排期：[development-schedule.md](development-schedule.md) 第 1 周 · 路线图：[development-roadmap.md](development-roadmap.md) 阶段 0 收尾 + 阶段 1
> 约束：[AGENTS.md](../AGENTS.md)。颜色只从 `EchoPodTheme.colors` 读取；十六进制色值**只允许出现在 `Color.kt`**。

---

## 0. 本周目标

| 目标 | 交付物 | 里程碑 |
|---|---|---|
| 外壳收尾 | `MiniPlayer`（Compact / Medium 两种样式）接入外壳；外壳与导航的 Compose UI 测试 | **M0 外壳**（周二） |
| 组件库 | 封面配色令牌 + `PodcastCover`、图标集、按钮、区块标题、进度条、筛选标签、提示条、单集 / 节目条目、继续收听卡片 | 阶段 1 完成（周五） |

**本周不做**：网络、数据库、真实封面图（Coil 在第 2 周导入对话框时接入）、全屏播放器（点击迷你播放器暂时只留回调）。

### 开工前状态（已确认）

- 阶段 0 的 Hilt、`MainShellViewModel`、Fake 播放链路、单测已在提交 `2aba623` 中。
- `EchoPodApp` 的 `miniPlayer` 槽位为空，`EchoPodNavHost` 的 `hasMiniPlayer` 未传入。
- `res/drawable` 只有 4 个导航图标、`ic_add`、`ic_echopod`。
- `app/src/androidTest` 目录尚不存在；`compose-ui-test-junit4`、`androidx-junit`、`ui-test-manifest` 依赖已配置。

### 从设计稿确认的两处设计决定

1. **字体**：设计语言页注明"Android 端可直接用系统 Roboto + 思源黑体回退"。因此**不打包 Inter**，继续用系统字体；时间、进度等数字统一开启等宽数字（`fontFeatureSettings = "tnum"`）。路线图阶段 1 中"是否引入 Inter"的待定项就此关闭。
2. **筛选标签选中色**：设计稿中选中的筛选标签是**墨色**（`inkButton` / `onInkButton`），未选中是 `container`。不是 brandContainer。

---

## 周一：迷你播放器

### 1.1 新增颜色令牌：`shadow`

迷你播放器、继续收听卡片都有投影：浅色用主文字色、深色用纯黑，再叠透明度。为了不在 UI 里写 `Color.Black`，在 `EchoPodColors` 中新增：

| 令牌 | 浅色 | 深色 | 用途 |
|---|---|---|---|
| `shadow` | `#1C1B19` | `#000000` | 投影颜色（使用时 `copy(alpha = …)`） |

`toMaterialColorScheme()` 无需改动。

### 1.2 播放相关图标（4 个）

先做本日需要的 4 个，其余图标周三统一补齐。格式与现有 `ic_nav_*.xml` 一致：`24dp`、`viewportWidth/Height = 24`，颜色写 `@android:color/black`，由 `Icon` 的 tint 着色。

| 文件 | 类型 | pathData（来自设计稿） |
|---|---|---|
| `ic_play.xml` | 填充 | `M8,5.5v13l11,-6.5z` |
| `ic_pause.xml` | 填充 | 两个圆角矩形：`M7.7,5h1.2a1.2,1.2 0,0 1,1.2 1.2v11.6a1.2,1.2 0,0 1,-1.2 1.2h-1.2a1.2,1.2 0,0 1,-1.2 -1.2V6.2a1.2,1.2 0,0 1,1.2 -1.2z` 与 x 偏移 7.4 的同形路径 |
| `ic_skip_back.xml` | 描边 1.7，圆角端点 | `M4.5,12a7.5,7.5 0,1 0,2.2 -5.3` 与 `M4,3.5v3.7h3.7` |
| `ic_skip_forward.xml` | 描边 1.7，圆角端点 | `M19.5,12a7.5,7.5 0,1 1,-2.2 -5.3` 与 `M20,3.5v3.7h-3.7` |

> 设计稿的快退 / 快进图标中间有数字"15 / 30"。VectorDrawable 不能渲染文字，因此图标只画箭头，数字由 Compose 叠加（见 1.4 的 `SkipIcon`）。秒数以后由设置项决定，这样也不用为每个秒数各画一个图标。

### 1.3 文案（`strings.xml`）

```xml
<!-- 迷你播放器 -->
<string name="player_play">播放</string>
<string name="player_pause">暂停</string>
<string name="player_seek_back">快退 %1$d 秒</string>
<string name="player_seek_forward">快进 %1$d 秒</string>
<string name="player_open">打开播放器</string>
<string name="mini_player_subtitle">%1$s · %2$sx</string>
<string name="mini_player_subtitle_remaining">%1$s · %2$sx · 剩余 %3$d 分</string>
```

### 1.4 `MiniPlayer`（`presentation/shell/MiniPlayer.kt`）

**规格（来自 `Main.dc.html` / `FoldHome.dc.html`）：**

| 项 | Compact | Medium（Expanded 暂用） |
|---|---|---|
| 高度 / 圆角 | 64 / 16 | 68 / 18 |
| 外边距 | 左右 8、距导航栏 8 | 左右 24、底部 16 + 系统手势条 |
| 内边距（起始 / 结束） | 10 / 4 | 12 / 8 |
| 元素间距 | 10 | 12 |
| 封面 | 44，圆角 8 | 46，圆角 8 |
| 标题 | 14 SemiBold，单行省略 | 同左 |
| 副标题 | 12 `textSecondary`："夜航电台 · 1.2x" | "夜航电台 · 1.2x · 剩余 23 分" |
| 按钮 | 播放 / 暂停（48 触控区，24 图标，`textPrimary`） | 快退（26 图标）+ 墨色圆形播放 / 暂停（48，图标 22，`inkButton` / `onInkButton`）+ 快进 |
| 底部进度线 | 高 2，左右内缩 12，轨道 `outline`，进度 `brand` | 同左 |
| 背景 / 投影 | `card`；`shadow` 两层投影 | 同左 |

**结构：**

```kotlin
/** 迷你播放器的两种样式；Expanded 在阶段 5 改为停靠详情栏，之前暂用 Medium。 */
enum class MiniPlayerStyle(
    val height: Dp, val corner: Dp, val coverSize: Dp,
    val startPadding: Dp, val endPadding: Dp, val spacing: Dp,
    val showSkipButtons: Boolean,
) {
    Compact(64.dp, 16.dp, 44.dp, 10.dp, 4.dp, 10.dp, showSkipButtons = false),
    Medium(68.dp, 18.dp, 46.dp, 12.dp, 8.dp, 12.dp, showSkipButtons = true);

    companion object {
        fun from(layout: NavigationLayout) =
            if (layout == NavigationLayout.BottomBar) Compact else Medium
    }
}

@Composable
fun MiniPlayer(
    nowPlaying: NowPlaying,
    style: MiniPlayerStyle,
    onOpen: () -> Unit,           // 点击左侧信息区：打开全屏播放器（第 3 周实现，现在传空）
    onTogglePlay: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
    seekBackSeconds: Int = 15,     // 阶段 12 改为读设置
    seekForwardSeconds: Int = 30,
)
```

- 根布局：`Box(Modifier.height(style.height).shadow(...).clip(RoundedCornerShape(style.corner)).background(colors.card))`，里面 `Row` + 底部对齐的进度线。
- 左侧信息区（封面 + 两行文字）整体 `clickable(onClickLabel = stringResource(R.string.player_open), role = Role.Button, onClick = onOpen)`，并 `semantics(mergeDescendants = true)`，TalkBack 读作"标题，副标题，按钮，打开播放器"。
- 封面：周一先用一个 `Box` 占位（`container` 背景 + 圆角 8），**周三 `PodcastCover` 完成后替换**。
- 播放 / 暂停按钮：`IconButton`（自带 48dp 触控区），`contentDescription` 随 `isPlaying` 切换"暂停 / 播放"。
- `SkipIcon(seconds, forward)`：`Box(contentAlignment = Center) { Icon(ic_skip_*), Text("$seconds") }`。数字字号约 `7.sp`、Bold、等宽数字；为避免受系统字体缩放影响撑破图标，字号用 `with(LocalDensity.current) { 7.4.dp.toSp() }`。
- 进度线复用周四的 `EchoPodProgressBar`；周一先在本文件内用 `Box` + `fillMaxWidth(progress)` 实现，周四替换。
- 背景、文字、图标、进度颜色全部来自 `EchoPodTheme.colors`。

**预览**：Compact 浅 / 深、Medium 浅 / 深、暂停态，共 5 个 `@Preview`，用 `FakePlaybackRepository` 中的示例数据构造 `NowPlaying`。

### 1.5 接入外壳（`EchoPodApp.kt`、`EchoPodNavHost.kt`）

```kotlin
// EchoPodApp.kt
EchoPodAppShell(
    ...,
    miniPlayer = { layout ->
        uiState.nowPlaying?.let { nowPlaying ->
            MiniPlayer(
                nowPlaying = nowPlaying,
                style = MiniPlayerStyle.from(layout),
                onOpen = { /* TODO(第 3 周)：展开全屏播放器 */ },
                onTogglePlay = { viewModel.onEvent(MainShellEvent.TogglePlayback) },
                onSeekBack = { viewModel.onEvent(MainShellEvent.SeekBack) },
                onSeekForward = { viewModel.onEvent(MainShellEvent.SeekForward) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .miniPlayerPlacement(layout),
            )
        }
    },
) {
    EchoPodNavHost(
        appState = appState,
        onImportClick = onImportClick,
        hasMiniPlayer = uiState.nowPlaying != null,
    )
}

/** 迷你播放器在内容区中的位置（只改 Modifier，不改组合结构）。 */
private fun Modifier.miniPlayerPlacement(layout: NavigationLayout): Modifier = when (layout) {
    // 内容区在底部导航栏之上，留 8dp 即"距导航栏 8dp"
    NavigationLayout.BottomBar -> padding(8.dp)
    // Medium：避开手势条后再留 16dp；Expanded 的纸面已消费底部 inset，这里自动为 0
    else -> windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
}
```

- `EchoPodNavHost` 中 `ExpandedRail -> 0.dp` 改为与 Medium 相同的 `84.dp`，注释写明"阶段 5 改为停靠详情栏前，Expanded 暂用 Medium 样式悬浮"。
- `MainActivity` 无需改动。

### 1.6 验证与提交

- `./gradlew :app:assembleDebug`、`./gradlew :app:testDebugUnitTest`
- 模拟器：三种断点各看一次；点击播放 / 暂停、快退 / 快进，进度线与副标题"剩余 N 分"变化；列表滚到底，最后一项不被迷你播放器遮挡。
- 对比 [01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png)、[02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png)、[14-首页-深色.png](../EchoPod-设计稿/PNG/01-手机/14-首页-深色.png) 的底部区域。

**提交**：`feat(shell): add mini player with compact and medium styles`

---

## 周二：外壳 UI 测试 → M0

### 2.1 测试基础

- 新建 `app/src/androidTest/java/com/echopod/`。
- 运行环境：Pixel Fold 模拟器（或任意 API 26+ 模拟器）；**不要**在个人真机上跑。
- 若运行时报找不到 `AndroidJUnitRunner`，在 `libs.versions.toml` 增加 `androidx.test:runner` 并 `androidTestImplementation`。
- 给占位列表加测试标签：`TopLevelPlaceholder` 的 `LazyColumn` 加 `Modifier.testTag(PlaceholderListTag)`，常量 `internal const val PlaceholderListTag = "top_level_list"`。

### 2.2 `EchoPodAppShellTest`（无状态外壳，直接指定形态）

| 测试 | 断言 |
|---|---|
| `bottomBar_showsFourItems_withoutFab` | 4 个导航文字都存在；`onNodeWithContentDescription("导入播客")` 不存在 |
| `mediumAndExpandedRail_showFabAndItems` | 两种 Rail 下 FAB 与 4 项都存在 |
| `clickItem_reportsIndex_andMarksSelected` | 点击"订阅"→ 回调收到 2；外部状态更新后 `assertIsSelected()` |
| `navigationTargets_areAtLeast48dp` | 遍历 4 项 + FAB：`assertHeightIsAtLeast(48.dp)`、`assertWidthIsAtLeast(48.dp)` |
| `switchingLayout_keepsContentInstance` | content 中 `val token = remember { Any() }` 并把它记到外部列表；把 `layout` 从 BottomBar → MediumRail → ExpandedRail → BottomBar 切换，断言记录到的 token 始终是同一个对象（验证"content 调用位置固定"） |
| `miniPlayerSlot_rendersPerLayout` | 传入 `MiniPlayer`：Compact 下没有"快退 15 秒"，Medium 下有 |

### 2.3 `EchoPodAppTest`（真实 NavHost + 手动构造 ViewModel）

```kotlin
private fun createViewModel(): MainShellViewModel {
    val repo = FakePlaybackRepository()
    return MainShellViewModel(
        SavedStateHandle(),
        ObserveNowPlayingUseCase(repo), TogglePlaybackUseCase(repo), SeekPlaybackUseCase(repo),
    )
}

composeRule.setContent {
    EchoPodTheme {
        EchoPodApp(
            appState = rememberEchoPodAppState(navigationLayout = NavigationLayout.BottomBar),
            viewModel = createViewModel(),
        )
    }
}
```

| 测试 | 步骤与断言 |
|---|---|
| `switchTabs_keepsScrollPosition` | 点"订阅"→ 对 `PlaceholderListTag` 执行 `performScrollToIndex(32)` → 点"首页"→ 点"订阅"→ "订阅 · 第 30 项"仍显示 |
| `stateRestoration_keepsTabAndScroll` | 用 `StateRestorationTester` 包裹，切到"订阅"并滚动后 `emulateSavedInstanceStateRestore()`，断言"订阅"仍选中、列表位置不变 |
| `importButton_opensAndClosesDialog` | Compact 首页点"导入"→ "导入播客"对话框出现 → 点"关闭"→ 消失 |
| `miniPlayer_togglesPlayback` | 点"暂停"→ 按钮变为"播放" |

> `createComposeRule()` 的宿主 Activity 不是 `@AndroidEntryPoint`，所以必须显式传入 `viewModel`，不能走 `hiltViewModel()`。

### 2.4 验证与提交

- `./gradlew :app:connectedDebugAndroidTest` 全部通过。
- 在 [coding-steps-navigation.md](coding-steps-navigation.md) 中把 Step 8 的 `MiniPlayer` 与 11.2 标为完成；路线图阶段 0 打勾。

**提交**：`test(shell): add adaptive shell and navigation UI tests` → **M0 外壳 达成**

---

## 周三：令牌、图标、封面

### 3.1 颜色令牌：`warning` 与封面配色（`Color.kt`）

| 令牌 | 浅色 | 深色 | 用途 |
|---|---|---|---|
| `warning` | `#A4561F` | `#F0A060`（**设计稿未给出深色值，暂用更新圆点的深色，待设计确认**） | "刷新失败 · 显示缓存"、提示条图标 |

封面配色：设计语言页说明"示例封面使用低饱和色，真实封面由 Palette 取主色"。没有封面图（或加载失败）时，用下面 8 组配色生成占位封面。深色模式下设计稿使用同一组颜色。

```kotlin
/** 占位封面的一组配色：底色、右上角圆形色块、首字颜色。 */
@Immutable
data class CoverPalette(val background: Color, val accent: Color, val content: Color)

// 写在 Color.kt 中，并作为 EchoPodColors.coverPalettes 暴露（浅 / 深色共用同一列表）
internal val EchoPodCoverPalettes = listOf(
    CoverPalette(Color(0xFF2E6B66), Color(0xFFE8D9B8), Color(0xFFF7F5F0)), // 雾青 · 码
    CoverPalette(Color(0xFF2E3A59), Color(0xFFC9783B), Color(0xFFF7F5F0)), // 夜蓝 · 夜
    CoverPalette(Color(0xFFD9C9A8), Color(0xFF2E6B66), Color(0xFF1C1B19)), // 沙色 · 慢
    CoverPalette(Color(0xFF8C4A3C), Color(0xFFE8B07A), Color(0xFFF7F5F0)), // 砖红 · 城
    CoverPalette(Color(0xFF5B5F97), Color(0xFFB9BCE6), Color(0xFFF7F5F0)), // 靛紫 · 微
    CoverPalette(Color(0xFF3B4A3F), Color(0xFFC9783B), Color(0xFFF7F5F0)), // 墨绿 · 商
    CoverPalette(Color(0xFFEFE6D2), Color(0xFF8C4A3C), Color(0xFF1C1B19)), // 纸色 · 纸
    CoverPalette(Color(0xFF1C1B19), Color(0xFF86C6BD), Color(0xFFF7F5F0)), // 墨色 · 外
)
```

`EchoPodColors` 新增字段 `val coverPalettes: List<CoverPalette>`（`@Immutable` 的 data class 中放 `List` 时，确保传入的是不可变列表）。

### 3.2 数字排版

`Type.kt` 新增：

```kotlin
/** 时间、进度、计数使用等宽数字，避免数字跳动（设计语言：时间使用等宽数字）。 */
fun TextStyle.tabularNumbers(): TextStyle = copy(fontFeatureSettings = "tnum")
```

### 3.3 图标集补齐（`res/drawable`）

全部来自设计稿 HTML 中的 SVG（24 视口；描边图标 1.8 宽、圆角端点；填充图标用 `fillColor`）：

| 文件 | 类型 | pathData |
|---|---|---|
| `ic_search.xml` | 描边 | 圆 `cx=11 cy=11 r=7`（写成 `M18,11a7,7 0,1 1,-14 0a7,7 0,1 1,14 0`）+ `M20,20l-3.5,-3.5` |
| `ic_download.xml` | 描边 | `M12,4v11m-5,-5 5,5 5,-5M5,20h14` |
| `ic_downloaded.xml` | 描边 2.2 | `M5,12.5l4.5,4.5L19,7.5`（设计稿未导出路径，按"对勾"自绘） |
| `ic_heart.xml` | 描边 | `M12,20s-7.5,-4.6 -7.5,-10.2A4.3,4.3 0,0 1,12 7a4.3,4.3 0,0 1,7.5 2.8C19.5,15.4 12,20 12,20z` |
| `ic_queue.xml` | 描边 | `M4,6h12M4,12h12M4,18h7M18,15v6m-3,-3h6` |
| `ic_timer.xml` | 描边 | `M19.5,14.5A8,8 0,1 1,9.5 4.5a6.5,6.5 0,0 0,10 10z` |
| `ic_link.xml` | 描边 | `M10,14a4,4 0,0 0,5.7 0l3,-3a4,4 0,0 0,-5.7 -5.7l-1,1` + `M14,10a4,4 0,0 0,-5.7 0l-3,3a4,4 0,0 0,5.7 5.7l1,-1` |
| `ic_history.xml` | 描边 | 圆 `r=9` + `M12,7v5l3,2` |
| `ic_stats.xml` | 描边 | `M5,20V11M12,20V4M19,20v-6` |
| `ic_sort.xml` | 描边 | `M7,4v16m0,0 -3,-3m3,3 3,-3M17,20V4m0,0 -3,3m3,-3 3,3` |
| `ic_refresh.xml` | 描边 | `M20,12a8,8 0,1 1,-2.3 -5.7` + `M20,4v4h-4` |
| `ic_share.xml` | 描边 | `M12,3v12M7,8l5,-5 5,5` + `M5,13v7h14v-7` |
| `ic_filter_lines.xml` | 描边 | `M4,7h16M7,12h10M10,17h4` |
| `ic_chevron_right.xml` | 描边 2 | `M9,6l6,6 -6,6` |
| `ic_chevron_down.xml` | 描边 2 | `M6,9l6,6 6,-6` |
| `ic_back.xml` | 描边 2 | `M15,6l-6,6 6,6`（`android:autoMirrored="true"`） |
| `ic_close.xml` | 描边 2 | `M7,7l10,10M17,7L7,17` |
| `ic_previous.xml` | 填充 | `M17,6v12L8.5,12z` + 竖条 `M7,6h0.2a1,1 0,0 1,1 1v10a1,1 0,0 1,-1 1H7a1,1 0,0 1,-1 -1V7a1,1 0,0 1,1 -1z` |
| `ic_next.xml` | 填充 | `M7,6v12l8.5,-6z` + 竖条（x = 15.8） |
| `ic_more_vert.xml` | 填充 | 三个圆点 `cy = 5.5 / 12 / 18.5`，`r = 1.6` |

- `ic_back`、`ic_chevron_right` 加 `android:autoMirrored="true"`，RTL 时自动镜像。
- 完成后在 Android Studio 的 Resource Manager 里逐个肉眼检查，与设计稿图标对比。

**提交**：`feat(designsystem): add icon set from design`

### 3.4 `PodcastCover`（`core/designsystem/component/PodcastCover.kt`）

**几何规则**（从设计稿 44 / 56 / 64 / 76 / 96dp 五种尺寸归纳，均按封面边长 S 计算）：

| 元素 | 规则 |
|---|---|
| 圆角 | S ≤ 48 → 8；S ≤ 60 → 10；否则 12 |
| 右上角圆形色块 | 直径 0.5S，向右上方各溢出 0.145S |
| 首字 | 字号 ≈ 0.46S、Bold、行高 1；距左 0.104S、距底 0.08S |
| 更新圆点（可选） | 直径 10，距左上 8；外圈 2dp 描边，颜色为封面底色 |
| 未听数徽标（可选） | 高 20、圆角 10、水平内边距 7，`inkButton` 底 / `onInkButton` 字，11 SemiBold；距右下 6 |
| 刷新失败（可选） | 整体 `alpha = 0.55` |

```kotlin
@Composable
fun PodcastCover(
    title: String,
    modifier: Modifier = Modifier,       // 由调用方指定 size(56.dp) 或 fillMaxWidth().aspectRatio(1f)
    hasUpdate: Boolean = false,
    unreadCount: Int = 0,
    dimmed: Boolean = false,
    // artworkUrl: String? = null        // 第 2 周接入 Coil 时增加；加载失败回退到占位封面
)
```

- 内部用 `BoxWithConstraints` 取实际边长 S，再按上表计算；首字字号用 `with(LocalDensity.current) { (S * 0.46f).toSp() }`，不随系统字体缩放（封面是图形）。
- 无障碍：封面是装饰，`clearAndSetSemantics {}`；未听数由所在条目的文字朗读。
- **纯函数**放在同目录 `CoverArt.kt`，便于 JVM 单测：

```kotlin
/** 按标题稳定地选择配色：同一个节目永远是同一种颜色。 */
internal fun coverPaletteIndex(title: String, paletteCount: Int): Int =
    Math.floorMod(title.trim().hashCode(), paletteCount)

/** 取首个可显示字符：跳过空白和常见符号；拉丁字母转大写；空标题返回"?"。 */
internal fun coverInitial(title: String): String
```

**单测**（`test/.../designsystem/component/CoverArtTest.kt`）：同标题同配色、不同标题分布到多个配色；`"代码与咖啡" → "代"`、`"  #142 Compose" → "1"`（或按实现约定）、`"echo" → "E"`、`"" → "?"`、Emoji 开头不被截成半个字符（按 code point 处理）。

**提交**：`feat(designsystem): add cover palette tokens and PodcastCover`，并把周一 `MiniPlayer` 中的占位封面替换为 `PodcastCover`。

---

## 周四：基础组件

### 4.1 `EchoPodButton`（`component/EchoPodButton.kt`）

| 变体 | 背景 / 文字 | 设计稿用途 |
|---|---|---|
| `Brand` | `brand` / `onBrand` | 订阅、播放最新 |
| `Ink` | `inkButton` / `onInkButton` | 导入 |
| `Tonal` | `brandContainer` / `onBrandContainer` | 已订阅 |
| `Text` | 透明 / `brand` | 全部、重试 |

| 尺寸 | 视觉高度 | 水平内边距 | 文字 |
|---|---|---|---|
| `Large` | 48 | 22（带前置图标时 18） | 15 SemiBold（`labelLarge`） |
| `Small` | 36 | 16 | 13 SemiBold |

```kotlin
@Composable
fun EchoPodButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Brand,
    size: ButtonSize = ButtonSize.Large,
    @DrawableRes leadingIcon: Int? = null,   // 18dp，与文字间距 6
    enabled: Boolean = true,
)
```

- 基于 M3 `Button`，用 `ButtonDefaults.buttonColors(...)` **显式传入全部四个颜色**（含 disabled，用令牌加透明度），`elevation = null`，形状 `CircleShape`。
- `Small` 的视觉高度 36，但 M3 按钮自带 `minimumInteractiveComponentSize`，触控区仍为 48。
- 改造首页标题栏的"+ 导入"按钮为 `EchoPodButton(variant = Ink, leadingIcon = R.drawable.ic_add)`，删除 `HomeScreen` 中手写的 `Button`。

### 4.2 `SectionHeader`

```kotlin
@Composable
fun SectionHeader(
    title: String,                                     // 18 SemiBold（titleLarge）
    modifier: Modifier = Modifier,
    actionLabel: String? = null,                       // "全部 12" + chevron，13 textSecondary
    onAction: (() -> Unit)? = null,
    hint: String? = null,                              // "下拉刷新"，12 textSecondary，不可点击
)
```

操作区高度 48（设计稿用 `margin: -12px 0` 扩大热区但不撑高标题行），用 `Modifier.minimumInteractiveComponentSize()` 实现。

### 4.3 `EchoPodProgressBar`

```kotlin
@Composable
fun EchoPodProgressBar(
    progress: Float,                // 0f..1f，内部 coerceIn
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,              // 继续收听卡片 4、单集条目 3、迷你播放器 2
)
```

`drawBehind` 绘制轨道（`outline`）与进度（`brand`），圆角 = 高度 / 2。语义：`progressSemantics(progress)`。替换 `MiniPlayer` 里周一的临时进度线。

### 4.4 `FilterChipRow`

| 项 | 规格 |
|---|---|
| 标签 | 视觉高 36、圆角 18、水平内边距 14；13 Medium |
| 选中 | `inkButton` / `onInkButton` |
| 未选中 | `container` / `textPrimary` |
| 间距 | 8；行可横向滚动，首尾留页边距（`contentPadding` 由调用方传入：Compact 16、Medium 24） |
| 触控 | 上下各扩 6 达到 48（设计语言原文） |
| 动效 | 选中态颜色 `animateColorAsState(tween(200))` |

```kotlin
@Immutable data class FilterChipItem(val label: String, val count: Int? = null)  // "有更新 4"

@Composable
fun FilterChipRow(
    items: List<FilterChipItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
)
```

语义：外层 `selectableGroup()`，每个标签 `selectable(selected, role = Role.Tab)`。

### 4.5 `InlineBanner`

| 项 | 规格（`Search.dc.html`） |
|---|---|
| 容器 | `card` 背景、圆角 16、内边距 上下 12 / 起始 16 / 结束 8、元素间距 12 |
| 图标 | 20dp，`warning` |
| 文字 | 标题 13 SemiBold `textPrimary`；说明 13 `textSecondary`；行高 1.45 |
| 操作 | `EchoPodButton(variant = Text, size = Small)`，例如"重试" |

```kotlin
@Composable
fun InlineBanner(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.ic_warning,    // 圆 + 感叹号，按设计自绘
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
)
```

需要补一个 `ic_warning.xml`（圆 `r=9` + `M12,8v5` + `M12,16.5v0.01`，描边 1.9）。

### 4.6 验证与提交

- 每个组件浅 / 深色 `@Preview`；按钮 4 变体 × 2 尺寸、筛选行选中 / 未选中。
- 首页"导入"按钮替换后，外观与 [01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png) 一致；周二的 `importButton_opensAndClosesDialog` 测试仍通过。

**提交**：`feat(designsystem): add button, section header, progress bar, filter chips and banner`

---

## 周五：列表条目、卡片、测试、验收

### 5.1 `EpisodeListItem`

规格来自设计语言页"单集条目（列表通用）"与 `Main.dc.html`"最新更新"：

| 项 | 规格 |
|---|---|
| 封面 | 56，圆角 10（`PodcastCover`，用节目标题生成） |
| 标题 | 15 SemiBold，行高 1.35，最多 2 行 |
| 元信息行 | 12 `textSecondary`，例如"代码与咖啡 · 2 小时前 · 48 分钟"；状态标签在前，间距 8 |
| 收听进度 | 有进度时在元信息下方显示 3dp 进度条，宽 160，上边距 8 |
| 尾部 | 插槽：首页为"下载"图标按钮；节目详情为圆形描边播放按钮（48，边框 1.5 `outline`）+ "更多" |
| 行内边距 | 上下 6，元素间距 12 |
| 长按 | 打开快捷菜单（本周只暴露 `onLongClick` 回调） |

**状态与优先级**（设计语言："播放中 › 已下载 › 下载中（环形进度）› 已播放（标题降为辅助色）"）：

```kotlin
sealed interface EpisodeStatus {
    data object None : EpisodeStatus
    data object Played : EpisodeStatus                            // 标题改为 textSecondary
    data class Downloading(val progress: Float) : EpisodeStatus   // 14dp 环形进度（brand / outline）+ "下载中"
    data object Downloaded : EpisodeStatus                        // ic_downloaded 14dp + "已下载"，brand SemiBold
    data object Playing : EpisodeStatus                           // "播放中"，brand SemiBold
}

@Immutable
data class EpisodeListItemState(
    val title: String,
    val podcastTitle: String,     // 用于生成封面
    val meta: String,             // 已格式化的元信息
    val status: EpisodeStatus = EpisodeStatus.None,
    val listenProgress: Float? = null,
)

@Composable
fun EpisodeListItem(
    state: EpisodeListItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
)
```

- 状态的优先级由**调用方**（ViewModel 映射时）决定，组件只渲染一个状态，避免组件里写业务判断。
- 文案进 `strings.xml`：`episode_status_playing`、`episode_status_downloaded`、`episode_status_downloading`、`action_download`、`action_more`。
- 整行 `combinedClickable(onClick, onLongClick)`；尾部按钮是独立可点击元素，不合并语义。

### 5.2 `PodcastGridItem` 与 `PodcastListItem`

| 组件 | 用途 | 规格 |
|---|---|---|
| `PodcastGridItem` | 首页"我的订阅"（固定宽 96）、订阅页网格（填满列宽） | 封面方形 + 间距 8 + 标题（13，首页 Medium / 订阅页 SemiBold，单行省略）+ 可选副标题（12，`textSecondary`；刷新失败时用 `warning`，并且封面 `dimmed`）；封面可带更新圆点、未听数徽标 |
| `PodcastListItem` | 搜索结果 | 封面 64 / 圆角 12；标题 15 SemiBold（`AnnotatedString`，关键词用 `brand` 高亮）；主播 12；"分类 · N 集"12；尾部 `EchoPodButton(Small)`：订阅（Brand）/ 已订阅（Tonal） |

```kotlin
@Composable
fun PodcastGridItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleIsWarning: Boolean = false,
    hasUpdate: Boolean = false,
    unreadCount: Int = 0,
    emphasizeTitle: Boolean = false,   // 订阅页 SemiBold，首页 Medium
)
```

### 5.3 `ContinueListeningCard`

| 项 | 规格（`Main.dc.html`"继续收听"） |
|---|---|
| 容器 | `card`、圆角 20、内边距 16、上下两部分间距 14；`shadow` 两层投影 |
| 上部 | 封面 76 + 间距 14：来源行（12 `textSecondary`，"夜航电台 · 第 87 期"）、标题（16 SemiBold，行高 1.4）、剩余时间（12 `brand` SemiBold） |
| 下部 | 进度条（4dp，占满剩余宽度）+ 时间（12 `textSecondary`，等宽数字，"38:12 / 61:05"）+ 播放键（48 圆形 `brand` 底，图标 20 `onBrand`） |
| 点击 | 上部整体 → `onOpen`（单集详情）；播放键 → `onTogglePlay`，`contentDescription` 为"继续播放 / 暂停" |

### 5.4 组件 UI 测试（`androidTest/.../designsystem/DesignSystemComponentsTest.kt`）

| 测试 | 断言 |
|---|---|
| `filterChips_selectAndTouchTarget` | 点击第 2 个标签回调 1；选中项 `assertIsSelected()`；每个标签高度 ≥ 48 |
| `buttons_smallSize_hasTouchTargetOf48` | `Small` 按钮的触控区 ≥ 48 |
| `podcastGrid_unreadBadge` | `unreadCount = 3` 时显示"3"，为 0 时不显示 |
| `episodeItem_longClick` | `performTouchInput { longClick() }` 触发 `onLongClick` |
| `inlineBanner_action` | 点击"重试"触发回调 |

### 5.5 组件总览预览

新建 `core/designsystem/component/DesignSystemGallery.kt`（只有 `@Preview`，不参与运行），按设计语言页的顺序排列所有组件，分浅色、深色两个预览，宽 390。用它和 [01-设计语言-QuietPaper.png](../EchoPod-设计稿/PNG/00-设计语言/01-设计语言-QuietPaper.png) 并排对比。

### 5.6 周验收（周五下午）

- [ ] `./gradlew :app:assembleDebug` 通过
- [ ] `./gradlew :app:testDebugUnitTest` 通过（新增 `CoverArtTest`）
- [ ] `./gradlew :app:connectedDebugAndroidTest` 通过（外壳、导航、组件）
- [ ] 模拟器上三种断点的迷你播放器与设计稿一致，浅 / 深色都检查
- [ ] 组件总览与设计语言页对比通过
- [ ] 全部新代码中没有 `MaterialTheme.colorScheme`，`Color(0x…)` 只出现在 `Color.kt`（`grep` 检查）
- [ ] 所有可点击元素触控区 ≥ 48dp
- [ ] 更新文档：路线图阶段 0、阶段 1 打勾；排期 §7 填写第 1 周实际完成与偏差

**提交**：
1. `feat(designsystem): add episode and podcast list items and continue listening card`
2. `test(designsystem): add component UI tests and gallery preview`
3. `docs: update roadmap and schedule after week 1`

---

## 本周新增 / 修改文件一览

| 文件 | 操作 | 日 |
|---|---|---|
| `core/designsystem/theme/Color.kt` | 新增 `shadow`、`warning`、`CoverPalette`、`coverPalettes` | 一、三 |
| `core/designsystem/theme/Type.kt` | 新增 `tabularNumbers()` | 三 |
| `res/drawable/ic_*.xml` | 新增约 25 个图标 | 一、三、四 |
| `res/values/strings.xml` | 迷你播放器、单集状态、通用操作文案 | 一、五 |
| `presentation/shell/MiniPlayer.kt` | 新增 | 一 |
| `presentation/shell/EchoPodApp.kt` | 接入 `miniPlayer` 槽位与 `hasMiniPlayer` | 一 |
| `presentation/navigation/EchoPodNavHost.kt` | Expanded 迷你播放器留白 | 一 |
| `presentation/feature/common/TopLevelPlaceholder.kt` | 列表测试标签 | 二 |
| `presentation/feature/home/HomeScreen.kt` | 导入按钮改用 `EchoPodButton` | 四 |
| `core/designsystem/component/` | `PodcastCover`、`CoverArt`、`EchoPodButton`、`SectionHeader`、`EchoPodProgressBar`、`FilterChipRow`、`InlineBanner`、`EpisodeListItem`、`PodcastGridItem`、`PodcastListItem`、`ContinueListeningCard`、`DesignSystemGallery` | 三 – 五 |
| `test/.../CoverArtTest.kt` | 新增 | 三 |
| `androidTest/.../EchoPodAppShellTest.kt`、`EchoPodAppTest.kt`、`DesignSystemComponentsTest.kt` | 新增 | 二、五 |

## 本周提交顺序

| # | 提交 | 日 |
|---|---|---|
| 1 | `feat(shell): add mini player with compact and medium styles` | 一 |
| 2 | `test(shell): add adaptive shell and navigation UI tests` | 二 |
| 3 | `feat(designsystem): add icon set from design` | 三 |
| 4 | `feat(designsystem): add cover palette tokens and PodcastCover` | 三 |
| 5 | `feat(designsystem): add button, section header, progress bar, filter chips and banner` | 四 |
| 6 | `feat(designsystem): add episode and podcast list items and continue listening card` | 五 |
| 7 | `test(designsystem): add component UI tests and gallery preview` | 五 |
| 8 | `docs: update roadmap and schedule after week 1` | 五 |

## 风险与应对

| 风险 | 应对 |
|---|---|
| UI 测试环境（模拟器启动、Runner 依赖）耗时超预期 | 周二上午先跑通一个最简单的测试再写其余用例；Runner 缺失时按 2.1 补依赖 |
| `StateRestorationTester` 与 NavHost 配合不稳定 | 该用例失败时先标 `@Ignore` 并记录原因，不阻塞 M0；改用模拟器手动旋转验证 |
| 封面首字在不同字体下位置偏差 | 以 96 / 56 / 44 三个尺寸的预览对比设计稿为准，微调系数，不追求像素级一致 |
| `warning` 深色值没有设计稿依据 | 先用 `#F0A060`，在本文档与 `Color.kt` 注释中标记"待设计确认" |
| 周四组件较多 | `InlineBanner` 可顺延到周五上午；周五的总览预览可顺延到第 2 周周一上午 |
