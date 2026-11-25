# EchoPod 编码步骤：自适应主导航（底部导航栏 + 折叠屏 Rail）

> 架构：Clean Architecture + MVVM ｜ UI：Jetpack Compose + Material 3
>
> 参考设计稿：
> - 手机首页（Compact）：[01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png)，HTML：`Main.dc.html`
> - 折叠屏展开竖向首页（Medium）：[02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png)，HTML：`FoldHome.dc.html`
> - 折叠屏展开横向首页（Expanded）：[03-首页-横向.png](../EchoPod-设计稿/PNG/02-折叠屏/03-首页-横向.png)，HTML：`FoldHomeLandscape.dc.html`
> - 折叠屏适配规则：[01-折叠屏适配规则.png](../EchoPod-设计稿/PNG/02-折叠屏/01-折叠屏适配规则.png)
> - 深色：[14-首页-深色.png](../EchoPod-设计稿/PNG/01-手机/14-首页-深色.png)、[24-首页-深色-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/24-首页-深色-竖向.png)

---

## 0. 目标与范围

**本阶段要交付：** 一个"应用外壳"（App Shell）——根据窗口宽度自动切换导航形态，四个一级页面（首页 / 发现 / 订阅 / 我的）可切换、状态可保留，并为迷你播放器预留位置。

| 窗口宽度 | 导航形态 | 其他 |
|---|---|---|
| Compact `< 600dp`（手机、折叠屏外屏） | 底部 `NavigationBar` | 迷你播放器悬浮在导航栏上方；"导入"按钮在首页标题栏 |
| Medium `600–839dp`（展开竖持 ≈704×840） | 左侧 `NavigationRail`（88dp），顶部 `+` 导入 FAB | 迷你播放器悬浮在内容区底部 |
| Expanded `≥ 840dp`（展开横持 ≈840×704、平板） | 左侧 `NavigationRail`（80dp，**不换成 Drawer**），顶部 `+` FAB | 内容区是带圆角的"纸面"，后续接双栏 |

**本阶段不做（只预留接口）：** 真实播放（Media3）、`ListDetailPaneScaffold` 双栏、桌面/书本姿态布局、各页面真实内容。

**完成标准：**
1. 三种宽度下导航形态、尺寸、颜色与设计稿一致（浅色 + 深色）。
2. 展开/折叠、旋转、分屏拖动窗口时：当前 Tab 不变，各 Tab 的滚动位置不丢。
3. `./gradlew :app:assembleDebug` 与 `./gradlew :app:testDebugUnitTest` 通过。

---

## 1. 设计稿规格（从 HTML 源文件中提取）

以下数值直接来自 `.dc.html` 源码，比看 PNG 估算更准确。

### 1.1 Compact 底部导航栏（`Main.dc.html`）

| 项 | 值 |
|---|---|
| 容器 | 高 `80dp`（不含系统手势条 inset），背景 `#EFECE5` / 深色 `#2A2B27`，4 等分 |
| 单项 | 纵向排列，`padding-top 12dp`，图标与文字间距 `4dp` |
| 选中指示器 | `64×32dp`，圆角 `16dp`，背景 `#D6E7E3` / `#1F3B38` |
| 图标 | `22dp`（24 视口），描边 `1.8`（选中态 `1.9`） |
| 选中 | 图标 `#123A37` / `#BFE3DD`；文字 `12sp` SemiBold(600)，颜色 `#1C1B19` / `#ECEAE4` |
| 未选中 | 图标与文字 `#6B675F` / `#A3A098`；文字 `12sp` Medium(500) |
| 迷你播放器 | 左右各 `8dp`，底边距导航栏顶部 `8dp`，高 `64dp`，圆角 `16dp`，白底 + 阴影 |

### 1.2 Medium Rail（`FoldHome.dc.html`）

| 项 | 值 |
|---|---|
| 容器 | 宽 `88dp`，背景 `#EFECE5` / `#2A2B27`，`padding-top 28dp`，子项间距 `12dp` |
| 导入 FAB | `56×56dp`，圆角 `16dp`，背景 `#1C1B19` / `#ECEAE4`，图标 `#F7F5F0` / `#131412`，下方额外间距 `20dp` |
| 单项 | 高 `60dp`，指示器 `56×32dp` 圆角 `16dp`，其余颜色/字号同 Compact |
| 内容区 | 背景 `#F7F5F0`，直接铺满 Rail 右侧 |
| 迷你播放器 | 内容区内悬浮：左右 `24dp`、底 `16dp`，高 `68dp`，圆角 `18dp`，多出"快退 15 / 快进 30"按钮 |

### 1.3 Expanded Rail（`FoldHomeLandscape.dc.html`）

| 项 | 值 |
|---|---|
| 整窗背景 | `#EFECE5`（Rail 自己**无背景**，透出整窗底色） |
| Rail | 宽 `80dp`，`padding-top 20dp`，子项间距 `8dp`；FAB 下方额外间距 `16dp` |
| 内容区 | 像一张"纸"：上下右外边距 `8dp`，圆角 `24dp`，背景 `#F7F5F0` |
| 迷你播放器 | 停靠在右侧详情栏底部（属于首页双栏布局，本阶段不实现） |

> AGENTS.md 规定 Medium Rail 为 88dp；Expanded 的 80dp 以设计稿为准，两者不冲突。

### 1.4 图标 SVG 路径（24×24 视口，圆角端点/连接）

