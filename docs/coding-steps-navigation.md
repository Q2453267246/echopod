# EchoPod 编码实施步骤：自适应底部导航栏与折叠屏适配

> **参考设计稿**：
> - 手机首页：[01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png)
> - 折叠屏适配规则：[01-折叠屏适配规则.png](../EchoPod-设计稿/PNG/02-折叠屏/01-折叠屏适配规则.png)
> - 折叠屏竖向首页（Medium）：[02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png)
> - 折叠屏横向首页（Expanded）：[03-首页-横向.png](../EchoPod-设计稿/PNG/02-折叠屏/03-首页-横向.png)
> - 设计语言（Quiet Paper V1.0）：[01-设计语言-QuietPaper.png](../EchoPod-设计稿/PNG/00-设计语言/01-设计语言-QuietPaper.png)

---

## 目标与范围

基于 **Clean Architecture + MVVM** 架构，完成 EchoPod 的应用导航壳层（App Shell）与主题底座：

1. **Compact（`< 600dp`，手机 / 折叠屏外屏）**：底部导航栏（`NavigationBar`），4 个一级入口：`首页`、`发现`、`订阅`、`我的`。
2. **Medium（`600–839dp`，展开竖向）与 Expanded（`>= 840dp`，展开横向 / 平板）**：左侧边导航栏（`NavigationRail`），顶部带 `+` 导入播客 FAB，下方为 4 个一级入口。
3. **Quiet Paper 浅色 / 深色双主题**：严格遵循设计稿色板、圆角、触控热区（`>= 48dp`）与线性图标规范（`1.8dp` 描边）。
4. **折叠/展开状态连续性**：切换屏幕形态时不丢失当前选中 Tab、页面滚动位置与导航状态。

---

## Step 0：Clean Architecture + MVVM 目录规划

在 `app/src/main/java/com/echopod/` 下按分层建立清晰边界，确保依赖方向严格为 `presentation -> domain <- data`（`domain` 层禁止引入 Android / Compose 依赖）：

```text
com.echopod
├── MainActivity.kt
├── di/
│   └── AppContainer.kt                         # 轻量手动依赖注入容器（后续可平滑替换为 Hilt）
├── core/
│   ├── designsystem/
│   │   ├── theme/
│   │   │   ├── Color.kt                        # Quiet Paper 浅色 & 深色精确色值
│   │   │   ├── Type.kt                         # 字体层级（28/22/18/16/15/12）
│   │   │   ├── Shape.kt                        # 圆角规范（8/12/16/20-24/Full）
│   │   │   └── Theme.kt                        # EchoPodTheme
│   │   └── icon/
│   │       └── EchoPodIcons.kt                 # 首页/发现/订阅/我的/导入 矢量图标（1.8dp 描边）
│   └── adaptive/
│       └── AdaptiveNavigationLayout.kt         # WindowWidthSizeClass -> BottomBar / Rail 映射
├── domain/
│   └── navigation/
│       ├── model/
│       │   └── AppDestination.kt               # 纯 Kotlin 枚举：HOME / DISCOVER / SUBSCRIPTIONS / PROFILE
│       ├── repository/
│       │   └── NavigationPreferencesRepository.kt
│       └── usecase/
│           ├── ObserveLastDestinationUseCase.kt
│           └── SaveLastDestinationUseCase.kt
├── data/
│   └── navigation/
│       └── InMemoryNavigationPreferencesRepository.kt  # 初始实现（后续可替换为 DataStore）
└── presentation/
    ├── navigation/
    │   ├── MainNavigationUiState.kt            # 不可变 UiState
    │   ├── MainNavigationViewModel.kt          # MVVM ViewModel + StateFlow + SavedStateHandle
    │   ├── EchoPodAppShell.kt                  # 自适应导航壳（Compact BottomBar / Medium+Expanded Rail）
    │   └── EchoPodNavHost.kt                   # Compose Navigation NavHost（保留 Tab 状态）
    └── feature/
        ├── home/HomePlaceholderScreen.kt
        ├── discover/DiscoverPlaceholderScreen.kt
        ├── subscriptions/SubscriptionsPlaceholderScreen.kt
        └── profile/ProfilePlaceholderScreen.kt
```

---

## Step 1：配置依赖（`gradle/libs.versions.toml` 与 `app/build.gradle.kts`）

1. 在 `gradle/libs.versions.toml` 中新增自适应布局、导航、Lifecycle ViewModel 及测试依赖：
   - `androidx.compose.material3:material3-adaptive-navigation-suite`（或配合 `androidx.compose.material3.adaptive:adaptive`）
   - `androidx.compose.material3.adaptive:adaptive`（提供 `currentWindowAdaptiveInfo()`）
   - `androidx.window:window`（为后续 `FoldingFeature` 桌面/书本模式做准备）
   - `androidx.navigation:navigation-compose`
   - `androidx.lifecycle:lifecycle-viewmodel-compose`、`androidx.lifecycle:lifecycle-runtime-compose`
   - 单元测试：`junit`、`org.jetbrains.kotlinx:kotlinx-coroutines-test`
   - UI 测试：`androidx.compose.ui:ui-test-junit4`、`androidx.compose.ui:ui-test-manifest`、`androidx.compose.ui:ui-tooling`、`androidx.compose.ui:ui-tooling-preview`
