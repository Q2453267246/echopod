# EchoPod 整体开发顺序（Development Roadmap）

> 本文档是**全项目**的开发路线图：按什么顺序做、每个阶段交付什么、依赖什么、怎么验收。
> 每个阶段开工前，再像 [coding-steps-navigation.md](coding-steps-navigation.md) 那样写一份该阶段的细化 coding steps。
> 工作量估算与两个月周计划见 [development-schedule.md](development-schedule.md)。
>
> 约束来源：[AGENTS.md](../AGENTS.md)（Clean Architecture + MVVM、Quiet Paper 令牌、折叠屏断点、验证命令）。
> 设计来源：`EchoPod-设计稿/PNG/`（43 张画板）与 `源文件（Claude设计稿HTML）/`。

---

## 0. 排序原则

1. **先有数据，再有页面**：除了外壳，几乎所有页面都依赖"订阅的节目 + 单集"。所以最先打通的业务链路是 *RSS 订阅 → 本地存储*，而不是先把页面画好再接数据。
2. **最短可用路径优先（"能听"）**：第一个里程碑只做"粘贴 RSS → 看到单集 → 能播放"，验证架构的每一层都通了，再横向铺开功能。
3. **每个功能一次交付三种断点**：Compact / Medium / Expanded 的布局随功能一起做，**不**留到最后统一"适配折叠屏"。只有铰链姿态（桌面 / 书本模式）放在单独阶段，因为它依赖播放器和双栏都已完成。
4. **基础设施按需引入**：Room、Media3、Coil、WorkManager 等依赖在**第一个用到它的阶段**才加入，不提前引入（AGENTS.md：避免未使用的三方库）。
5. **domain 先行**：每个功能先写 domain 模型 + 仓库接口 + UseCase（纯 Kotlin、可单测），再写 data 实现，最后写 UI。UI 开发期间可以用 Fake 仓库并行推进。
6. **小步提交**：每个阶段拆成若干可独立构建、可独立验证的提交；每次提交前跑 `assembleDebug` + `testDebugUnitTest`。

---

## 1. 页面 → 阶段对照

| 页面 / 能力 | 手机设计稿 | 折叠屏设计稿 | 阶段 |
|---|---|---|---|
| 外壳、导航、主题 | 01-首页（底栏） | 01-适配规则、02/03-首页 | **阶段 0 ✅** |
| 迷你播放器 | 01-首页（底部） | 02-首页-竖向（含快退/快进） | 阶段 0 收尾 → 阶段 4 接真数据 |
| 导入（RSS） | 10-小宇宙导入-预览 | 22/23-导入对话框 | 阶段 3 |
| 节目详情 | 06-节目详情、15-深色 | 03-展开-横向订阅双栏 | 阶段 3 |
| 全屏播放器 | 08-全屏播放器 | 10/11-播放器、05-展开-横向播放器双栏 | 阶段 4 |
| 首页 | 01-首页、14-深色 | 02/03、24/25-深色 | 阶段 5 |
| 订阅 | 04-订阅 | 04/05、26/27-深色、03-订阅双栏 | 阶段 5 |
| 单集详情 | 07-单集详情 | 08/09-单集详情、07-单集列表与详情 | 阶段 6 |
| 播放队列 | 09-播放队列 | 14/15-播放队列、10-展开-竖向播放队列 | 阶段 7 |
| 发现 / 搜索 | 02-发现、03-搜索结果 | 06/07-发现与搜索 | 阶段 8 |
| 小宇宙链接、OPML、批量导入 | 10-小宇宙导入-预览 | 22/23、12-展开-竖向导入对话框 | 阶段 9 |
| 后台刷新、更新圆点 | 01-首页"3 个节目有更新" | — | 阶段 10 |
| 离线下载 | 11-离线下载 | 16/17-我的与离线下载 | 阶段 11 |
| 我的、收藏、历史、设置 | 05-我的、13-设置 | 16/17、18/19-设置 | 阶段 12 |
| 半折叠：桌面 / 书本模式 | — | 04/13-桌面模式、12-书本模式 | 阶段 13 |
| 收听统计（P1） | 12-收听统计 | 20/21-收听统计 | 阶段 14 |
| 打磨与发布 | 全部 | 全部 | 阶段 15 |

---

## 2. 里程碑

| 里程碑 | 包含阶段 | 用户能做什么 |
|---|---|---|
| **M0 外壳** ✅ | 0 | 在三种断点间切换 Tab，状态不丢 |
| **M1 能听** | 1 – 4 | 粘贴 RSS 订阅一个节目 → 看节目详情 → 播放（后台、通知栏、进度记忆） |
| **M2 日常可用** | 5 – 7 | 首页"继续收听 / 最新更新"、订阅列表、单集详情与章节、播放队列 |
| **M3 发现与迁移** | 8 – 10 | 搜索订阅、小宇宙 / OPML 迁移、后台自动刷新 |
| **M4 离线与个性化** | 11 – 12 | 离线下载、收藏、历史、设置全部生效 |
| **M5 折叠屏完整** | 13 | 桌面模式 / 书本模式播放器 |
| **M6 发布** | 14 – 15 | 收听统计、性能与无障碍打磨、Release 构建 |