| 图标 | path |
|---|---|
| 首页 | `M3.5 10.5 12 3.5l8.5 7V20H15v-6H9v6H3.5z` |
| 发现 | 圆 `cx=12 cy=12 r=9` + `m15.5 8.5-2 5-5 2 2-5z` |
| 订阅 | 4 个圆角矩形 `7×7 rx=1.8`，左上角分别在 `(3.5,3.5)` `(13.5,3.5)` `(3.5,13.5)` `(13.5,13.5)` |
| 我的 | 圆 `cx=12 cy=8 r=4` + `M4.5 20.5c1.5-3.6 4.4-5.5 7.5-5.5s6 1.9 7.5 5.5` |
| 导入 | `M12 5v14M5 12h14`，描边 `2` |

---

## 2. 架构设计

### 2.1 分层与本阶段各层职责

```text
presentation ──► domain ◄── data
```

导航栏本身几乎没有业务逻辑，所以**不要为"当前选中哪个 Tab"造 Repository / UseCase**——那是纯 UI 状态，交给 `NavController` 与 `SavedStateHandle` 即可。本阶段各层真正要做的事：

| 层 | 本阶段内容 |
|---|---|
| `core/designsystem` | Quiet Paper 主题、图标、`EchoPodNavigationBar` / `EchoPodNavigationRail` 组件 |
| `core/adaptive` | 窗口宽度 → `NavigationLayout` 的映射（纯函数，可 JVM 单测） |
| `presentation` | 路由、`EchoPodAppState`（导航 UI 逻辑）、`MainShellViewModel` + `MainShellUiState`、外壳 Composable、占位页面 |
| `domain` + `data` | 外壳里**唯一**需要业务数据的是迷你播放器 → 用它打通第一条完整链路：`PlaybackRepository` 接口 → 假数据实现 → UseCase → ViewModel（Step 8，可单独提交） |

### 2.2 状态归属（避免"双数据源"）

| 状态 | 归属 | 原因 |
|---|---|---|
| 当前一级页面 | `NavController` 返回栈（`EchoPodAppState` 派生） | 返回栈是唯一真相；在 ViewModel 里再存一份会出现不一致 |
| 各 Tab 的滚动位置 / 页面状态 | `NavHost` 的 `saveState` / `restoreState` + `rememberSaveable` | 官方多返回栈机制，跨配置变更自动保存 |
| 当前导航形态（Bar / Rail） | 每帧根据 `currentWindowAdaptiveInfo()` 计算 | 纯派生值，不需要保存 |
| 导入弹窗是否显示 | `MainShellViewModel` + `SavedStateHandle` | 折叠/展开后弹窗不应消失 |
| 迷你播放器数据 | `MainShellViewModel` ← `ObserveNowPlayingUseCase` | 业务数据，走 Clean Architecture |

### 2.3 目标目录

```text
app/src/main/java/com/echopod/
├── EchoPodApplication.kt                  # 持有 AppContainer（Step 8）
├── MainActivity.kt
├── di/
│   └── AppContainer.kt                    # 手动 DI（Step 8）
├── core/
│   ├── adaptive/
│   │   └── NavigationLayout.kt            # 替换当前空的 AdaptiveNavigationLayout.kt
│   └── designsystem/
│       ├── theme/  Color.kt  Type.kt  Shape.kt  Theme.kt
│       └── component/
│           ├── EchoPodNavigationBar.kt
│           └── EchoPodNavigationRail.kt
├── domain/                                # Step 8，纯 Kotlin
│   └── playback/
│       ├── model/NowPlaying.kt
│       ├── repository/PlaybackRepository.kt
│       └── usecase/ObserveNowPlayingUseCase.kt  TogglePlaybackUseCase.kt
├── data/                                  # Step 8
│   └── playback/FakePlaybackRepository.kt
└── presentation/
    ├── navigation/
    │   ├── TopLevelDestination.kt         # 4 个一级页面：路由 + 文案 + 图标
    │   ├── EchoPodNavHost.kt
    │   └── EchoPodAppState.kt
    ├── shell/
    │   ├── MainShellUiState.kt
    │   ├── MainShellViewModel.kt
    │   ├── EchoPodAppShell.kt             # 无状态外壳：按 NavigationLayout 摆放
    │   └── EchoPodApp.kt                  # 有状态入口：连接 ViewModel / AppState
    └── feature/
        ├── home/HomeScreen.kt
        ├── discover/DiscoverScreen.kt
        ├── subscriptions/SubscriptionsScreen.kt
        └── profile/ProfileScreen.kt

app/src/main/res/
├── drawable/  ic_nav_home / ic_nav_discover / ic_nav_subscriptions / ic_nav_profile / ic_add .xml
├── values/    strings.xml  themes.xml  colors.xml
└── values-night/  themes.xml  colors.xml
```

---

## Step 1：添加依赖 ✅ 已完成

**文件：** `gradle/libs.versions.toml`、`app/build.gradle.kts`、根 `build.gradle.kts`