2. 在 `app/build.gradle.kts` 中引入以上依赖，并开启 `testOptions`（如需）。
3. 执行 `./gradlew :app:assembleDebug` 确认依赖解析和基础构建通过。

---

## Step 2：实现 Quiet Paper 设计系统（`core/designsystem`）

### 2.1 色彩规范（`Color.kt` & `Theme.kt`）

严格对照设计稿与 HTML 源码中的色值建立 `EchoPodLightColorScheme` 与 `EchoPodDarkColorScheme`：

| 语义 | 浅色（Light） | 深色（Dark） | 导航栏用途 |
|---|---|---|---|
| Background | `#F7F5F0` | `#131412` | 页面暖白/深墨底色 |
| Surface / Card | `#FFFFFF` | `#22231F` | 卡片与迷你播放器 |
| SurfaceContainer | `#EFECE5` | `#2A2B27` | 底部导航栏 / 侧边 Rail 背景 |
| OnBackground（主文字） | `#1C1B19` | `#ECEAE4` | 选中 Tab 文字 / Rail 导入 FAB 背景 |
| OnSurfaceVariant（辅助文字） | `#6B675F` | `#A3A098` | 未选中图标与文字 |
| Outline（描边） | `#E3DFD6` | `#33342F` | 分割线 / 卡片描边 |
| Primary（品牌·雾青） | `#2E6B66` | `#86C6BD` | 进度条、播放按钮 |
| SecondaryContainer（品牌容器） | `#D6E7E3` | `#1F3B38` | 导航选中胶囊背景 |
| OnSecondaryContainer | `#123A37` | `#BFE3DD` | 导航选中图标颜色 |
| Tertiary（更新圆点） | `#E4893F` | `#F0A060` | 仅用于更新提示圆点 |

### 2.2 矢量图标（`EchoPodIcons.kt`）

按设计稿 SVG 路径创建 24dp 线性图标（`strokeWidth = 1.8f / 1.9f`，圆角端点）：
- `Home`（房屋轮廓）
- `Discover`（指南针）
- `Subscriptions`（2×2 圆角四宫格）
- `Profile`（人物轮廓）
- `Add`（`+` 号，用于侧边 Rail 顶部导入 FAB）

---

## Step 3：搭建 Domain 层与 Data 层（Clean Architecture）

1. **`domain/navigation/model/AppDestination.kt`**：
   - 定义纯 Kotlin 枚举 `AppDestination { HOME, DISCOVER, SUBSCRIPTIONS, PROFILE }`（不持有 `@StringRes` 或 `ImageVector`，UI 资源映射留在 `presentation` 层）。
2. **`domain/navigation/repository/NavigationPreferencesRepository.kt`**：
   - 定义接口：`observeLastDestination(): Flow<AppDestination>` 与 `suspend fun saveLastDestination(destination: AppDestination)`。
3. **`domain/navigation/usecase/`**：
   - `ObserveLastDestinationUseCase`：对外暴露上次停留的一级页面流。
   - `SaveLastDestinationUseCase`：记录当前切换的一级页面。
4. **`data/navigation/InMemoryNavigationPreferencesRepository.kt`**：
   - 使用 `MutableStateFlow(AppDestination.HOME)` 实现仓库接口；后续引入 Jetpack DataStore 时，只需替换该实现，无需改动 Domain 与 UI 层。
5. **`di/AppContainer.kt`**：
   - 组装 Repository、UseCase 与 `ViewModelProvider.Factory`。

---

## Step 4：实现 MVVM 导航 ViewModel（`presentation/navigation`）

1. **`MainNavigationUiState.kt`**：
   ```kotlin
   data class MainNavigationUiState(
       val currentDestination: AppDestination = AppDestination.HOME,
       val isImportDialogVisible: Boolean = false,
   )
   ```
2. **`MainNavigationViewModel.kt`**：
   - 构造注入 `ObserveLastDestinationUseCase`、`SaveLastDestinationUseCase` 与 `SavedStateHandle`。
   - 暴露 `val uiState: StateFlow<MainNavigationUiState>`。
   - 提供单向数据流事件方法：
     - `onDestinationSelected(destination: AppDestination)`：更新 `SavedStateHandle` 并调用 `SaveLastDestinationUseCase`。
     - `onImportClicked()` / `onDismissImport()`：控制侧边 Rail 顶部 `+` 按钮的导入弹窗入口状态。

---

## Step 5：实现折叠屏自适应导航壳（`core/adaptive` + `EchoPodAppShell`）

### 5.1 窗口宽度断点规则（严禁按设备型号判断）

通过 `currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass` 决定导航形态：

```kotlin
enum class AdaptiveNavigationType {
    BOTTOM_BAR,      // Compact: < 600dp
    NAVIGATION_RAIL, // Medium: 600..839dp & Expanded: >= 840dp
}
```