---

## 3. 跨阶段的架构决定（开工前统一）

### 3.1 包结构（单模块，按层 + 按功能）

```text
com.echopod
├── core/
│   ├── designsystem/   theme/、component/（所有 Quiet Paper 组件）
│   ├── adaptive/       NavigationLayout、姿态（Posture）帮助函数
│   ├── common/         Result/错误类型、时间格式化、Dispatcher 注入
│   └── testing/        （test 源集）Fake 仓库、测试数据
├── domain/<feature>/   model/、repository/、usecase/          ← 纯 Kotlin
├── data/<feature>/     local/（Room）、remote/（网络/RSS）、XxxRepositoryImpl
├── di/                 Hilt 模块（DatabaseModule、NetworkModule、XxxModule）
└── presentation/
    ├── shell/          外壳、MainShellViewModel、MiniPlayer
    ├── navigation/     路由、NavHost、EchoPodAppState
    └── feature/<page>/ XxxScreen（无状态）+ XxxRoute（取 ViewModel）+ XxxViewModel + XxxUiState
```

- **先保持单 Gradle 模块**。等到构建时间明显变长（或阶段 15）再考虑拆 `:core:designsystem`、`:domain` 等模块；包结构现在就按模块边界组织，以后拆分只是移动目录。
- 每个页面分两层：`XxxRoute`（`hiltViewModel()` + `collectAsStateWithLifecycle()`）和 `XxxScreen(state, onEvent)`（纯 UI，可预览、可测）。

### 3.2 二级页面与全屏播放器的导航方式

| 页面 | 方式 | 原因 |
|---|---|---|
| 节目详情、单集详情、队列、下载、设置、统计 | NavHost 中的普通目的地（类型安全路由，带参数如 `PodcastRoute(podcastId)`） | 走返回栈，支持系统返回、深链 |
| 二级页面打开时 | 隐藏底部导航栏 / Rail 仍保留（Medium、Expanded） | 与设计稿一致：手机端详情页无底栏 |
| 全屏播放器 | **外壳级覆盖层**（`MainShellViewModel.isPlayerExpanded`，存 `SavedStateHandle`），300ms 上滑进入，`BackHandler` 收起 | 播放器不属于任何 Tab 的返回栈；切 Tab、折叠展开都不影响它 |
| 导入 | 外壳级对话框（已有 `isImportSheetVisible`）；Compact 全屏，Medium/Expanded 居中对话框 | 两处入口共用一个状态 |
| Expanded 下的列表 / 详情 | `ListDetailPaneScaffold`（订阅 → 节目 → 单集） | 折叠回 Compact 时，详情栏内容变成单栏的当前页 |

### 3.3 数据与依赖（按阶段引入）

| 依赖 | 首次引入阶段 | 用途 |
|---|---|---|
| OkHttp | 2 | 下载 RSS、搜索 API |
| Room（KSP） | 2 | 节目、单集、进度、队列、下载、收藏、收听记录 |
| DataStore Preferences | 2 | 设置项、最近搜索 |
| Coil 3（Compose） | 3 | 封面图 |
| Media3（ExoPlayer + Session） | 4 | 后台播放、通知栏、蓝牙 / 耳机控制 |
| material3-adaptive-layout / navigation | 5 | `ListDetailPaneScaffold` |
| WorkManager + hilt-work | 10 | 定时刷新订阅 |
| Media3 DownloadManager（或 WorkManager） | 11 | 离线下载（与播放缓存共用） |
| androidx.palette | 15 | 动态封面取色 |

- 版本以接入时的最新稳定版为准，**接入前确认兼容 `compileSdk = 35`**，统一写进 `libs.versions.toml`。
- RSS 解析用 Android 自带的 `XmlPullParser`（放在 data 层），不引入额外 RSS 库；JVM 单测里可加 `testImplementation` 的 kxml2 实现，或用 Robolectric。

### 3.4 domain 核心模型（阶段 2 一次定好，后续只加字段）

| 模型 | 关键字段 |
|---|---|
| `Podcast` | id、feedUrl、title、author、description、artworkUrl、category、episodeCount、lastUpdatedAt、isSubscribed、hasNewEpisodes |
| `Episode` | id（guid）、podcastId、title、summary、showNotesHtml、publishedAt、duration、audioUrl、sizeBytes、number |
| `Chapter` | episodeId、startMs、title |
| `PlaybackProgress` | episodeId、positionMs、isCompleted、lastPlayedAt |
| `QueueItem` | episodeId、position |
| `DownloadItem` | episodeId、state（Queued / WaitingWifi / Running / Failed / Done）、downloadedBytes、totalBytes |
| `ListeningSession` | episodeId、podcastId、startedAt、listenedMs（真实时长，不含拖动跳过） |
| `AppSettings` | themeMode、dynamicCoverColor、defaultSpeed、skipForward/Back、autoPlayNext、pauseOnHeadsetUnplug、wifiOnlyDownload、refreshInterval、autoDownloadNew |