```toml
[versions]
adaptive = "1.1.0"            # currentWindowAdaptiveInfo()
navigation = "2.9.0"          # 类型安全路由
lifecycle = "2.9.0"
serialization = "1.8.1"
coroutines = "1.10.2"
junit = "4.13.2"
androidx-junit = "1.2.1"

[libraries]
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
adaptive = { module = "androidx.compose.material3.adaptive:adaptive", version.ref = "adaptive" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-junit" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

`app/build.gradle.kts`：

```kotlin
plugins {
    // ...已有
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.adaptive)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
}
```

根 `build.gradle.kts` 增加 `alias(libs.plugins.kotlin.serialization) apply false`。

> 不需要单独引 `androidx.window:window`：`currentWindowAdaptiveInfo()` 已经通过 `windowPosture` 提供铰链与桌面模式信息。也**不用**引 `material-icons-extended`（体积大），图标已按 1.4 节做成 Vector Drawable（见 Step 3）。
>
> 版本号以 Gradle Sync 结果为准；以上版本均兼容当前 `compileSdk = 35` / AGP 8.13。

**验证：** `./gradlew :app:assembleDebug`
**提交：** `build: add adaptive, navigation, lifecycle and test dependencies`

---

## Step 2：Quiet Paper 主题（`core/designsystem/theme`）✅ 已完成

已实现文件：`Color.kt`、`Type.kt`、`Shape.kt`、`Theme.kt`，并在 `MainActivity` 中用 `EchoPodTheme` 替换了默认配色。

### 2.1 颜色令牌（`Color.kt` + `Theme.kt`）

> **规则：UI 代码中的颜色一律使用 `EchoPodTheme.colors.xxx`，禁止使用 `MaterialTheme.colorScheme.xxx`。** 使用 M3 组件时，颜色参数也要显式传入项目令牌，不依赖 M3 默认取色。

`Color.kt` 定义 `EchoPodColors`（按设计稿语义命名），`Theme.kt` 通过 `CompositionLocal` 提供，访问方式为 `EchoPodTheme.colors.brand`：

| 令牌 | 浅色 | 深色 | 用在哪 |
|---|---|---|---|
| `background` | `#F7F5F0` | `#131412` | 页面底色 |
| `card` | `#FFFFFF` | `#22231F` | 卡片、迷你播放器 |
| `container` | `#EFECE5` | `#2A2B27` | **底部导航栏 / Rail 背景**、搜索框、筛选标签、Expanded 整窗底色 |
| `textPrimary` | `#1C1B19` | `#ECEAE4` | 主文字、选中导航标签 |
| `textBody` | `#4A4741` | `#C9C5BC` | 正文辅助（节目简介段落） |
| `textSecondary` | `#6B675F` | `#A3A098` | 辅助文字、未选中导航图标与标签 |
| `outline` | `#E3DFD6` | `#3A3B36` | 进度轨道、圆形按钮描边 |
| `divider` | `#E3DFD6` | `#33342F` | 分割线 |
| `brand` / `onBrand` | `#2E6B66` / `#FFFFFF` | `#86C6BD` / `#0F2A27` | 进度条、播放按钮 |
| `brandContainer` | `#D6E7E3` | `#1F3B38` | **导航选中指示器**、"已订阅"按钮 |
| `onBrandContainer` | `#123A37` | `#BFE3DD` | **导航选中图标** |
| `inkButton` / `onInkButton` | `#1C1B19` / `#F7F5F0` | `#ECEAE4` / `#131412` | **Rail 顶部导入 FAB**、首页"导入"按钮 |
| `updateDot` | `#E4893F` | `#F0A060` | 更新提示圆点 |

`MaterialTheme` 仍会收到一份由这些令牌**派生**的 `ColorScheme`（`toMaterialColorScheme()`，`internal`），作用只是兜底：涟漪、光标、对话框等 M3 内部取色不会出现默认紫色。业务代码不读取它。

### 2.2 字体层级（`Type.kt`）

| M3 角色 | 设计用途 | 字号 / 字重 |
|---|---|---|
| `headlineMedium` | 页面标题 "EchoPod" | 28 / 700，字距 -0.6 |
| `headlineSmall` | 播放器 / 节目详情标题 | 22 / 700 |
| `titleLarge` | 区块标题"继续收听" | 18 / 600 |
| `titleMedium` | 单集标题 | 16 / 600 |
| `titleSmall` | 小标题、搜索框 | 14 / 600 |
| `bodyLarge` | 正文 | 15 / 400，行高 24 |
| `bodyMedium` / `bodySmall` | 辅助信息 | 13 / 400、12 / 400 |
| `labelLarge` | 按钮 | 15 / 600 |
| `labelMedium` | **导航标签**、筛选标签 | 12 / 500（选中时 `copy(fontWeight = SemiBold)`） |

设计规范允许 Android 端回退到系统字体（Roboto + 思源黑体），因此不打包 Inter / Noto Sans SC 字体文件。

### 2.3 圆角（`Shape.kt`）

`extraSmall 8`（小封面）· `small 12`（封面）· `medium 16`（迷你播放器）· `large 20`（卡片）· `extraLarge 24`（大卡片、弹窗、Expanded 纸面）。按钮与导航指示器为全圆，不在 `Shapes` 内。

### 2.4 窗口主题（`res/values[-night]/themes.xml`、`colors.xml`）

`AndroidManifest` 改用 `@style/Theme.EchoPod`，仅设置 `windowBackground` 为 `#F7F5F0` / `#131412`。作用：冷启动以及折叠/展开导致 Activity 重建时，不会闪白屏或黑屏。

---

## Step 3：图标与文案资源 ✅ 已完成

图标以 **Vector Drawable** 形式放在 `res/drawable/`，路径与描边直接取自设计稿 SVG（24dp、1.8 描边、圆角端点）：

| 资源 | 用途 |
|---|---|
| `ic_nav_home.xml` | 首页 |
| `ic_nav_discover.xml` | 发现 |
| `ic_nav_subscriptions.xml` | 订阅 |
| `ic_nav_profile.xml` | 我的 |
| `ic_add.xml` | Rail 顶部导入 FAB、首页"导入"按钮（2.0 描边） |

使用方式：`Icon(painterResource(R.drawable.ic_nav_home), contentDescription = null, modifier = Modifier.size(22.dp))`。`Icon` 会用 `LocalContentColor` 着色，因此 XML 里的黑色描边只是占位。导航项的文字已经是无障碍标签，图标 `contentDescription` 传 `null`。

