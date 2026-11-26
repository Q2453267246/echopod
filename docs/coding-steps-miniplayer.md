# EchoPod 编码步骤：迷你播放器（MiniPlayer）接入

> 架构：Clean Architecture + MVVM ｜ UI：Jetpack Compose + Material 3
>
> 前置文档：[coding-steps-navigation.md](coding-steps-navigation.md)（Step 8 链路、Step 9 外壳）
>
> 参考设计稿：
> - 手机首页（Compact）：[01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png)，HTML：`Main.dc.html`
> - 手机深色：[14-首页-深色.png](../EchoPod-设计稿/PNG/01-手机/14-首页-深色.png)，HTML：`HomeDark.dc.html`
> - 折叠屏展开竖向（Medium）：[02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png)，HTML：`FoldHome.dc.html`

---

## 0. 现状与目标

**已完成（不用再改）：**

| 层 | 文件 | 内容 |
|---|---|---|
| domain | `domain/playback/model/NowPlaying.kt` | 标题、播客名、倍速、进度、剩余分钟、是否在播 |
| domain | `domain/playback/repository/PlaybackRepository.kt` | `nowPlaying: Flow<NowPlaying?>`、`togglePlayPause()`、`seekBy(seconds)` |
| domain | `domain/playback/usecase/*` | `ObserveNowPlaying` / `TogglePlayback` / `SeekPlayback` |
| data | `data/playback/FakePlaybackRepository.kt` | 设计稿示例数据（夜航电台 · 1.2x · 剩余 23 分钟） |
| di | `di/PlaybackModule.kt` | `@Binds` 仓库接口 → Fake 实现 |
| presentation | `MainShellUiState` / `MainShellViewModel` | `nowPlaying` 已 combine 进 UiState；`TogglePlayback` / `SeekBack(-15)` / `SeekForward(+30)` 事件已处理 |
| presentation | `EchoPodAppShell` | 已有 `miniPlayer: @Composable BoxScope.(NavigationLayout) -> Unit` 槽位，位于内容 `Box` 内、NavHost 之后 |
| presentation | `EchoPodNavHost` | 已有 `hasMiniPlayer` 参数，按形态给页面加 `bottomExtraPadding`（72 / 84dp） |

**本文要做：**

1. 图标与文案资源（播放、暂停、快退 15、快进 30）。
2. `MiniPlayerStyle`：把 Compact / Medium 两套尺寸收拢为一处，供播放器与 NavHost 共用。
3. 纯函数文案格式化（倍速、副标题）+ JVM 单测。
4. 无状态 `MiniPlayer` Composable + 预览。
5. 在 `EchoPodApp` 接入槽位、给 `EchoPodNavHost` 传 `hasMiniPlayer`。
6. （可选）出现 / 消失动画。
7. ViewModel 单测补全、Compose UI 测试、人工验收。

**不做：** 真实播放（Media3）、全屏播放器页面（点击迷你播放器先留 TODO）、Expanded 双栏里停靠在详情栏底部的播放器（属于 `ListDetailPaneScaffold` 阶段）、真实封面图片加载。

**完成标准：**
1. Compact、Medium 下迷你播放器尺寸、颜色、位置与设计稿一致（浅色 + 深色）；Expanded 下外壳不渲染。
2. 播放 / 暂停、快退、快进点击后 UI 立即反映新状态；所有按钮触控区 ≥ 48dp。
3. 折叠 / 展开、旋转时播放状态不变、Tab 与滚动位置不丢；列表最后一项不被播放器遮挡。
4. `./gradlew :app:assembleDebug` 与 `./gradlew :app:testDebugUnitTest` 通过。

---

## 0.1 文件位置一览（新建 / 修改哪些文件、放在哪个包）

源码根目录：`app/src/main/java/com/echopod/`（包名前缀 `com.echopod`）；资源根目录：`app/src/main/res/`。

```text
app/src/main/
├── java/com/echopod/
│   └── presentation/
│       ├── navigation/
│       │   └── EchoPodNavHost.kt         # 修改：底部留白改用 MiniPlayerStyle（Step 2）
│       └── shell/
│           ├── MiniPlayerStyle.kt        # 新建：尺寸规格枚举 + miniPlayerStyleFor()（Step 2）
│           ├── MiniPlayerText.kt         # 新建：formatSpeed() / miniPlayerSubtitle()（Step 3）
│           ├── MiniPlayer.kt             # 新建：无状态 MiniPlayer Composable + 预览（Step 4）
│           └── EchoPodApp.kt             # 修改：接入 miniPlayer 槽位、hasMiniPlayer（Step 5、6）
└── res/
    ├── drawable/
    │   ├── ic_play.xml                   # 新建（Step 1）
    │   ├── ic_pause.xml                  # 新建（Step 1）
    │   ├── ic_replay.xml                 # 新建（Step 1）
    │   └── ic_forward.xml                # 新建（Step 1）
    └── values/
        └── strings.xml                   # 修改：追加 6 条迷你播放器文案（Step 1）

app/src/test/java/com/echopod/presentation/shell/
├── MiniPlayerStyleTest.kt                # 新建（Step 2）
├── MiniPlayerTextTest.kt                 # 新建（Step 3）
└── MainShellViewModelTest.kt             # 修改：补快退 / 快进用例（Step 7.1）

app/src/androidTest/java/com/echopod/presentation/shell/   # 目录目前不存在，需新建
└── MiniPlayerTest.kt                     # 新建（Step 7.3）
```