### 3.5 错误与离线策略（全项目统一）

- 仓库返回 `Flow` 的地方**以本地数据库为唯一数据源**：网络结果先写库，UI 只观察库（"离线优先"）。
- 网络失败时保留缓存并在 UiState 中带一个非阻塞错误（对应设计稿："刷新失败 · 显示缓存"、"海外目录暂时无法连接 · 已显示国内目录结果 · 重试"）。
- 错误类型放 `core/common`，UI 只认 `UiMessage`（文案资源 ID + 可选重试动作），不在 Composable 里拼接异常信息。

---

## 阶段 0：外壳与导航 ✅（收尾中）

详见 [coding-steps-navigation.md](coding-steps-navigation.md)。

- [x] 主题令牌、导航栏 / Rail、三种断点、状态连续性
- [x] Hilt、`MainShellViewModel`、导入弹窗占位、播放链路（Fake）与单测
- [ ] **`MiniPlayer` Composable**：Compact 64dp / Medium 68dp + 快退快进；接入外壳 `miniPlayer` 槽位与 `hasMiniPlayer`
- [ ] **11.2 Compose UI 测试**：`EchoPodAppShellTest`、`EchoPodAppTest`

**验收**：MiniPlayer 与 [01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png)、[02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png) 底部一致；点击播放 / 暂停能改变 Fake 状态。

---

## 阶段 1：设计系统组件库

**目标**：把各页面反复出现的 UI 元素先做成 `core/designsystem/component` 里的无状态组件，后续页面只做拼装。

| 组件 | 出现位置 | 要点 |
|---|---|---|
| `PodcastCover` | 所有列表、详情、播放器 | 有图用 Coil（阶段 3 接入）；**无图时生成占位封面**：标题首字 + 根据标题哈希选的一组封面配色（设计稿里的"码 / 夜 / 慢 / 城"方块），配色定义为设计系统令牌，不在页面里写颜色 |
| `SectionHeader` | 首页"继续收听 / 我的订阅 / 最新更新" | 标题 + 可选尾部操作（"全部 12 >"、"下拉刷新"） |
| `EpisodeListItem` | 首页最新更新、节目详情、单集列表 | 封面 + 标题（2 行）+ 元信息行 + 尾部操作（下载 / 已下载 / 剩余时间） |
| `PodcastListItem` / `PodcastGridItem` | 订阅、搜索结果、首页订阅行 | 未听数徽标、更新圆点（`updateDot`） |
| `ContinueListeningCard` | 首页 | 进度条 + 时间 + 品牌色圆形播放键 |
| `FilterChipRow` | 订阅、节目详情 | 全部 / 有更新 4 / 科技…；选中用墨色（`inkButton`），未选中用 `container` |
| `EchoPodButton`（Ink / Brand / Tonal / Text） | 导入、订阅、播放最新、重试 | 高 48dp、圆角 24dp |
| `ProgressBar`、`InlineBanner`（错误 / 提示条）、`SettingsRow`（开关 / 值） | 播放进度、"刷新失败"、设置页 | |
| 图标集 | 全部 | 补齐 search、download、downloaded、play、pause、skip15、skip30、more、heart、queue、timer、speed、chevron 等，统一 24 视口、1.8 描边、圆角端点 |

**排版决定（已定）**：设计语言页注明 Android 端可直接使用系统字体，因此不打包 Inter；时间、进度等数字统一使用等宽数字（`fontFeatureSettings = "tnum"`）。

**验收**：每个组件有浅色 / 深色 `@Preview`；所有可点击元素 ≥ 48dp；颜色只来自 `EchoPodTheme.colors`。
**测试**：组件级 Compose UI 测试只覆盖有逻辑的部分（占位封面配色稳定、徽标显示条件）。

---

## 阶段 2：数据基础设施

**目标**：建立本地数据库、网络、RSS 解析与设置存储，并用单测证明正确；本阶段**不做页面**。