`res/values/strings.xml` 已新增：`nav_home`、`nav_discover`、`nav_subscriptions`、`nav_profile`、`action_import`（首页按钮文字"导入"）、`action_import_podcast`（FAB 无障碍描述"导入播客"）。

---

## Step 4：窗口宽度 → 导航形态（`core/adaptive/NavigationLayout.kt`）✅ 已完成

`NavigationLayout.kt`（替换了原先空的 `AdaptiveNavigationLayout.kt`）：

```kotlin
enum class NavigationLayout { BottomBar, MediumRail, ExpandedRail }

/** 纯函数：只依赖 window-core 的 WindowSizeClass，可在 JVM 单测。 */
fun navigationLayoutFor(sizeClass: WindowSizeClass): NavigationLayout = when {
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> NavigationLayout.ExpandedRail
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> NavigationLayout.MediumRail
    else -> NavigationLayout.BottomBar
}

@Composable
fun rememberNavigationLayout(): NavigationLayout {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(sizeClass) { navigationLayoutFor(sizeClass) }
}
```

要点：
- **只看窗口宽度**，不看设备型号、不看横竖屏。横屏手机（宽 ≥ 600dp）也会切到 Rail，这是期望行为。
- `MediumRail` 与 `ExpandedRail` 分开，是因为两者 Rail 宽度、间距、内容区样式都不同（见 1.2 / 1.3）。
- 若 adaptive 版本较旧没有 `isWidthAtLeastBreakpoint`，改用 `windowWidthSizeClass == WindowWidthSizeClass.EXPANDED` 判断。

**单测**（`app/src/test/.../core/adaptive/NavigationLayoutTest.kt`）：

用真实宽度（含 599 / 600 / 839 / 840 边界）测试，而不是只测断点桶的下限：

```kotlin
private fun layoutForWidth(widthDp: Float) =
    navigationLayoutFor(WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp, 800f))

@Test fun `width below 600dp uses bottom bar`() {
    assertEquals(BottomBar, layoutForWidth(390f))
    assertEquals(BottomBar, layoutForWidth(599f))
}
// 600 / 704 / 839 → MediumRail；840 / 1280 → ExpandedRail
```

> `app/build.gradle.kts` 中显式依赖了 `androidx.window:window-core`（1.5.1），供 `NavigationLayout.kt` 与单测直接使用 `WindowSizeClass`。

**提交：** `feat(adaptive): map window width to navigation layout`

---

## Step 5：导航组件（`core/designsystem/component`）✅ 已完成

已实现文件：`NavItem.kt`（数据 + 共用的 `NavItemIcon` / `NavItemLabel`）、`EchoPodNavigationBar.kt`、`RailStyle.kt`、`EchoPodNavigationRail.kt`，均带浅/深色 `@Preview`。

基于 M3 的 `NavigationBar` / `NavigationRail` 封装——它们自带 Tab 语义、选中动画、48dp 热区与 insets 处理，而且默认尺寸（80dp 高、64×32 指示器、80dp Rail、56×32 指示器）与设计稿基本一致。封装成自有组件的好处是：调用方不感知 M3 细节，将来要换成完全自绘也只改这一处。

> 为什么不直接用 `NavigationSuiteScaffold`：它不支持"迷你播放器夹在内容和底栏之间"、Expanded 的"纸面"内容区，Rail 顶部 FAB 的支持也取决于版本。自己用 `NavigationBar` + `NavigationRail` 组合更可控。

组件只接收**纯 UI 数据**，不认识路由：

```kotlin
@Immutable
data class NavItem(val label: String, @DrawableRes val iconRes: Int)
```

### 5.1 `EchoPodNavigationBar`

```kotlin
@Composable
fun EchoPodNavigationBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = EchoPodTheme.colors.container,
        contentColor = EchoPodTheme.colors.textSecondary,
        tonalElevation = 0.dp,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(index) },
                icon = { Icon(painterResource(item.iconRes), null, Modifier.size(22.dp)) },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                    )
                },
                colors = echoPodNavItemColors(),
            )
        }
    }
}
```

`echoPodNavItemColors()` 全部取项目令牌：

```kotlin
@Composable
internal fun echoPodNavItemColors(): NavigationBarItemColors {
    val c = EchoPodTheme.colors
    return NavigationBarItemDefaults.colors(
        selectedIconColor = c.onBrandContainer,
        selectedTextColor = c.textPrimary,
        indicatorColor = c.brandContainer,
        unselectedIconColor = c.textSecondary,
        unselectedTextColor = c.textSecondary,
    )
}
```

`echoPodRailItemColors()` 同理，使用 `NavigationRailItemDefaults.colors(...)`。`NavigationBar` 默认 `windowInsets` 已包含底部手势条，**不要**再在外面加 `navigationBarsPadding()`。

### 5.2 `EchoPodNavigationRail`

`RailStyle` 中的数值都是设计稿的**最终视觉值**：

```kotlin
enum class RailStyle(
    val width: Dp,           // Rail 宽度
    val topPadding: Dp,      // Rail 顶部 → FAB 顶部
    val fabToFirstItem: Dp,  // FAB 底部 → 第一个导航项
    val itemSpacing: Dp,     // 导航项之间
    val transparent: Boolean,
) {
    Medium(width = 88.dp, topPadding = 28.dp, fabToFirstItem = 32.dp, itemSpacing = 12.dp, transparent = false),
    Expanded(width = 80.dp, topPadding = 20.dp, fabToFirstItem = 24.dp, itemSpacing = 8.dp, transparent = true),
}
```