| 文件 | 完整路径 | `package` | 新建 / 修改 | 步骤 |
|---|---|---|---|---|
| `ic_play.xml` 等 4 个图标 | `app/src/main/res/drawable/` | —（资源，`R.drawable.ic_play` 等） | 新建 | Step 1 |
| `strings.xml` | `app/src/main/res/values/strings.xml` | —（资源） | 修改 | Step 1 |
| `MiniPlayerStyle.kt` | `app/src/main/java/com/echopod/presentation/shell/MiniPlayerStyle.kt` | `com.echopod.presentation.shell` | 新建 | Step 2 |
| `EchoPodNavHost.kt` | `app/src/main/java/com/echopod/presentation/navigation/EchoPodNavHost.kt` | `com.echopod.presentation.navigation` | 修改 | Step 2 |
| `MiniPlayerText.kt` | `app/src/main/java/com/echopod/presentation/shell/MiniPlayerText.kt` | `com.echopod.presentation.shell` | 新建 | Step 3 |
| `MiniPlayer.kt` | `app/src/main/java/com/echopod/presentation/shell/MiniPlayer.kt` | `com.echopod.presentation.shell` | 新建 | Step 4 |
| `EchoPodApp.kt` | `app/src/main/java/com/echopod/presentation/shell/EchoPodApp.kt` | `com.echopod.presentation.shell` | 修改 | Step 5、6 |
| `MiniPlayerStyleTest.kt` | `app/src/test/java/com/echopod/presentation/shell/MiniPlayerStyleTest.kt` | `com.echopod.presentation.shell` | 新建 | Step 2 |
| `MiniPlayerTextTest.kt` | `app/src/test/java/com/echopod/presentation/shell/MiniPlayerTextTest.kt` | `com.echopod.presentation.shell` | 新建 | Step 3 |
| `MainShellViewModelTest.kt` | `app/src/test/java/com/echopod/presentation/shell/MainShellViewModelTest.kt` | `com.echopod.presentation.shell` | 修改 | Step 7.1 |
| `MiniPlayerTest.kt` | `app/src/androidTest/java/com/echopod/presentation/shell/MiniPlayerTest.kt` | `com.echopod.presentation.shell` | 新建 | Step 7.3 |

**为什么都放在 `presentation/shell`，而不是 `core/designsystem/component` 或 `domain`：**

- `MiniPlayer` 直接接收 domain 模型 `NowPlaying`，而 `core/designsystem` 只放与业务无关的通用组件（如 `EchoPodNavigationBar`），不应依赖 `domain`；
- 它只被应用外壳使用（`EchoPodAppShell` 的 `miniPlayer` 槽位），与 `MainShellViewModel` / `MainShellUiState` / `MainShellEvent` 同属一个功能单元；
- `MiniPlayerStyle` 依赖 `Dp` 与 `NavigationLayout`，`MiniPlayerText` 是 UI 文案格式，都属于展示逻辑，**不能**放进 `domain/`（domain 必须是纯 Kotlin、不认识 UI 单位与导航形态）；
- 测试类与被测类保持同一个包名（`com.echopod.presentation.shell`），这样才能访问 `internal` 的 `formatSpeed()` / `miniPlayerSubtitle()`。

**不需要改动的文件**（Step 8 已完成）：`domain/playback/**`、`data/playback/FakePlaybackRepository.kt`、`di/PlaybackModule.kt`、`presentation/shell/MainShellUiState.kt`、`MainShellViewModel.kt`、`EchoPodAppShell.kt`。

---

## 1. 设计稿规格（从 HTML 源文件中提取）

| 项 | Compact（`Main.dc.html`） | Medium（`FoldHome.dc.html`） |
|---|---|---|
| 位置 | 内容区底部，左右 `8dp`，距导航栏顶部 `8dp` | 内容区底部，左右 `24dp`，底 `16dp`（+ 系统手势条） |
| 卡片 | 高 `64dp`，圆角 `16dp` | 高 `68dp`，圆角 `18dp` |
| 背景 / 阴影 | `card`（`#FFFFFF` / `#22231F`），双层柔和阴影 | 同左 |
| 内边距 | 左 `10dp`、右 `4dp`，子项间距 `10dp` | 左 `12dp`、右 `8dp`，子项间距 `12dp` |
| 封面 | `44dp`，圆角 `8dp` | `46dp`，圆角 `8dp` |
| 标题 | `14sp` / 600，单行省略，`textPrimary` | 同左 |
| 副标题 | `12sp`，`textSecondary`，"夜航电台 · 1.2x" | "夜航电台 · 1.2x · 剩余 23 分" |
| 按钮 | 只有播放/暂停：`48dp` 透明底，图标 `24dp`，`textPrimary` | 快退 15 · 播放/暂停 · 快进 30；播放/暂停为 `48dp` **墨色圆形**（`inkButton` 底、`onInkButton` 图标 `22dp`），快退/快进图标 `26dp`、描边 `1.7` |
| 进度线 | 贴底 `2dp`，左右缩进 `12dp`；轨道 `outline`，已播 `brand` | 同左，左右缩进 `14dp` |
| 点击区域 | 封面 + 文字整块可点 → 全屏播放器 | 同左 |

> 颜色全部有对应令牌：轨道 `#E3DFD6 / #3A3B36` = `outline`，已播 `#2E6B66 / #86C6BD` = `brand`，墨色按钮 `#1C1B19 / #ECEAE4` = `inkButton`。

**Expanded：** 设计稿中播放器停靠在右侧详情栏底部，属于首页双栏布局 —— 本阶段外壳**不渲染**迷你播放器（`EchoPodNavHost` 里 Expanded 的 `bottomExtraPadding` 已经是 `0dp`）。

---

## Step 1：图标与文案资源（`res/`）