> **注意设计稿细节**：即使在 `Expanded (>= 840dp)` 横向展开态，EchoPod 依然采用 **侧边 `NavigationRail`**（而非展开式 `NavigationDrawer`），以便将横向空间留给双栏 `ListDetailPaneScaffold`。

### 5.2 视觉与交互尺寸规格

- **Compact 底部导航栏（`BOTTOM_BAR`）**：
  - 内容区高度：`80.dp`，叠加系统底部手势栏 `WindowInsets.navigationBars`。
  - 背景色：`MaterialTheme.colorScheme.surfaceContainer`（`#EFECE5` / `#2A2B27`）。
  - 4 等分网格布局，单项触控热区 `>= 48.dp`。
  - 选中态胶囊：`64.dp × 32.dp`，圆角 `16.dp`，背景 `#D6E7E3`（深色 `#1F3B38`），图标 `#123A37`（深色 `#BFE3DD`），文字 `12.sp SemiBold`。
  - 未选中态：图标与文字 `#6B675F`（深色 `#A3A098`），文字 `12.sp Medium`。
  - 切换动效：`200ms` 选中态过渡。
- **Medium / Expanded 侧边导航栏（`NAVIGATION_RAIL`）**：
  - 宽度：`88.dp`，适配 `WindowInsets.safeDrawing`（左侧/顶部安全区）。
  - 背景色同底部栏（`#EFECE5` / `#2A2B27`）。
  - 顶部 `+` 导入播客 FAB：`56.dp × 56.dp`，圆角 `16.dp`，背景 `#1C1B19`（深色 `#ECEAE4`），图标 `#F7F5F0`（深色 `#131412`），底部间距 `20.dp`。
  - 4 个导航项垂直居中排列，选中胶囊 `56.dp × 32.dp`，圆角 `16.dp`，间距 `12.dp`。

### 5.3 Navigation 接入与状态保留（`EchoPodNavHost.kt`）

- 使用 `rememberNavController()` 管理 4 个一级页面。
- 切换 Tab 时遵循官方多返回栈配置，确保滚动位置与页面状态不丢失：
  ```kotlin
  navController.navigate(destination.route) {
      popUpTo(navController.graph.findStartDestination().id) {
          saveState = true
      }
      launchSingleTop = true
      restoreState = true
  }
  ```
- 检查 `AndroidManifest.xml`：确认应用支持多窗口与动态尺寸调整（API 24+ 默认 `resizeableActivity="true"`），不在 Manifest 锁死屏幕方向。

---

## Step 6：接入占位页面与 `MainActivity`

1. 在 `presentation/feature/` 下分别创建 4 个轻量占位页（首页、发现、订阅、我的），每页放一个可滚动列表与当前窗口尺寸类别标识（如 `Compact / Medium / Expanded`），方便肉眼验证切换与折叠连续性。
2. 更新 `MainActivity.kt`：
   - 保持 `enableEdgeToEdge()`。
   - 用 `EchoPodTheme` 包裹 `EchoPodAppShell`。
   - 为侧边 Rail 顶部的 `+` 导入按钮挂载占位 Dialog/BottomSheet 回调。

---

## Step 7：测试与验收清单

### 7.1 自动化测试

1. **单元测试（`app/src/test/.../MainNavigationViewModelTest.kt`）**：
   - 默认启动选中 `AppDestination.HOME`。
   - 调用 `onDestinationSelected(AppDestination.SUBSCRIPTIONS)` 后，`uiState` 与 Repository 同步更新。
   - `onImportClicked()` / `onDismissImport()` 状态流转正确。
2. **Compose UI 测试（`app/src/androidTest/.../EchoPodAppShellTest.kt`）**：
   - 模拟 `Compact` 宽度（如 `412.dp`）：断言显示底部导航栏，不显示侧边 Rail。
   - 模拟 `Medium`（`704.dp`）与 `Expanded`（`840.dp`）：断言显示侧边 Rail 及 `+` 导入按钮，不显示底部导航栏。
   - 点击 4 个导航项，断言选中态与内容区正确切换。

### 7.2 命令行构建与验证

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

### 7.3 人工视觉与折叠屏验收

- [ ] **Compact（手机竖屏）**：底部导航栏 4 项居中、胶囊色值与高度（`80dp`）匹配 [01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png)。
- [ ] **Medium（折叠屏展开竖向 ~704×840）**：自动切换为左侧 `88dp` Rail，顶部显示黑色圆角 `+` 按钮，匹配 [02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png)。
- [ ] **Expanded（折叠屏展开横向 ~840×704）**：保持左侧 `88dp` Rail（不展开为 Drawer），匹配 [03-首页-横向.png](../EchoPod-设计稿/PNG/02-折叠屏/03-首页-横向.png)。
- [ ] **浅色 / 深色模式切换**：所有背景、胶囊、图标、文字颜色与 Quiet Paper 规范一致。
- [ ] **展开/折叠连续性**：切换到「订阅」Tab 并向下滚动后，执行折叠/展开或旋转屏幕，Tab 仍停留在「订阅」且滚动位置保留。
- [ ] **触控热区与安全区**：所有导航项与 FAB 触控热区 `>= 48dp`，底部手势条与状态栏无遮挡。