M3 1.3.x 的 `NavigationRail` 内部自带间距：Column 上下 `4dp` padding、子项之间 `spacedBy(4dp)`、header 后再加 `8dp` Spacer。组件内用"设计值 − M3 内置值"补齐，得到与设计稿一致的最终间距：

```kotlin
private val M3RailColumnPadding = 4.dp
private val M3RailItemSpacing = 4.dp
private val M3RailHeaderToContent = M3RailItemSpacing + 8.dp + M3RailItemSpacing // 16dp

NavigationRail(
    // 背景画在外层，内部 Rail 透明 → 外层可以加 verticalScroll，矮窗口时可滚动且背景仍铺满
    modifier = modifier.fillMaxHeight().width(style.width)
        .background(if (style.transparent) Color.Transparent else EchoPodTheme.colors.container)
        .verticalScroll(rememberScrollState()),
    containerColor = Color.Transparent,
    contentColor = EchoPodTheme.colors.textSecondary,
    // 并入起始侧挖孔，横屏时摄像头不遮挡 Rail
    windowInsets = NavigationRailDefaults.windowInsets
        .union(WindowInsets.displayCutout.only(WindowInsetsSides.Start)),
    header = {
        ImportFab( // 56dp、圆角 16dp、inkButton / onInkButton、无阴影
            onClick = onImportClick,
            contentDescription = importContentDescription,
            modifier = Modifier.padding(
                top = style.topPadding - M3RailColumnPadding,
                bottom = style.fabToFirstItem - M3RailHeaderToContent,
            ),
        )
    },
) {
    val itemPadding = (style.itemSpacing - M3RailItemSpacing) / 2
    items.forEachIndexed { index, item ->
        NavigationRailItem(
            selected = index == selectedIndex,
            onClick = { onItemClick(index) },
            icon = { NavItemIcon(item) },
            label = { NavItemLabel(item, index == selectedIndex) },
            colors = echoPodRailItemColors(),
            modifier = Modifier.padding(vertical = itemPadding).height(60.dp),
        )
    }
}
```

注意：
- 升级 material3 后若 Rail 间距发生变化，先核对上面三个 M3 内置值。
- 如果 M3 内部布局与设计稿的偏差无法接受，再在这个文件里改为自绘（`Column` + `Modifier.selectable(role = Role.Tab)`），对外 API 不变。

**验证：** 为两个组件各写浅/深色 `@Preview`，与 PNG 并排比对。
**提交：** `feat(designsystem): add EchoPodNavigationBar and EchoPodNavigationRail`

---

## Step 6：路由与导航状态（`presentation/navigation`）✅ 已完成

已实现文件：`TopLevelDestination.kt`、`EchoPodAppState.kt`、`EchoPodNavHost.kt`。为了让 `NavHost` 能编译，同时创建了 4 个占位页面（`presentation/feature/*/XxxScreen.kt`，共用 `feature/common/TopLevelPlaceholder.kt`：大标题 + 50 项可滚动列表），Step 10 在此基础上补充 insets 与首页导入按钮。

### 6.1 `TopLevelDestination.kt`

```kotlin
@Serializable data object HomeRoute
@Serializable data object DiscoverRoute
@Serializable data object SubscriptionsRoute
@Serializable data object ProfileRoute

enum class TopLevelDestination(
    val route: Any,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    HOME(HomeRoute, R.string.nav_home, R.drawable.ic_nav_home),
    DISCOVER(DiscoverRoute, R.string.nav_discover, R.drawable.ic_nav_discover),
    SUBSCRIPTIONS(SubscriptionsRoute, R.string.nav_subscriptions, R.drawable.ic_nav_subscriptions),
    PROFILE(ProfileRoute, R.string.nav_profile, R.drawable.ic_nav_profile),
}
```

> 它引用了 Android 资源 ID（`@StringRes` / `@DrawableRes`），属于 presentation，**不要**放进 `domain`。

所需文案已在 Step 3 加入 `strings.xml`。

### 6.2 `EchoPodAppState.kt`（导航 UI 逻辑的状态持有者）

```kotlin
@Stable
class EchoPodAppState(
    val navController: NavHostController,
    val navigationLayout: NavigationLayout,
) {
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val entry by navController.currentBackStackEntryAsState()
            val dest = entry?.destination ?: return null
            return TopLevelDestination.entries.firstOrNull { d ->
                dest.hierarchy.any { it.hasRoute(d.route::class) }
            }
        }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun rememberEchoPodAppState(
    navController: NavHostController = rememberNavController(),
    navigationLayout: NavigationLayout = rememberNavigationLayout(),
) = remember(navController, navigationLayout) { EchoPodAppState(navController, navigationLayout) }
```

- `saveState` / `restoreState` 是"切 Tab 不丢滚动位置"的关键。
- 用 `hierarchy` 判断，是为了以后进入"节目详情"等二级页面时，所属 Tab 仍保持选中。
- 再次点击当前 Tab：先保持默认行为（无操作），"滚回顶部"后续再做。

### 6.3 `EchoPodNavHost.kt`

```kotlin
@Composable
fun EchoPodNavHost(appState: EchoPodAppState, modifier: Modifier = Modifier) {
    NavHost(appState.navController, startDestination = HomeRoute, modifier = modifier) {
        composable<HomeRoute> { HomeScreen() }
        composable<DiscoverRoute> { DiscoverScreen() }
        composable<SubscriptionsRoute> { SubscriptionsScreen() }
        composable<ProfileRoute> { ProfileScreen() }
    }
}
```