> **文件**
> - 新建 `app/src/main/res/drawable/ic_play.xml`、`ic_pause.xml`、`ic_replay.xml`、`ic_forward.xml`（资源文件，没有 `package`，代码中通过 `com.echopod.R.drawable.xxx` 引用）
> - 修改 `app/src/main/res/values/strings.xml`（追加到 `</resources>` 之前）

### 1.1 Vector Drawable（`app/src/main/res/drawable/`）

路径直接取自设计稿 SVG（24×24 视口）。颜色写 `#FF000000`，运行时由 `Icon(tint = ...)` / `LocalContentColor` 着色。

`ic_play.xml`（实心三角）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 播放：实心三角（Quiet Paper） -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M8,5.5v13l11,-6.5z" />
</vector>
```

`ic_pause.xml`（两根圆角竖条，`rect x=6.5 / 13.9, w=3.6, h=14, rx=1.2`）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 暂停：两根圆角竖条（Quiet Paper） -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M7.7,5h1.2a1.2,1.2 0,0 1,1.2,1.2v11.6a1.2,1.2 0,0 1,-1.2,1.2h-1.2a1.2,1.2 0,0 1,-1.2,-1.2V6.2a1.2,1.2 0,0 1,1.2,-1.2z" />
    <path
        android:fillColor="#FF000000"
        android:pathData="M15.1,5h1.2a1.2,1.2 0,0 1,1.2,1.2v11.6a1.2,1.2 0,0 1,-1.2,1.2h-1.2a1.2,1.2 0,0 1,-1.2,-1.2V6.2a1.2,1.2 0,0 1,1.2,-1.2z" />
</vector>
```

`ic_replay.xml`（逆时针箭头，数字 "15" 由 Compose 叠加，见 Step 4）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 快退：逆时针圆弧箭头，1.7 描边；秒数由 Compose Text 叠加 -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:pathData="M4.5,12a7.5,7.5 0,1 0,2.2 -5.3"
        android:strokeWidth="1.7"
        android:strokeColor="#FF000000"
        android:strokeLineCap="round"
        android:strokeLineJoin="round" />
    <path
        android:pathData="M4,3.5v3.7h3.7"
        android:strokeWidth="1.7"
        android:strokeColor="#FF000000"
        android:strokeLineCap="round"
        android:strokeLineJoin="round" />
</vector>
```

`ic_forward.xml`（顺时针箭头，数字 "30" 由 Compose 叠加）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 快进：顺时针圆弧箭头，1.7 描边；秒数由 Compose Text 叠加 -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:pathData="M19.5,12a7.5,7.5 0,1 1,-2.2 -5.3"
        android:strokeWidth="1.7"
        android:strokeColor="#FF000000"
        android:strokeLineCap="round"
        android:strokeLineJoin="round" />
    <path
        android:pathData="M20,3.5v3.7h-3.7"
        android:strokeWidth="1.7"
        android:strokeColor="#FF000000"
        android:strokeLineCap="round"
        android:strokeLineJoin="round" />
</vector>
```

> 为什么数字不画进 Vector：设计稿里 "15 / 30" 是 SVG `<text>`，Vector Drawable 不支持文字；手工描数字路径成本高且难改。Compose 叠一个 `Text` 即可，并对它清空无障碍语义，避免 TalkBack 读出"快退 15 秒，15"。

### 1.2 文案（`res/values/strings.xml`）

```xml
    <!-- 迷你播放器 -->
    <string name="action_play">播放</string>
    <string name="action_pause">暂停</string>
    <string name="action_seek_back">快退 15 秒</string>
    <string name="action_seek_forward">快进 30 秒</string>
    <string name="action_open_player">打开播放器</string>
    <string name="mini_player_remaining">剩余 %1$d 分</string>
```

**提交：** `feat(res): add mini player icons and strings`

---

## Step 2：`MiniPlayerStyle`（`presentation/shell/MiniPlayerStyle.kt`）

> **文件**
> - 新建 `app/src/main/java/com/echopod/presentation/shell/MiniPlayerStyle.kt`，`package com.echopod.presentation.shell`
> - 修改 `app/src/main/java/com/echopod/presentation/navigation/EchoPodNavHost.kt`，`package com.echopod.presentation.navigation`（新增 `import com.echopod.presentation.shell.miniPlayerStyleFor`）
> - 新建 `app/src/test/java/com/echopod/presentation/shell/MiniPlayerStyleTest.kt`，`package com.echopod.presentation.shell`

两套尺寸差异较多（高度、圆角、封面、边距、是否有快退快进），用一个枚举收拢，**同时作为 NavHost 底部留白的唯一来源** —— 现在 `EchoPodNavHost` 里写死的 72 / 84dp 与播放器尺寸是两处独立数字，改一处忘一处就会遮挡列表。