1. **domain**：按 §3.4 定义模型；定义 `PodcastRepository`、`EpisodeRepository`、`SettingsRepository` 接口。
2. **Room**：`EchoPodDatabase` + 实体（`PodcastEntity`、`EpisodeEntity`、`ChapterEntity`、`PlaybackProgressEntity` 等）+ DAO（全部返回 `Flow`）；`exportSchema = true`，把 schema JSON 提交进仓库，为后续迁移做准备。
3. **网络**：`NetworkModule` 提供单例 `OkHttpClient`（超时、User-Agent 使用通用客户端名、缓存）；所有 IO 通过注入的 `CoroutineDispatcher` 切线程，便于测试。
4. **RSS 解析器**：解析 `channel`（标题、作者、简介、封面、分类）与 `item`（guid、标题、`enclosure`、`itunes:duration`、`pubDate`、`content:encoded`、`podcast:chapters` 链接）。容错：缺 guid 时用 enclosure URL 作为 id；多种时间格式。
5. **DataStore**：`SettingsRepository` 实现，提供默认值。
6. **Debug 种子数据**：`src/debug` 中提供一个开关，可以把 `test/resources` 里的示例 RSS 导入数据库，方便没网时开发 UI。

**测试**：RSS 解析（多份真实 feed 样本，放 `test/resources`）、DAO（Room in-memory，需 Robolectric 或 androidTest）、Repository（Fake DAO + Fake 网络）。
**提交拆分**：`domain models` → `room schema` → `rss parser` → `network + repository` → `settings datastore`。

---

## 阶段 3：订阅第一个节目（RSS 导入 + 节目详情）

**目标**：用户粘贴 RSS 地址 → 预览 → 订阅 → 看到节目详情和单集列表。这是 M1 的前半段。

1. **UseCase**：`PreviewFeedUseCase(url)`（只拉取解析不入库）、`SubscribeUseCase(feed)`、`ObservePodcastUseCase(id)`、`ObserveEpisodesUseCase(podcastId, filter)`、`UnsubscribeUseCase`。
2. **导入对话框 v1**（替换 `ImportPlaceholderDialog`）：三个分段（小宇宙链接 / RSS 地址 / OPML 文件），本阶段只实现 **RSS 地址**；输入 → 校验 → 预览卡片（封面、名称、主播、分类、可导入单集数、最近更新、更新频率）→ "订阅「xxx」"。
   - Compact：全屏页面（[10-小宇宙导入-预览.png](../EchoPod-设计稿/PNG/01-手机/10-小宇宙导入-预览.png)）；Medium / Expanded：居中对话框（[22/23-导入对话框](../EchoPod-设计稿/PNG/02-折叠屏/22-导入对话框-竖向.png)）。
   - 导入状态（输入文本、当前分段）放 `ImportViewModel` + `SavedStateHandle`，折叠 / 展开不丢。
3. **Coil 接入**：`PodcastCover` 支持网络图片，失败回退到占位封面。
4. **节目详情页** `PodcastRoute(podcastId)`：头部信息、简介展开、已订阅 / 播放最新一集、筛选（全部 / 未播放 / 已下载）、单集列表（[06-节目详情.png](../EchoPod-设计稿/PNG/01-手机/06-节目详情.png)）。
5. **临时入口**：订阅 Tab 先用简单列表展示已订阅节目，点击进入详情（阶段 5 再做完整订阅页）。

**验收**：用真实 RSS 地址订阅成功；断网时重新打开节目详情仍能看到已缓存单集；无效地址给出可读错误。
**测试**：UseCase 单测；`ImportViewModel` 状态机单测（Idle → Loading → Preview / Error → Subscribed）。

---

## 阶段 4：真实播放（Media3）

**目标**：替换 `FakePlaybackRepository`，实现后台播放、通知栏、进度记忆、全屏播放器。完成即达成 **M1 能听**。

1. **`PlaybackService`**（`MediaSessionService`）+ ExoPlayer；Manifest 声明前台服务类型 `mediaPlayback`、`POST_NOTIFICATIONS` 权限请求（Android 13+）。
2. **`Media3PlaybackRepository`** 实现 `PlaybackRepository`（只改 `PlaybackModule` 的一行 `@Binds`）：通过 `MediaController` 连接服务；暴露 `nowPlaying`、`positionFlow`（播放时按固定间隔更新）、play / pause / seek / setSpeed / playEpisode。
3. **进度持久化**：定期 + 暂停 / 切集时把位置写入 `PlaybackProgress`；进度超过约 95% 标记已播放。重新打开 App 时恢复上次的单集与位置（不自动播放）。
4. **扩展 domain**：`PlayEpisodeUseCase`、`SetSpeedUseCase`、`ObservePlaybackPositionUseCase`；`NowPlaying` 增加 episodeId、artworkUrl、duration、position。
5. **MiniPlayer 接真数据**；节目详情的"播放最新一集"、单集行点击播放。
6. **全屏播放器**（外壳级覆盖层，见 §3.2）：封面、标题、进度拖动、±15/30、上一集 / 下一集、倍速、收藏、下载、队列、简介入口、"接下来"条（[08-全屏播放器.png](../EchoPod-设计稿/PNG/01-手机/08-全屏播放器.png)）。
   - Medium：单栏放大（[10-播放器-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/10-播放器-竖向.png)）。
   - Expanded：左控制、右章节 / 简介 / 队列 Tab（[05-展开-横向播放器双栏.png](../EchoPod-设计稿/PNG/02-折叠屏/05-展开-横向播放器双栏.png)）；章节数据在阶段 6 完成前显示空状态。
   - 背景色：先用 `container` 系列令牌，动态封面取色在阶段 15 做。