页面切换动画：一级 Tab 之间用 `fadeIn/fadeOut(tween(200))`，符合"按钮/选中 200ms"的动效规范。

**单测：** 此步主要是 UI 胶水，放到 Step 10 用 Compose UI 测试覆盖。
**提交：** `feat(navigation): add top-level routes, NavHost and app state`

---

## Step 7：外壳 ViewModel（`presentation/shell`）

先只管导入弹窗；迷你播放器字段在 Step 8 加入。

```kotlin
@Immutable
data class MainShellUiState(
    val isImportSheetVisible: Boolean = false,
    // Step 8: val nowPlaying: NowPlaying? = null,
)

class MainShellViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val uiState: StateFlow<MainShellUiState> =
        savedStateHandle.getStateFlow(KEY_IMPORT_VISIBLE, false)
            .map { MainShellUiState(isImportSheetVisible = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainShellUiState())

    fun onEvent(event: MainShellEvent) {
        when (event) {
            MainShellEvent.ImportClicked -> savedStateHandle[KEY_IMPORT_VISIBLE] = true
            MainShellEvent.ImportDismissed -> savedStateHandle[KEY_IMPORT_VISIBLE] = false
        }
    }

    private companion object { const val KEY_IMPORT_VISIBLE = "import_visible" }
}

sealed interface MainShellEvent {
    data object ImportClicked : MainShellEvent
    data object ImportDismissed : MainShellEvent
}
```

- 用 `SavedStateHandle` 保存：折叠/展开导致 Activity 重建、甚至进程被回收后，弹窗状态都还在。
- 只有 `SavedStateHandle` 一个构造参数时，默认的 `viewModel()` 工厂即可创建，无需手动 Factory。
- 导入入口有两处：Compact 在首页标题栏（"+ 导入"按钮，由首页通过回调上抛），Medium/Expanded 在 Rail 顶部 FAB。**两处都发同一个 `ImportClicked` 事件**。

**单测**（`MainShellViewModelTest`）：初始不可见；`ImportClicked` 后可见；`ImportDismissed` 后不可见；用预置 `SavedStateHandle(mapOf("import_visible" to true))` 构造时恢复为可见。

**提交：** `feat(shell): add MainShellViewModel with saved import state`

---

## Step 8：迷你播放器的 Clean Architecture 链路（可单独提交，也可推后）

外壳里唯一的业务数据是"正在播放"。用它打通 domain → data → presentation 的完整链路，以后接 Media3 时只替换 data 层。如果只想先交付导航栏，跳过本步，外壳的 `miniPlayer` 槽位传空即可。

**domain（纯 Kotlin，不得 import android/androidx）：**

```kotlin
data class NowPlaying(
    val episodeTitle: String,
    val podcastTitle: String,
    val speed: Float,
    val progress: Float,          // 0f..1f
    val remainingMinutes: Int,
    val isPlaying: Boolean,
)

interface PlaybackRepository {
    val nowPlaying: Flow<NowPlaying?>
    suspend fun togglePlayPause()
    suspend fun seekBy(seconds: Int)
}

class ObserveNowPlayingUseCase(private val repo: PlaybackRepository) {
    operator fun invoke(): Flow<NowPlaying?> = repo.nowPlaying
}
class TogglePlaybackUseCase(private val repo: PlaybackRepository) {
    suspend operator fun invoke() = repo.togglePlayPause()
}
```

**data：** `FakePlaybackRepository` 用 `MutableStateFlow` 持有设计稿中的示例（《在深夜写代码的人，都在想些什么》· 夜航电台 · 1.2x · 剩余 23 分钟）。

**di：** `AppContainer` 创建 Repository 与 UseCase；`EchoPodApplication` 持有它（记得在 Manifest 写 `android:name=".EchoPodApplication"`）。此时 `MainShellViewModel` 多了构造参数，用 `viewModelFactory { initializer { MainShellViewModel(createSavedStateHandle(), container.observeNowPlaying, container.togglePlayback) } }` 创建。

**presentation：** `MainShellUiState` 增加 `nowPlaying: NowPlaying?`，用 `combine(savedStateHandle.getStateFlow(...), observeNowPlaying())` 生成；事件增加 `TogglePlayback`、`SeekBack`、`SeekForward`。`MiniPlayer` Composable 放在 `presentation/shell`，只接收数据与回调，`showSkipButtons` 参数控制 Medium 下的快退/快进按钮。

**单测：** UseCase 各一个（用 Fake 仓库）；ViewModel 用 `runTest` + `UnconfinedTestDispatcher`（`Dispatchers.setMain`）断言 `nowPlaying` 能透传、`TogglePlayback` 会调用到仓库。

**提交：** `feat(playback): add now-playing domain, fake repository and shell wiring`

---

## Step 9：自适应外壳（`presentation/shell/EchoPodAppShell.kt` + `EchoPodApp.kt`）✅ 基础版已完成

已提前于 Step 7 / 8 实现，`MainActivity` 已接入（`setContent { EchoPodTheme { EchoPodApp() } }`），三种形态可在设备上切换。尚待补充：
- `onImportClick` 目前为空操作，Step 7 完成后改为 `viewModel.onEvent(MainShellEvent.ImportClicked)` 并显示导入弹窗；
- `miniPlayer` 槽位默认为空，Step 8 完成后接入。

已在 Pixel 10 Pro Fold 模拟器验证：折叠（外屏，Compact）→ 展开（≈852dp，Expanded）→ 强制 704dp（Medium）→ 恢复，当前 Tab 与滚动位置均保持。

### 9.1 无状态外壳

外壳**不读取窗口尺寸、不持有 ViewModel**，只根据传入的 `navigationLayout` 摆放——这样 UI 测试可以直接指定任意形态。