```kotlin
package com.echopod.presentation.shell

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.echopod.core.adaptive.NavigationLayout

/**
 * 迷你播放器的两种尺寸规格（数值取自 Main.dc.html / FoldHome.dc.html）。
 *
 * @property height 卡片高度。
 * @property cornerRadius 卡片圆角。
 * @property coverSize 封面边长。
 * @property paddingStart 卡片左内边距。
 * @property paddingEnd 卡片右内边距。
 * @property spacing 子项间距。
 * @property progressInset 底部进度线左右缩进。
 * @property horizontalMargin 卡片与内容区左右边缘的距离。
 * @property bottomMargin 卡片与内容区底边（Compact 为导航栏顶部）的距离。
 * @property showSkipButtons 是否显示快退 15 / 快进 30。
 * @property showRemaining 副标题是否显示"剩余 N 分"。
 * @property filledPlayButton 播放/暂停是否为墨色圆形按钮。
 */
enum class MiniPlayerStyle(
    val height: Dp,
    val cornerRadius: Dp,
    val coverSize: Dp,
    val paddingStart: Dp,
    val paddingEnd: Dp,
    val spacing: Dp,
    val progressInset: Dp,
    val horizontalMargin: Dp,
    val bottomMargin: Dp,
    val showSkipButtons: Boolean,
    val showRemaining: Boolean,
    val filledPlayButton: Boolean,
) {
    Compact(
        height = 64.dp, cornerRadius = 16.dp, coverSize = 44.dp,
        paddingStart = 10.dp, paddingEnd = 4.dp, spacing = 10.dp, progressInset = 12.dp,
        horizontalMargin = 8.dp, bottomMargin = 8.dp,
        showSkipButtons = false, showRemaining = false, filledPlayButton = false,
    ),
    Medium(
        height = 68.dp, cornerRadius = 18.dp, coverSize = 46.dp,
        paddingStart = 12.dp, paddingEnd = 8.dp, spacing = 12.dp, progressInset = 14.dp,
        horizontalMargin = 24.dp, bottomMargin = 16.dp,
        showSkipButtons = true, showRemaining = true, filledPlayButton = true,
    );

    /** 页面列表底部需要额外留出的高度：卡片高度 + 底边距（Compact 72dp，Medium 84dp）。 */
    val reservedHeight: Dp get() = height + bottomMargin
}

/**
 * 导航形态 → 迷你播放器规格。Expanded 返回 null：外壳不渲染，由双栏详情栏负责。
 *
 * 纯函数，可直接在 JVM 单测中验证。
 */
fun miniPlayerStyleFor(layout: NavigationLayout): MiniPlayerStyle? = when (layout) {
    NavigationLayout.BottomBar -> MiniPlayerStyle.Compact
    NavigationLayout.MediumRail -> MiniPlayerStyle.Medium
    NavigationLayout.ExpandedRail -> null
}
```

然后让 `EchoPodNavHost` 改用它（`presentation/navigation/EchoPodNavHost.kt`），删掉写死的 72 / 84 / 0：

```kotlin
import com.echopod.presentation.shell.miniPlayerStyleFor

    // 迷你播放器占位高度与 MiniPlayerStyle 同源；Expanded 返回 null → 0dp
    val bottomExtraPadding =
        if (hasMiniPlayer) miniPlayerStyleFor(layout)?.reservedHeight ?: 0.dp else 0.dp
```

**JVM 单测**（`app/src/test/java/com/echopod/presentation/shell/MiniPlayerStyleTest.kt`）：

```kotlin
package com.echopod.presentation.shell

import androidx.compose.ui.unit.dp
import com.echopod.core.adaptive.NavigationLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiniPlayerStyleTest {

    @Test
    fun styleFor_mapsEachNavigationLayout() {
        assertEquals(MiniPlayerStyle.Compact, miniPlayerStyleFor(NavigationLayout.BottomBar))
        assertEquals(MiniPlayerStyle.Medium, miniPlayerStyleFor(NavigationLayout.MediumRail))
        assertNull(miniPlayerStyleFor(NavigationLayout.ExpandedRail))
    }

    @Test
    fun reservedHeight_matchesDesign() {
        assertEquals(72.dp, MiniPlayerStyle.Compact.reservedHeight) // 64 + 8
        assertEquals(84.dp, MiniPlayerStyle.Medium.reservedHeight)  // 68 + 16
    }
}
```

---

## Step 3：文案格式化（`presentation/shell/MiniPlayerText.kt`）

> **文件**
> - 新建 `app/src/main/java/com/echopod/presentation/shell/MiniPlayerText.kt`，`package com.echopod.presentation.shell`
> - 新建 `app/src/test/java/com/echopod/presentation/shell/MiniPlayerTextTest.kt`，`package com.echopod.presentation.shell`（必须与被测文件同包：两个函数是 `internal`，同模块可见，同包可省去 import）

倍速与副标题拼接写成纯函数，方便 JVM 单测。**不要**用 `String.format("%.1f")`：在德语等区域会输出 `1,2x`。

```kotlin
package com.echopod.presentation.shell

import kotlin.math.roundToInt

/** 播放倍速显示：1.0 → "1x"，1.2 → "1.2x"，1.25 → "1.25x"，0.75 → "0.75x"。与系统区域无关。 */
internal fun formatSpeed(speed: Float): String {
    val hundredths = (speed * 100).roundToInt()
    val whole = hundredths / 100
    val fraction = hundredths % 100
    return when {
        fraction == 0 -> "${whole}x"
        fraction % 10 == 0 -> "$whole.${fraction / 10}x"
        else -> "$whole.${fraction.toString().padStart(2, '0')}x"
    }
}

/**
 * 副标题："夜航电台 · 1.2x"；传入 [remaining] 时追加为 "夜航电台 · 1.2x · 剩余 23 分"。
 *
 * @param remaining 已经本地化好的"剩余 N 分"文案（由 Composable 用 stringResource 生成）；null 表示不显示。
 */
internal fun miniPlayerSubtitle(podcastTitle: String, speed: Float, remaining: String?): String =
    listOfNotNull(podcastTitle, formatSpeed(speed), remaining).joinToString(" · ")
```

**JVM 单测**（`app/src/test/java/com/echopod/presentation/shell/MiniPlayerTextTest.kt`）：