7. **定时关闭**（播放器"定时"按钮）：本阶段实现基础版（15/30/60 分钟、本集结束）。

**验收**：锁屏 / 切后台持续播放；通知栏与蓝牙耳机按键可控；折叠 / 展开 / 旋转时**播放不中断**；杀进程重启后能恢复到上次位置。
**测试**：仓库层用 Fake `Player` 或抽象出 `PlayerController` 接口来测进度保存逻辑；ViewModel 单测；手动验证清单。

---

## 阶段 5：首页 + 订阅页（真实内容）

**目标**：完成两个最常用的一级 Tab，并引入 Expanded 下的列表 / 详情双栏。

1. **首页**（[01-首页.png](../EchoPod-设计稿/PNG/01-手机/01-首页.png)）
   - 页头：EchoPod、日期 · "N 个节目有更新"、搜索按钮、"+ 导入"（Compact）。
   - 继续收听卡片（最近一个未播完的单集）、我的订阅横向列表（Compact 横滑；Medium **5 列**网格）、最新更新（Compact 1 列；Medium **2 列**）、下拉刷新。
   - Medium：搜索框常驻页头（[02-首页-竖向.png](../EchoPod-设计稿/PNG/02-折叠屏/02-首页-竖向.png)）。
   - Expanded：首页 + 右侧详情栏，迷你播放器停靠在详情栏底部（[03-首页-横向.png](../EchoPod-设计稿/PNG/02-折叠屏/03-首页-横向.png)）——外壳 `miniPlayer` 槽位在 Expanded 下改由页面渲染。
2. **订阅页**（[04-订阅.png](../EchoPod-设计稿/PNG/01-手机/04-订阅.png)）
   - 统计行（N 个节目 · N 集未听）、筛选 Chip（全部 / 有更新 / 分类）、节目列表（未听数、最近更新时间）、"刷新失败 · 显示缓存"提示条。
   - Expanded：`ListDetailPaneScaffold`，左订阅列表、右节目详情（[03-展开-横向订阅双栏.png](../EchoPod-设计稿/PNG/02-折叠屏/03-展开-横向订阅双栏（Expanded）.png)）；**选中的节目与筛选条件在折叠 / 展开时保留**。
3. **手动刷新**：`RefreshSubscriptionsUseCase`（并发拉取所有订阅，限制并发数，单个失败不影响其他）。
4. **分类**：订阅时从 RSS `itunes:category` 映射；用户自定义分组（通勤 / 睡前）作为后续需求记入 backlog。
5. 删除阶段 0 的 `TopLevelPlaceholder` 在这两页的使用。

**验收**：与浅色 / 深色设计稿逐一对比（含 [14-首页-深色](../EchoPod-设计稿/PNG/01-手机/14-首页-深色.png)、[26/27-订阅-深色](../EchoPod-设计稿/PNG/02-折叠屏/26-订阅-深色-竖向.png)）；Expanded 双栏折叠后详情变为单栏当前页。
**测试**：`HomeViewModel` / `SubscriptionsViewModel` 单测；Compose UI 测试覆盖 Compact / Medium / Expanded 三种布局下的关键元素。

---

## 阶段 6：单集详情 + 章节

1. **单集详情页** `EpisodeRoute(episodeId)`（[07-单集详情.png](../EchoPod-设计稿/PNG/01-手机/07-单集详情.png)）：元信息（期数、日期、时长、大小、来源）、播放按钮、简介 / Show Notes / 章节 三个 Tab、"本期提到"链接。
2. **Show Notes 渲染**：HTML → `AnnotatedString`（链接、粗体、列表），**不使用 WebView**；外部链接用 Custom Tabs 或系统浏览器打开。
3. **章节来源**（按优先级）：Podcasting 2.0 `podcast:chapters` JSON → Show Notes 中的时间戳行（`00:00 开场…`）解析。点击章节跳转播放位置。
4. **Expanded**：节目详情中的单集列表 + 单集详情双栏（[07-展开-横向单集列表与详情.png](../EchoPod-设计稿/PNG/02-折叠屏/07-展开-横向单集列表与详情.png)）；与阶段 5 的订阅双栏组合成三级：订阅 → 节目 → 单集。
5. 播放器 Expanded 右栏的"章节"Tab 接入真实数据。

**测试**：章节解析器单测（多种时间格式）、HTML 转换单测。

---

## 阶段 7：播放队列