```kotlin
@Composable
fun EchoPodAppShell(
    navigationLayout: NavigationLayout,
    items: List<NavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
    miniPlayer: @Composable BoxScope.(NavigationLayout) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val windowBg = when (navigationLayout) {
        NavigationLayout.ExpandedRail -> EchoPodTheme.colors.container
        else -> EchoPodTheme.colors.background
    }
    Row(Modifier.fillMaxSize().background(windowBg)) {
        if (navigationLayout != NavigationLayout.BottomBar) {
            EchoPodNavigationRail(
                items, selectedIndex, onItemClick, onImportClick,
                importContentDescription = stringResource(R.string.action_import_podcast),
                style = if (navigationLayout == NavigationLayout.MediumRail) RailStyle.Medium else RailStyle.Expanded,
            )
        }
        Column(Modifier.weight(1f)) {
            Box(Modifier.weight(1f).then(contentAreaModifier(navigationLayout))) {
                content()                         // ← NavHost 始终在同一个调用位置
                miniPlayer(navigationLayout)      // Compact/Medium 下悬浮在内容底部
            }
            if (navigationLayout == NavigationLayout.BottomBar) {
                EchoPodNavigationBar(items, selectedIndex, onItemClick)
            }
        }
    }
}
```

- `contentAreaModifier`（只改 Modifier，不改组合结构）：
  - BottomBar：`consumeWindowInsets(WindowInsets.navigationBars)`——手势条已由底部导航栏处理；
  - MediumRail：`consumeWindowInsets(safeDrawing.only(Start))`——起始侧已由 Rail 处理；
  - ExpandedRail：同样消费起始侧，再 `windowInsetsPadding(safeDrawing.only(Top + End + Bottom))` → `padding(top/end/bottom = 8.dp)` → `clip(RoundedCornerShape(24.dp))` → `background(EchoPodTheme.colors.background)`，即避开系统栏的"纸面"。
- **`content()` 必须处于固定的调用位置**：Bar/Rail 用 `if` 包裹并放在它前后，Compose 会给 `if` 块单独分组，`content()` 的身份不变；所以拖动分屏窗口、从 Medium 变成 Compact 时 `NavHost` 不会被销毁重建。千万不要写成 `when(layout) { BottomBar -> Scaffold { content() }; else -> Row { content() } }`——那样三个分支里的 `content()` 是三个不同实例，切换形态会丢页面状态。
- **Insets**：外壳按上面的规则"消费" insets，页面在列表首尾放 `Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))` / `Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))`。这两个修饰符会自动扣除已消费的部分（Compact 底部为 0，Expanded 上下都为 0），列表内容仍可滚动到系统栏后面。**不要**用 `WindowInsets.xxx.asPaddingValues()`（它不感知消费，会重复留白），也不要整体 `safeDrawingPadding()`（背景无法延伸到系统栏后面）。
- **迷你播放器位置**：Compact 时 `Modifier.align(BottomCenter).padding(horizontal = 8.dp, vertical = 8.dp)`，因为它在内容 `Box` 内、导航栏之上，自然满足"距导航栏 8dp"；Medium 时 `padding(horizontal = 24.dp, vertical = 16.dp)` 并额外加底部系统 inset；Expanded 时外壳**不渲染**（由首页的详情栏负责，Step 9 之后的任务）。页面列表需要在底部留出迷你播放器高度的 `contentPadding`，避免最后一项被挡住。

### 9.2 有状态入口 `EchoPodApp.kt`

```kotlin
@Composable
fun EchoPodApp(
    appState: EchoPodAppState = rememberEchoPodAppState(),
    viewModel: MainShellViewModel = viewModel(/* Step 8 后传 factory */), // Step 7 加入
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val destinations = TopLevelDestination.entries
    val items = destinations.map { NavItem(stringResource(it.labelRes), it.iconRes) }
    val selected = appState.currentTopLevelDestination

    EchoPodAppShell(
        navigationLayout = appState.navigationLayout,
        items = items,
        selectedIndex = destinations.indexOf(selected),
        onItemClick = { appState.navigateToTopLevel(destinations[it]) },
        onImportClick = { viewModel.onEvent(MainShellEvent.ImportClicked) },
        miniPlayer = { layout -> /* uiState.nowPlaying?.let { MiniPlayer(...) } */ },
    ) {
        EchoPodNavHost(appState)
    }

    if (uiState.isImportSheetVisible) {
        ImportPlaceholderDialog(onDismiss = { viewModel.onEvent(MainShellEvent.ImportDismissed) })
    }
}
```

`MainActivity` 中：`setContent { EchoPodTheme { EchoPodApp() } }`。

**AndroidManifest 检查：** 不要设置 `screenOrientation`；不要为了"避免重建"给 Activity 加 `configChanges`——状态保留应依赖 `SavedStateHandle` 与 `rememberSaveable`，而不是阻止重建。

**提交：** `feat(shell): add adaptive app shell with bar/rail switching`

---

## Step 10：占位页面（基础版已在 Step 6 完成）

已有：4 个页面 + 共用的 `TopLevelPlaceholder`（大标题 + 50 项 `LazyColumn`，用来验证"切换 Tab / 折叠展开后滚动位置不丢"），目前只处理了状态栏 inset。本步骤待补：