```kotlin
package com.echopod.presentation.shell

import org.junit.Assert.assertEquals
import org.junit.Test

class MiniPlayerTextTest {

    @Test
    fun formatSpeed_trimsTrailingZeros() {
        assertEquals("1x", formatSpeed(1.0f))
        assertEquals("1.2x", formatSpeed(1.2f))
        assertEquals("1.25x", formatSpeed(1.25f))
        assertEquals("0.75x", formatSpeed(0.75f))
        assertEquals("2x", formatSpeed(2.0f))
    }

    @Test
    fun subtitle_withoutRemaining() {
        assertEquals("夜航电台 · 1.2x", miniPlayerSubtitle("夜航电台", 1.2f, remaining = null))
    }

    @Test
    fun subtitle_withRemaining() {
        assertEquals(
            "夜航电台 · 1.2x · 剩余 23 分",
            miniPlayerSubtitle("夜航电台", 1.2f, remaining = "剩余 23 分"),
        )
    }
}
```

**提交：** `feat(shell): add mini player style and text formatting`

---

## Step 4：无状态 `MiniPlayer`（`presentation/shell/MiniPlayer.kt`）

> **文件**
> - 新建 `app/src/main/java/com/echopod/presentation/shell/MiniPlayer.kt`，`package com.echopod.presentation.shell`
> - 依赖：`com.echopod.domain.playback.model.NowPlaying`（domain 模型）、`com.echopod.core.designsystem.theme.EchoPodTheme`（颜色令牌）、`com.echopod.R`（图标与文案）
> - 同包内直接使用 Step 2 的 `MiniPlayerStyle` 和 Step 3 的 `miniPlayerSubtitle()`，无需 import

要点：

- **只接收数据与回调**，不读 ViewModel、不读窗口尺寸；规格由 `style` 决定。
- 颜色全部来自 `EchoPodTheme.colors`；M3 的 `IconButton` 显式传 `colors`。
- 所有按钮 `48dp`；封面 + 文字整块是一个可点击区域（`onClickLabel` 供 TalkBack 朗读"打开播放器"）。
- 封面暂用"播客名首字 + 品牌容器色"占位：`NowPlaying` 还没有封面字段，接入真实封面时再加 `artworkUrl` 并引入图片库，本阶段不引入新依赖。

```kotlin
package com.echopod.presentation.shell

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme
import com.echopod.domain.playback.model.NowPlaying

/** 迷你播放器卡片阴影高度（设计稿为两层柔和阴影，近似取 6dp）。 */
private val MiniPlayerElevation = 6.dp

/** 底部进度线粗细。 */
private val ProgressHeight = 2.dp

/**
 * 悬浮迷你播放器（无状态）。
 *
 * 设计稿：PNG/01-手机/01-首页.png（Compact）、PNG/02-折叠屏/02-首页-竖向.png（Medium）。
 * 摆放位置（对齐、外边距、系统 inset）由调用方通过 [modifier] 决定，组件本身只负责卡片内部。
 *
 * @param nowPlaying 正在播放的单集。
 * @param style 尺寸规格，见 [MiniPlayerStyle]。
 * @param onClick 点击封面 / 标题区域，进入全屏播放器。
 * @param onTogglePlayback 播放 / 暂停。
 * @param onSeekBack 快退 15 秒（仅 Medium 显示）。
 * @param onSeekForward 快进 30 秒（仅 Medium 显示）。
 */
@Composable
fun MiniPlayer(
    nowPlaying: NowPlaying,
    style: MiniPlayerStyle,
    onClick: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EchoPodTheme.colors
    val shape = RoundedCornerShape(style.cornerRadius)
    val remaining =
        if (style.showRemaining) stringResource(R.string.mini_player_remaining, nowPlaying.remainingMinutes) else null
    val subtitle = miniPlayerSubtitle(nowPlaying.podcastTitle, nowPlaying.speed, remaining)

    Box(
        modifier
            .fillMaxWidth()
            .height(style.height)
            .shadow(MiniPlayerElevation, shape)   // shadow 默认 clip = true，后续背景与内容都裁成圆角
            .background(colors.card),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = style.paddingStart, end = style.paddingEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(style.spacing),
        ) {
            // 封面 + 文字：整块可点击 → 全屏播放器
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClickLabel = stringResource(R.string.action_open_player), onClick = onClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(style.spacing),
            ) {
                CoverPlaceholder(nowPlaying.podcastTitle, style.coverSize)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = nowPlaying.episodeTitle,
                        style = MaterialTheme.typography.titleSmall,   // 14 / 600
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,    // 12 / 400
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (style.showSkipButtons) {
                SkipButton(
                    iconRes = R.drawable.ic_replay,
                    seconds = 15,
                    contentDescription = stringResource(R.string.action_seek_back),
                    onClick = onSeekBack,
                )
            }
            PlayPauseButton(
                isPlaying = nowPlaying.isPlaying,
                filled = style.filledPlayButton,
                onClick = onTogglePlayback,
            )
            if (style.showSkipButtons) {
                SkipButton(
                    iconRes = R.drawable.ic_forward,
                    seconds = 30,
                    contentDescription = stringResource(R.string.action_seek_forward),
                    onClick = onSeekForward,
                )
            }
        }

        MiniPlayerProgress(
            progress = nowPlaying.progress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = style.progressInset),
        )
    }
}

/** 封面占位：品牌容器底色 + 播客名首字。接入真实封面后替换为图片。 */
@Composable
private fun CoverPlaceholder(podcastTitle: String, size: Dp) {
    val colors = EchoPodTheme.colors
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.brandContainer),
    ) {
        Text(
            text = podcastTitle.take(1),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 5.dp, bottom = 3.dp),
            color = colors.onBrandContainer,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 播放 / 暂停按钮，48dp。
 * - Compact：透明底，图标 24dp textPrimary；
 * - Medium（[filled]）：墨色圆形，图标 22dp onInkButton。
 */
@Composable
private fun PlayPauseButton(isPlaying: Boolean, filled: Boolean, onClick: () -> Unit) {
    val colors = EchoPodTheme.colors
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (filled) colors.inkButton else Color.Transparent,
            contentColor = if (filled) colors.onInkButton else colors.textPrimary,
        ),
    ) {
        Icon(
            painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = stringResource(if (isPlaying) R.string.action_pause else R.string.action_play),
            modifier = Modifier.size(if (filled) 22.dp else 24.dp),
        )
    }
}

/** 快退 / 快进按钮：26dp 圆弧箭头 + 居中的秒数（秒数不参与无障碍朗读）。 */
@Composable
private fun SkipButton(
    @DrawableRes iconRes: Int,
    seconds: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    // 数字是图标的一部分，用 dp 换算字号，不随系统字体缩放撑破图标
    val labelSize = with(LocalDensity.current) { 7.5.dp.toSp() }
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(contentColor = EchoPodTheme.colors.textPrimary),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = seconds.toString(),
                modifier = Modifier
                    .offset(y = 1.dp)                 // 设计稿数字基线略低于圆心
                    .clearAndSetSemantics {},
                fontSize = labelSize,
                lineHeight = labelSize,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** 贴底 2dp 进度线：轨道 outline，已播部分 brand。 */
@Composable
private fun MiniPlayerProgress(progress: Float, modifier: Modifier = Modifier) {
    val colors = EchoPodTheme.colors
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(ProgressHeight)
            .progressSemantics(value)
            .background(colors.outline),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(value)
                .background(colors.brand),
        )
    }
}

// ---- 预览：与 01-首页.png / 14-首页-深色.png / 02-首页-竖向.png 对比 ----

private val PreviewNowPlaying = NowPlaying(
    episodeTitle = "在深夜写代码的人，都在想些什么",
    podcastTitle = "夜航电台",
    speed = 1.2f,
    progress = 0.62f,
    remainingMinutes = 23,
    isPlaying = true,
)

@Preview(name = "MiniPlayer · Compact · Light", widthDp = 390)
@Composable
private fun MiniPlayerCompactLightPreview() {
    EchoPodTheme(darkTheme = false) {
        MiniPlayer(PreviewNowPlaying, MiniPlayerStyle.Compact, {}, {}, {}, {}, Modifier.padding(8.dp))
    }
}

@Preview(name = "MiniPlayer · Compact · Dark", widthDp = 390)
@Composable
private fun MiniPlayerCompactDarkPreview() {
    EchoPodTheme(darkTheme = true) {
        MiniPlayer(PreviewNowPlaying, MiniPlayerStyle.Compact, {}, {}, {}, {}, Modifier.padding(8.dp))
    }
}

@Preview(name = "MiniPlayer · Medium · Light", widthDp = 616)
@Composable
private fun MiniPlayerMediumLightPreview() {
    EchoPodTheme(darkTheme = false) {
        MiniPlayer(PreviewNowPlaying, MiniPlayerStyle.Medium, {}, {}, {}, {}, Modifier.padding(24.dp))
    }
}
```