1. **domain**：`QueueRepository`（Room 表 `queue`）+ `AddToQueue`、`RemoveFromQueue`、`MoveInQueue`、`ClearQueue`、`ObserveQueue` UseCase。
2. **播放服务联动**：当前集播完 → 若开启"自动播放下一集"，标记已播放并播放队列下一项；Media3 播放列表与数据库队列保持一致（以数据库为准）。
3. **队列页**（[09-播放队列.png](../EchoPod-设计稿/PNG/01-手机/09-播放队列.png)）：正在播放卡片、自动播放开关、"接下来 N 集 · 约 X 小时"、**长按拖动排序**（基于 `LazyColumn` + `animateItem` 自行实现，不引入拖拽库，除非实现成本过高）、左滑 / 按钮移除、清空（二次确认）。
4. 入口：我的 → 播放队列、全屏播放器"队列"、单集行的"加入队列"菜单。
5. Medium：[10-展开-竖向播放队列.png](../EchoPod-设计稿/PNG/02-折叠屏/10-展开-竖向播放队列.png)。

**验收**：拖动排序后杀进程重启顺序不变；队列播完自动停止。
**测试**：排序算法单测、自动下一集逻辑单测。

---

## 阶段 8：发现与搜索

1. **发现页**（[02-发现.png](../EchoPod-设计稿/PNG/01-手机/02-发现.png)）：搜索框、"添加播客"三个入口（RSS / 小宇宙 / OPML → 打开导入对话框对应分段）、最近搜索（DataStore，可清空）、按分类浏览。
2. **搜索**（[03-搜索结果.png](../EchoPod-设计稿/PNG/01-手机/03-搜索结果.png)）：
   - 数据源抽象为 `PodcastDirectory` 接口，多个实现并行查询并合并：**本地已订阅** + **海外目录**（如 iTunes Search API）+ **国内目录**（数据源待调研确定，接入前确认服务条款）。
   - 单个目录失败时显示"海外目录暂时无法连接 · 已显示其他结果 · 重试"，不让整页失败。
   - 输入防抖（约 300ms）、结果分"节目 / 主播"Tab、显示"已订阅"状态、一键订阅（复用阶段 3 的 `SubscribeUseCase`）。
3. **分类浏览**：先用目录 API 的分类榜单；无合适 API 时降级为"按分类搜索"。
4. Medium / Expanded：左发现、右搜索结果（[06/07-发现与搜索](../EchoPod-设计稿/PNG/02-折叠屏/06-发现与搜索-竖向.png)）。

**测试**：多目录合并与部分失败的单测；防抖用 `runTest` 虚拟时间测试。

---

## 阶段 9：导入完整版（小宇宙链接、OPML、批量）

1. **小宇宙链接**：识别节目 / 单集分享链接 → 获取页面中公开的节目名称与主播 → 在公开目录中查找该节目的 **公开 RSS 地址** → 校验"节目名称、主播、单集标题一致" → 预览并订阅。
   - **原则**（与设计稿文案一致）：只使用公开 RSS；找不到公开音频源时明确提示"可能为付费或私有内容，未做任何绕过"。订阅后只通过 RSS 获取更新。
   - 识别逻辑做成 `ShareLinkResolver` 接口，便于以后支持其他平台。
2. **批量**：多行输入，每行独立解析；结果汇总"成功 N · 重复 N · 失败 N"并列出失败原因。
3. **OPML 导入**：系统文件选择器（SAF）选文件 → 解析 → 批量订阅；**OPML 导出**（我的 → 导入与导出）。
4. **备份**（可选，P1）：导出订阅 + 进度 + 收藏为 JSON。

**测试**：链接识别、OPML 解析 / 生成、批量结果汇总单测。

---

## 阶段 10：后台刷新与更新提醒

1. **WorkManager** 周期任务 `RefreshFeedsWorker`（间隔来自设置：每 3 / 6 / 12 小时），约束：联网；可选"仅 Wi-Fi"。Hilt 注入 Worker（hilt-work）。
2. **增量更新**：使用 HTTP `ETag` / `Last-Modified`，只写入新单集；记录每个节目的 `hasNewEpisodes`。
3. **更新圆点**：首页"N 个节目有更新"、订阅列表与首页订阅格子上的 `updateDot`；进入节目详情后清除。
4. （可选）新单集通知，默认关闭。
5. 若设置开启"自动下载新节目"，刷新后为新单集创建下载任务（依赖阶段 11，可先留接口）。

**测试**：Worker 用 `work-testing` 单测；增量合并逻辑单测。

---

## 阶段 11：离线下载