- 左右边距按形态区分：Compact `16dp`、Medium `24dp`。
- 列表底部 `contentPadding`：Rail 形态加上 `WindowInsets.navigationBars` 底部值，并为迷你播放器留出高度。
- 首页标题栏在 **Compact** 下显示"+ 导入"按钮（高 `48dp`、圆角 `24dp`、`EchoPodTheme.colors.inkButton` 背景、`onInkButton` 文字），通过 `onImportClick` 回调上抛；Medium/Expanded 下隐藏（Rail 已经有 FAB）。首页通过参数 `showImportButton: Boolean` 得知，不要在页面里自己读窗口尺寸。

**提交：** `feat(feature): add placeholder top-level screens`

---

## Step 11：测试与验收

### 11.1 JVM 单元测试（`./gradlew :app:testDebugUnitTest`）

| 测试类 | 覆盖内容 |
|---|---|
| `NavigationLayoutTest` | 0 / 599 / 600 / 839 / 840 / 1200dp 边界映射 |
| `MainShellViewModelTest` | 导入弹窗事件、`SavedStateHandle` 恢复、（Step 8 后）nowPlaying 透传与播放切换 |
| `ObserveNowPlayingUseCaseTest` / `TogglePlaybackUseCaseTest` | Step 8 的 UseCase |

### 11.2 Compose UI 测试（`./gradlew :app:connectedDebugAndroidTest`，需要设备）

`EchoPodAppShellTest` 直接给无状态外壳传入不同的 `NavigationLayout`，不依赖真实设备宽度：

- `BottomBar`：能找到 4 个导航项；找不到"导入播客"FAB。
- `MediumRail` / `ExpandedRail`：能找到 FAB 和 4 个导航项。
- 点击"订阅"→ 回调收到索引 2，且该项 `assertIsSelected()`。
- 所有导航项与 FAB：`assertHeightIsAtLeast(48.dp)` 与 `assertWidthIsAtLeast(48.dp)`。

另写一个 `EchoPodAppTest`（带真实 NavHost）：切到"订阅"→ 滚动列表 → 切到"首页"→ 切回"订阅"，断言列表仍在滚动后的位置；再用 `StateRestorationTester` 模拟重建，断言选中 Tab 与滚动位置仍在。

### 11.3 人工验收清单

推荐使用 Android Studio 的 **Resizable (Experimental)** 模拟器或 **Pixel Fold** 模拟器，可以即时切换手机 / 折叠 / 展开 / 平板。

- [ ] **Compact**：底部 80dp 导航栏，4 项等分；选中胶囊 64×32 浅雾青色；与 [01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png) 对比。
- [ ] **Medium（展开竖持）**：左侧 88dp Rail、深色圆角 `+` FAB；与 [02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png) 对比。
- [ ] **Expanded（展开横持）**：80dp 透明 Rail + 圆角纸面内容区，**没有**变成抽屉；与 [03-首页-横向.png](../EchoPod-设计稿/PNG/02-折叠屏/03-首页-横向.png) 对比。
- [ ] **深色模式**：与 [14-首页-深色.png](../EchoPod-设计稿/PNG/01-手机/14-首页-深色.png)、[24-首页-深色-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/24-首页-深色-竖向.png) 对比，不出现 M3 默认紫色。
- [ ] **连续性**：在"订阅"滚到第 30 项 → 折叠 / 展开 / 旋转 / 拖动分屏窗口宽度 → 仍在"订阅"且停在第 30 项附近。
- [ ] **导入弹窗**：打开后折叠 / 展开，弹窗仍在。
- [ ] **Edge-to-edge**：状态栏、手势条后面是页面底色；内容和导航不被遮挡；横屏挖孔不挡 Rail。
- [ ] **横屏手机**（宽 ≥ 600、高 < 480）：Rail 内容放得下或可以滚动。
- [ ] **TalkBack**：导航项朗读为"首页，标签，已选中"；FAB 朗读为"导入播客"。

---

## 提交顺序一览

| # | 提交 | 可验证点 |
|---|---|---|
| 1 | `build: add adaptive, navigation, lifecycle and test dependencies` | 已完成 |
| 2 | `feat(designsystem): implement Quiet Paper color, type and shape tokens` | 已完成 |
| 3 | `feat(res): add navigation icons and strings` | 已完成 |
| 4 | `feat(adaptive): map window width to navigation layout` | 已完成 |
| 5 | `feat(designsystem): add EchoPodNavigationBar and EchoPodNavigationRail` | 已完成 |
| 6 | `feat(navigation): add top-level routes, NavHost and app state` | 已完成 |
| 7 | `feat(shell): add MainShellViewModel with saved import state` | 单测 |
| 8 | `feat(playback): add now-playing domain, fake repository and shell wiring` | 单测（可推后） |
| 9 | `feat(shell): add adaptive app shell with bar/rail switching` | 基础版已完成（导入、迷你播放器待接入） |
| 10 | `feat(feature): add placeholder top-level screens` | 连续性验收 |
| 11 | `test: add shell UI tests` | `connectedDebugAndroidTest` |

每次提交前执行：

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

---

## 下一阶段（本文档之外）

1. **首页内容**：按断点切换订阅 4/5 列、最新更新 1/2 列；Compact 搜索图标 → Medium 常驻搜索框。
2. **Expanded 双栏**：首页 / 订阅用 `ListDetailPaneScaffold`（`androidx.compose.material3.adaptive:adaptive-layout`），迷你播放器停靠在详情栏底部。
3. **折叠姿态**：读取 `currentWindowAdaptiveInfo().windowPosture`——书本模式让双栏分界线对齐铰链（`hingeList`）；桌面模式（`isTabletop`）用于播放器上下分屏，控件不跨铰链。
4. **真实播放**：用 Media3 `MediaSessionService` 实现 `PlaybackRepository`，替换 `FakePlaybackRepository`，domain 与 UI 不用改。