> **注意**
> - `Color.Transparent` 是 Compose 常量，不属于"硬编码 `Color(0x...)`"；`Color.kt` 本身也在用。
> - `shadow()` 的阴影色默认为黑色，深色模式下几乎不可见，这与设计稿深色版（阴影更重但背景更深）观感一致，不需要额外处理。
> - 预览外层的 `padding` 只是为了让阴影露出来。

**提交：** `feat(shell): add stateless MiniPlayer composable`

---

## Step 5：在 `EchoPodApp` 接入（`presentation/shell/EchoPodApp.kt`）

> **文件**
> - 修改 `app/src/main/java/com/echopod/presentation/shell/EchoPodApp.kt`，`package com.echopod.presentation.shell`
> - `MiniPlayer`、`miniPlayerStyleFor`、`MainShellEvent` 都在同一个包，无需 import；只需补下面列出的 Compose 布局 import
> - `MiniPlayerInsets` 是 `private` 顶层属性，写在 `EchoPodApp.kt` 文件末尾，不单独建文件

替换文件中的 `TODO(Step 8)`：

```kotlin
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

    EchoPodAppShell(
        navigationLayout = appState.navigationLayout,
        items = items,
        selectedIndex = selectedIndex,
        onItemClick = { index -> appState.navigateToTopLevel(destinations[index]) },
        onImportClick = onImportClick,
        miniPlayer = { layout ->
            val style = miniPlayerStyleFor(layout)
            val nowPlaying = uiState.nowPlaying
            if (style != null && nowPlaying != null) {
                MiniPlayer(
                    nowPlaying = nowPlaying,
                    style = style,
                    onClick = { /* TODO(全屏播放器)：appState.navigateToPlayer() */ },
                    onTogglePlayback = { viewModel.onEvent(MainShellEvent.TogglePlayback) },
                    onSeekBack = { viewModel.onEvent(MainShellEvent.SeekBack) },
                    onSeekForward = { viewModel.onEvent(MainShellEvent.SeekForward) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(MiniPlayerInsets)
                        .padding(horizontal = style.horizontalMargin)
                        .padding(bottom = style.bottomMargin),
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
```

文件底部加一个私有属性：

```kotlin
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
```

**为什么这样摆：**