1. **下载引擎**：推荐 Media3 `DownloadManager` + `DownloadService`（下载内容直接被播放器缓存使用，断网可播）；存储在应用专属目录。
2. **domain**：`DownloadRepository`、`Enqueue / Cancel / Retry / Delete / CleanPlayed` UseCase；状态映射为 Queued / WaitingWifi / Running / Failed / Done。
3. **策略**：仅 Wi-Fi、同时下载数（默认 2）、已播放自动清理；显示"应用专属存储 x GB / 设备可用 y GB"。
4. **下载页**（[11-离线下载.png](../EchoPod-设计稿/PNG/01-手机/11-离线下载.png)）：进行中（进度、速度、重试）、已完成（断网可播放、未播放 / 已播放）、"清理 N 集已播放，释放 X MB"。
5. **接入点**：首页最新更新的下载按钮、节目详情"已下载"筛选、全屏播放器"下载"、单集详情。
6. Expanded：我的 + 下载双栏（[17-我的与离线下载-横向](../EchoPod-设计稿/PNG/02-折叠屏/17-我的与离线下载-横向.png)）。

**验收**：开飞行模式能播放已下载单集；下载中切换 Wi-Fi → 移动网络时按"仅 Wi-Fi"暂停。

---

## 阶段 12：我的、收藏、历史、设置

1. **我的页**（[05-我的.png](../EchoPod-设计稿/PNG/01-手机/05-我的.png)）：本地模式说明、本周收听摘要（阶段 14 前先显示基础数字或隐藏）、入口列表（播放队列、离线下载、我的收藏、播放历史、导入与导出、设置）。
2. **收藏**：全屏播放器心形按钮、单集详情；收藏列表页。
3. **播放历史**：最近 30 天，基于 `PlaybackProgress.lastPlayedAt`。
4. **设置页**（[13-设置.png](../EchoPod-设计稿/PNG/01-手机/13-设置.png)）——每一项都要**真正生效**：
   - 外观：主题模式（跟随系统 / 浅色 / 深色）→ `EchoPodTheme(darkTheme = …)` 由 `MainActivity` 根据设置决定；动态封面取色开关（阶段 15 实现效果）。
   - 播放：默认速度、快进 / 快退秒数（同步到 MiniPlayer、播放器、通知栏）、自动播放下一集、耳机拔出时暂停（`AudioManager.ACTION_AUDIO_BECOMING_NOISY`）。
   - 下载与订阅：仅 Wi-Fi、自动刷新频率（重新调度 Worker）、自动下载新节目。
5. Expanded：设置分组列表 + 详情双栏（[19-设置-横向](../EchoPod-设计稿/PNG/02-折叠屏/19-设置-横向.png)）。

**测试**：`SettingsViewModel` 单测；主题切换的 UI 测试。

---

## 阶段 13：折叠姿态（半折叠）

依赖：阶段 4 播放器、阶段 5 / 6 双栏。

1. `core/adaptive` 增加 `rememberPosture()`：基于 `currentWindowAdaptiveInfo().windowPosture`（`isTabletop`、`hingeList`），输出 `Flat / Tabletop(hingeBounds) / Book(hingeBounds)`。
2. **桌面模式播放器**（水平铰链）：上半屏封面与标题，下半屏进度与控制；**任何控件不跨铰链**（[04-半折叠-桌面模式播放器](../EchoPod-设计稿/PNG/02-折叠屏/04-半折叠-桌面模式播放器.png)、[13-横向](../EchoPod-设计稿/PNG/02-折叠屏/13-半折叠-桌面模式播放器（横向）.png)）。
3. **书本模式**（垂直铰链）：播放器左封面控制、右章节（[12-书本模式播放器](../EchoPod-设计稿/PNG/02-折叠屏/12-半折叠-书本模式播放器（竖向）.png)）；所有 `ListDetailPaneScaffold` 的分界线对齐铰链。
4. 姿态切换时**播放不中断、滚动位置不丢**。

**验收**：在 Pixel Fold 模拟器的"半折叠"姿态下验证；Compose UI 测试用 `DeviceConfigurationOverride` / 自定义 `WindowAdaptiveInfo` 注入姿态。

---

## 阶段 14：收听统计（P1）

1. **记录**：播放服务在播放中按真实经过时间累计 `ListeningSession`（倍速折算前的真实时长；拖动跳过的部分不计入），停止 / 切集时写库。
2. **聚合 UseCase**：日 / 周 / 月 / 年的总时长、集数、累计、同比变化、每日柱状数据、"最常听"排行。
3. **统计页**（[12-收听统计.png](../EchoPod-设计稿/PNG/01-手机/12-收听统计（P1）.png)）：时间段切换、汇总卡片、柱状图（`Canvas` 自绘，不引入图表库）、排行；Medium / Expanded 见 [20/21-收听统计](../EchoPod-设计稿/PNG/02-折叠屏/20-收听统计-竖向.png)。
4. 我的页的"本周收听"接入真实数据。

**测试**：聚合计算单测（跨天、跨周边界、时区）。

---

## 阶段 15：打磨与发布

1. **动态封面取色**：用 palette 从封面提取主色，生成播放器背景渐变；对比度不足时回退到令牌色；受设置开关控制。
2. **动效**：统一 200ms（按钮、选中态）/ 300ms（页面、播放器展开）；共享元素（迷你播放器 → 全屏播放器封面，可选）。
3. **无障碍**：TalkBack 朗读顺序、所有图标按钮的 contentDescription、字体放大 200% 不截断、触控区 ≥ 48dp 全量检查。
4. **深色模式全量对比**：所有页面对照设计稿深色版本；确认无 M3 默认色泄漏。
5. **性能**：Compose Compiler Metrics 检查不稳定参数；Baseline Profile；列表滚动帧率；冷启动时间。
6. **Release**：R8 混淆与 keep 规则、签名配置、版本号策略、隐私说明（本地模式、无账号、网络请求仅用于拉取 RSS / 目录搜索）。
7. **（可选）模块化**：按 §3.1 的边界拆分 Gradle 模块。

---

## 4. 每个阶段的固定工作流

1. **写阶段 coding steps 文档**（`docs/coding-steps-<feature>.md`）：从 HTML 设计稿提取尺寸 / 文案 / 状态 → 列出 domain / data / presentation 文件 → 拆分提交。
2. **domain**：模型 + 仓库接口 + UseCase + 单测。
3. **data**：实现 + Hilt 绑定 + 单测（Fake 网络 / 内存数据库）。
4. **presentation**：`UiState` + `ViewModel`（+ 单测）→ `XxxScreen` 无状态 UI（浅 / 深色预览）→ `XxxRoute` 接线。
5. **三种断点**：Compact → Medium → Expanded 依次实现，Compose UI 测试覆盖三种断点。
6. **验证**：`./gradlew :app:assembleDebug`、`./gradlew :app:testDebugUnitTest`；有 UI 测试时跑 `connectedDebugAndroidTest`；在 Pixel Fold 模拟器上做折叠 / 展开 / 旋转连续性检查。
7. **更新文档**：在本路线图对应阶段打勾，记录偏离设计稿的决定。

### 每个阶段的"完成定义"（Definition of Done）

- [ ] 浅色 / 深色与设计稿对比通过
- [ ] Compact / Medium / Expanded 三种断点布局完成并有 UI 测试
- [ ] 折叠 / 展开 / 旋转：播放不中断、Tab / 选中项 / 滚动位置 / 输入内容不丢
- [ ] Edge-to-edge：系统栏后面是页面底色，内容不被遮挡
- [ ] 所有新 UseCase、ViewModel 有 JVM 单测
- [ ] 颜色只来自 `EchoPodTheme.colors`；触控区 ≥ 48dp
- [ ] 没有引入本阶段未使用的依赖
- [ ] `assembleDebug` 与 `testDebugUnitTest` 通过

---

## 5. 风险与待定事项

| 事项 | 影响阶段 | 处理方式 |
|---|---|---|
| 国内播客目录的数据来源 | 8 | 阶段 8 开工前调研可用的公开接口与服务条款；未确定前只上线本地 + 海外目录 |
| 小宇宙页面结构变化 / 无公开 RSS | 9 | 解析逻辑集中在 `ShareLinkResolver`，失败时给出明确提示，不做任何绕过 |
| 新版 AndroidX 库要求更高 compileSdk | 2 – 15 | 接入新依赖前检查；需要时单独一个提交升级 compileSdk / AGP |
| 章节数据缺失（多数 RSS 没有章节） | 6 | 两级降级：`podcast:chapters` → Show Notes 时间戳 → 隐藏章节 Tab |
| Media3 与队列状态双数据源 | 4、7 | 以 Room 为唯一数据源，播放器播放列表由数据库驱动 |
| 中文字体包体积 | 1 | 已决定：全部使用系统字体，不打包字体文件 |
| 拖拽排序实现成本 | 7 | 先自行实现；若交互质量不达标再评估引入成熟的拖拽库 |

---

## 6. 进度总览

| 阶段 | 内容 | 状态 |
|---|---|---|
| 0 | 外壳与导航 | ✅ 基本完成（MiniPlayer UI、UI 测试待做） |
| 1 | 设计系统组件库 | ⏳ |
| 2 | 数据基础设施 | ⏳ |
| 3 | RSS 导入 + 节目详情 | ⏳ |
| 4 | Media3 真实播放 + 全屏播放器 | ⏳ |
| 5 | 首页 + 订阅页 + 双栏 | ⏳ |
| 6 | 单集详情 + 章节 | ⏳ |
| 7 | 播放队列 | ⏳ |
| 8 | 发现与搜索 | ⏳ |
| 9 | 小宇宙 / OPML / 批量导入 | ⏳ |
| 10 | 后台刷新与更新提醒 | ⏳ |
| 11 | 离线下载 | ⏳ |
| 12 | 我的、收藏、历史、设置 | ⏳ |
| 13 | 折叠姿态（桌面 / 书本模式） | ⏳ |
| 14 | 收听统计（P1） | ⏳ |
| 15 | 打磨与发布 | ⏳ |