- 槽位 lambda 的接收者是 `BoxScope`，所以可以直接 `Modifier.align(BottomCenter)`；它在 `content()` **之后**调用，绘制在 NavHost 之上。
- `content()`（NavHost）的调用位置不变，播放器出现 / 消失、形态切换都不会重建 NavHost —— Tab 与滚动位置保持。
- 页面列表底部的 `windowInsetsBottomHeight(navigationBars)` + `bottomExtraPadding(reservedHeight)` 正好等于"系统 inset + 卡片高度 + 底边距"，最后一项不会被遮挡。
- 播放状态在 `@Singleton` 仓库里，`MainShellViewModel` 作用域为 Activity；折叠 / 展开导致 Activity 重建时 ViewModel 保留，`collectAsStateWithLifecycle` 首帧即拿到当前值，播放器不会闪一下。

**提交：** `feat(shell): show mini player in compact and medium layouts`

---

## Step 6（可选）：出现 / 消失动画

> **文件**
> - 修改 `app/src/main/java/com/echopod/presentation/shell/EchoPodApp.kt`，`package com.echopod.presentation.shell`（替换 Step 5 写的 `miniPlayer = { ... }` lambda；`MiniPlayerAnimMillis` 常量放在文件顶部 `private const val`）

Quiet Paper 规范：底部浮层 300ms。用 `AnimatedVisibility` 包住 Step 5 的 `MiniPlayer`；退出动画期间 `uiState.nowPlaying` 已经是 null，所以要保留最后一次非空值继续绘制：

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

private const val MiniPlayerAnimMillis = 300

        miniPlayer = { layout ->
            val style = miniPlayerStyleFor(layout)
            val nowPlaying = uiState.nowPlaying
            // 先写后读：退出动画期间继续用最后一次非空值绘制
            val lastNowPlaying = remember { mutableStateOf(nowPlaying) }
            if (nowPlaying != null) lastNowPlaying.value = nowPlaying

            AnimatedVisibility(
                visible = style != null && nowPlaying != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(MiniPlayerAnimMillis)) { it } + fadeIn(tween(MiniPlayerAnimMillis)),
                exit = slideOutVertically(tween(MiniPlayerAnimMillis)) { it } + fadeOut(tween(MiniPlayerAnimMillis)),
            ) {
                val shown = lastNowPlaying.value ?: return@AnimatedVisibility
                val shownStyle = style ?: MiniPlayerStyle.Compact   // 仅在切到 Expanded 的退出动画中用到
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
        },
```

- `AnimatedVisibility(visible = true)` 首次组合不播放入场动画，所以折叠 / 旋转重建时播放器直接出现，不会每次都滑入；只有冷启动（`stateIn` 初始值为 null → 仓库发出数据）时滑入一次。
- 如果切到 Expanded 时不想看到退出动画，把 `style != null` 从 `visible` 中拿掉，改为外层 `if (style != null)`。

**提交：** `feat(shell): animate mini player visibility`

---

## Step 7：测试

> **文件**
> - 修改 `app/src/test/java/com/echopod/presentation/shell/MainShellViewModelTest.kt`，`package com.echopod.presentation.shell`（JVM 单测，`src/test`）
> - 新建 `app/src/androidTest/java/com/echopod/presentation/shell/MiniPlayerTest.kt`，`package com.echopod.presentation.shell`（仪器测试，`src/androidTest`；该目录目前不存在，需要手动新建 `androidTest/java/com/echopod/presentation/shell/` 整条路径）
> - 区分：`src/test` 跑在本机 JVM，不能用 Compose UI；`src/androidTest` 跑在设备 / 模拟器上，`createComposeRule()` 只能放这里

### 7.1 ViewModel 单测补全（`MainShellViewModelTest.kt`）

已有 `TogglePlayback`，补上快退 / 快进事件：

```kotlin
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
```

### 7.2 JVM 单测汇总（`./gradlew :app:testDebugUnitTest`）

| 测试类 | 覆盖内容 |
|---|---|
| `MiniPlayerStyleTest`（新） | 形态 → 规格映射；Expanded 为 null；`reservedHeight` 72 / 84dp |
| `MiniPlayerTextTest`（新） | 倍速格式（去尾零、与区域无关）；副标题有无"剩余" |
| `MainShellViewModelTest`（补） | 快退 / 快进事件改变进度 |
| `PlaybackUseCasesTest`（已有） | 三个 UseCase 及进度边界 |

### 7.3 Compose UI 测试（`app/src/androidTest/java/com/echopod/presentation/shell/MiniPlayerTest.kt`）

依赖 `compose-ui-test-junit4`、`androidx-junit` 已在 `build.gradle.kts` 中，无需新增。文案用资源读取，避免测试里硬编码中文。

```kotlin
package com.echopod.presentation.shell

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme
import com.echopod.domain.playback.model.NowPlaying
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniPlayerTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int) = context.getString(id)

    private val sample = NowPlaying(
        episodeTitle = "在深夜写代码的人，都在想些什么",
        podcastTitle = "夜航电台",
        speed = 1.2f,
        progress = 0.5f,
        remainingMinutes = 23,
        isPlaying = true,
    )

    private fun setMiniPlayer(
        style: MiniPlayerStyle,
        nowPlaying: NowPlaying = sample,
        onToggle: () -> Unit = {},
        onBack: () -> Unit = {},
        onForward: () -> Unit = {},
    ) = rule.setContent {
        EchoPodTheme {
            MiniPlayer(nowPlaying, style, onClick = {}, onToggle, onBack, onForward)
        }
    }

    @Test
    fun compact_showsPauseOnly() {
        setMiniPlayer(MiniPlayerStyle.Compact)

        rule.onNodeWithText(sample.episodeTitle).assertIsDisplayed()
        rule.onNodeWithText("夜航电台 · 1.2x").assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.action_pause)).assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.action_seek_back)).assertDoesNotExist()
        rule.onNodeWithContentDescription(str(R.string.action_seek_forward)).assertDoesNotExist()
    }

    @Test
    fun medium_showsSkipButtonsAndRemaining() {
        setMiniPlayer(MiniPlayerStyle.Medium)

        rule.onNodeWithText("夜航电台 · 1.2x · 剩余 23 分").assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.action_seek_back)).assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.action_seek_forward)).assertIsDisplayed()
    }

    @Test
    fun paused_showsPlayIcon() {
        setMiniPlayer(MiniPlayerStyle.Compact, nowPlaying = sample.copy(isPlaying = false))

        rule.onNodeWithContentDescription(str(R.string.action_play)).assertIsDisplayed()
    }

    @Test
    fun buttons_forwardClicks() {
        var toggles = 0
        var backs = 0
        var forwards = 0
        setMiniPlayer(
            MiniPlayerStyle.Medium,
            onToggle = { toggles++ },
            onBack = { backs++ },
            onForward = { forwards++ },
        )

        rule.onNodeWithContentDescription(str(R.string.action_pause)).performClick()
        rule.onNodeWithContentDescription(str(R.string.action_seek_back)).performClick()
        rule.onNodeWithContentDescription(str(R.string.action_seek_forward)).performClick()

        assertEquals(1, toggles)
        assertEquals(1, backs)
        assertEquals(1, forwards)
    }

    @Test
    fun buttons_meetMinimumTouchTarget() {
        setMiniPlayer(MiniPlayerStyle.Medium)

        listOf(R.string.action_pause, R.string.action_seek_back, R.string.action_seek_forward).forEach { id ->
            rule.onNodeWithContentDescription(str(id))
                .assertWidthIsAtLeast(48.dp)
                .assertHeightIsAtLeast(48.dp)
        }
    }
}
```

> `IconButton` 会合并子节点语义，`onNodeWithContentDescription` 找到的是 48dp 的按钮节点本身，所以尺寸断言测的是触控区，而不是 26dp 的图标。

运行（需要模拟器或真机）：

```sh
./gradlew :app:connectedDebugAndroidTest
```

**提交：** `test(shell): cover mini player style, text and UI`

---

## Step 8：人工验收清单

推荐 Android Studio 的 **Pixel Fold** 或 **Resizable (Experimental)** 模拟器。

- [ ] **Compact**：卡片左右 8dp、距底部导航栏 8dp、高 64dp、圆角 16dp；只有一个暂停按钮；与 [01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png) 对比。
- [ ] **Compact 深色**：卡片 `#22231F`、进度轨道 `#3A3B36`、已播 `#86C6BD`；与 [14-首页-深色.png](../EchoPod-设计稿/PNG/01-手机/14-首页-深色.png) 对比。
- [ ] **Medium**：左右 24dp、底 16dp、高 68dp、圆角 18dp；快退 15 · 墨色圆形暂停 · 快进 30；副标题含"剩余 23 分"；与 [02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png) 对比。
- [ ] **Expanded**：外壳内不出现迷你播放器；列表底部没有多余留白。
- [ ] **交互**：点暂停 → 图标变播放；快退 / 快进 → 进度线移动；点封面或标题有水波纹（全屏播放器待做）。
- [ ] **遮挡**：各 Tab 列表滚到底，最后一项完整露出在播放器上方。
- [ ] **连续性**：暂停状态下折叠 / 展开 / 旋转 → 仍为暂停，进度不变，当前 Tab 与滚动位置不变。
- [ ] **Edge-to-edge**：Medium 下卡片不压在手势条上；横屏三键导航时不被右侧导航栏挡住。
- [ ] **TalkBack**：播放器标题区域朗读"…，双击以打开播放器"；按钮分别朗读"暂停 / 快退 15 秒 / 快进 30 秒"，不会多读出"15""30"。
- [ ] **大字体**（系统字体 200%）：标题、副标题单行省略，不撑高卡片；"15 / 30"不溢出图标。

---

## 提交顺序一览

| # | 提交 | 可验证点 |
|---|---|---|
| 1 | `feat(res): add mini player icons and strings` | 编译通过 |
| 2 | `feat(shell): add mini player style and text formatting` | `MiniPlayerStyleTest`、`MiniPlayerTextTest` 通过；NavHost 留白改为同源 |
| 3 | `feat(shell): add stateless MiniPlayer composable` | 预览与设计稿一致（浅 / 深、Compact / Medium） |
| 4 | `feat(shell): show mini player in compact and medium layouts` | 设备上三种形态表现正确 |
| 5 | `feat(shell): animate mini player visibility`（可选） | 冷启动滑入，折叠 / 旋转不重复播放动画 |
| 6 | `test(shell): cover mini player style, text and UI` | JVM 单测 + `connectedDebugAndroidTest` 通过 |

每次提交前执行：

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

---

## 下一阶段（本文档之外）

1. **全屏播放器**：新增 `PlayerRoute` 与 `appState.navigateToPlayer()`，替换 `onClick` 中的 TODO；页面切换用 300ms 动画。设计稿：`08-全屏播放器.png`、`Player.dc.html`。
2. **Expanded 双栏**：首页 / 订阅改用 `ListDetailPaneScaffold`，迷你播放器停靠在详情栏底部，可复用 `MiniPlayer` 并新增一个 `MiniPlayerStyle.Docked`。
3. **真实封面**：`NowPlaying` 增加 `artworkUrl`，`CoverPlaceholder` 换成图片加载（届时再评估引入 Coil）。
4. **真实播放**：Media3 `MediaSessionService` 实现 `PlaybackRepository`，只改 `PlaybackModule` 的绑定，`MiniPlayer` 与 ViewModel 不用改。
