# 词慕 · 毛怪助手歌词（Lyricon Provider for MG Aide）

给 **毛怪助手（MG Aide / `com.sennagi.mgaide`）** 做的 [词慕（Lyricon）](https://github.com/tomakino/lyricon) 歌词提供器（Xposed 模块）。
模块自带 Compose（仿 Miuix 风格）界面：首页是「状态栏歌词」开关与说明，第二页是关于与我的 GitHub 项目。

- 作者：**junxiang255**（<https://github.com/junxiang255>）
- 简介：新来的毛怪
- 参考实现：<https://github.com/tomakino/LyricProvider>
- 词慕开发文档：<https://tomakino.github.io/lyricon/zh-cn/developer/provider/>
- Provider 库：`io.github.proify.lyricon:provider:0.1.70`

## 功能

| 能力 | 状态 |
|:--|:--|
| 把毛怪助手当前播放的歌曲（歌名/歌手/时长）推给词慕 | ✅ |
| 逐句 + **逐字（KTV）** 歌词推送，含翻译行（`subText` → `secondary`） | ✅ |
| 状态栏歌词随播放进度滚动 | ✅ |
| 模块首页「状态栏歌词」开关（Miuix 风格 UI） | ✅ |
| 开关即时生效（无需重启毛怪助手），关闭后清空词慕歌词 | ✅ |
> 不向毛怪助手界面注入任何悬浮窗或界面元素，所有开关都在本模块 App 内。

## 界面

- `首页`：大标题、`歌词`（状态栏歌词开关）、`使用说明`、`状态`（作用域 / 版本 / 歌词来源）
- `关于`：头像 + 名字 + 简介卡片（点击打开 GitHub）、`我的 GitHub 项目`（实时数据，含 fork 标记 / 语言 / star）、`链接`
- 图标：使用作者提供的立绘头像（自适应图标 + 各密度 mipmap）

截图：[首页](screenshot-home.png) · [关于](screenshot-about.png)

## 原理

### 1. 数据来源：hook 目标应用的 MusicViewModel

毛怪助手的 `com.sennagiapp.mgaide.viewmodel.MusicViewModel` 未被 R8 混淆，公开的
StateFlow 字段名保持原样，插件在 `Application.onCreate` 之后 hook 其构造器拿到实例，
再以 200 ms 周期反射读取：

| 字段 | 内容 |
|:--|:--|
| `currentSong` | `MusicItem`（`getTitle` / `getArtist` / `getDuration`） |
| `progress` | 播放进度（毫秒） |
| `isPlaying` | 播放状态 |
| `lyricsLines` | `List<ve3>`：`time`(毫秒) / `mainText` / `subText` / `words`(`List<bf3>`: `startTime`/`endTime`/`text`) |
| `duration` | 时长（毫秒，作为兜底） |

> 行结构与毛怪助手 `FloatingLyricsService` 内部判定当前行时一致：`line.time <= progressMs + 1000`，
> 因此 `ve3.time` 单位确认为毫秒。

### 2. 推送：Lyricon Provider Bridge

```kotlin
LyriconFactory.createProvider(
    context = app,
    providerPackageName = "io.github.proify.lyricon.mgaprovider",
    playerPackageName = "com.sennagi.mgaide",
).apply { register() }
```

- 歌曲/歌词变化 → `player.setSong(Song(id, name, artist, duration, lyrics = List<RichLyricLine>(...)))`
- 播放状态变化 → `player.setPlaybackState(playing)`
- 进度变化 → `player.setPosition(position)`
- 关闭开关 → `player.setSong(null)`

`RichLyricLine.words` 即逐字时间轴，词慕渲染为 KTV 进度。

### 3. 开关：跨进程配置桥（ContentProvider）

hook 在毛怪助手进程，开关 UI 在模块进程：

- 模块侧写入 `filesDir/config.properties`（`statusbar_lyrics=true/false`）
  —— 用文件而非 SharedPreferences：部分 ROM 的隔离存储会让 prefs 不落地
- `ConfigProvider`（`io.github.proify.lyricon.mgaprovider.config`，exported）用 `call("export")` 返回开关值
- hook 侧 `MgPrefs.isEnabled()` 用 `ContentResolver.call` 读取，800 ms 缓存


## 目录结构

```
mg-aide-provider/
├── settings.gradle.kts
├── build.gradle.kts              # AGP 8.13 / Kotlin 2.3.21 / compose 插件
├── gradle.properties
├── local.properties              # sdk.dir
└── app/
    ├── libs/xposed-api-82.jar    # compileOnly
    └── src/main/
        ├── assets/xposed_init    # io.github.proify.lyricon.mgaprovider.HookEntry
        ├── AndroidManifest.xml   # lyricon_module / xposedmodule / scope / ConfigProvider / INTERNET
        ├── res/mipmap-*/         # 作者头像图标（含 adaptive icon）
        ├── res/drawable-nodpi/avatar.jpg
        └── java/io/github/proify/lyricon/mgaprovider/
            ├── Constants.kt        # 包名、轮询间隔、作者信息
            ├── HookEntry.kt        # IXposedHookLoadPackage 入口
            ├── MgRuntime.kt        # 装配：hook VM、轮询、推送
            ├── MusicState.kt       # 反射读取 MusicViewModel + LRC 行映射
            ├── ProviderBridge.kt   # 词慕 provider 创建/注册/推送/清空
            ├── MgPrefs.kt          # hook 侧读开关（ContentResolver）
            ├── ModulePrefs.kt      # 模块侧读写开关（filesDir/config.properties）
            ├── ConfigProvider.kt   # 跨进程配置出口
            ├── MLog.kt
            └── ui/
                ├── MainActivity.kt # ComponentActivity + Compose
                ├── Theme.kt        # Miuix 风格配色
                ├── Components.kt   # SectionTitle / GroupCard / SwitchRow / 底部导航
                ├── HomeScreen.kt   # 首页
                ├── AboutScreen.kt  # 关于（GitHub 数据 + 缓存）
                └── GithubRepository.kt
```

## 构建

环境：JDK 17+（实测 JDK 24）、Android SDK（platform 36）、Gradle 8.13、Compose BOM 2025.06.01。

```powershell
# AGP 不接受非 ASCII 工程路径，构建目录放纯英文路径
$env:JAVA_HOME="E:\JDK24"
gradle assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

## 使用

1. 安装 `app-debug.apk`。
2. LSPosed → 模块 → 启用「词慕 · 毛怪助手歌词」，作用域勾选 **MG Aide**（`com.sennagi.mgaide`）。
3. 强制停止并重开毛怪助手。
4. 打开本模块 App，在首页打开 / 关闭「状态栏歌词」。
5. 在词慕里把歌词来源选为「毛怪助手」，播放歌曲即可看到状态栏逐字歌词。

## 已验证（PME110 / ColorOS, LSPosed 2.1.0）

```
[MGLyricProvider] runtime started, enabled=true
[MGLyricProvider] MusicViewModel captured
[MGLyricProvider] Lyricon provider created & register() sent
[LyricViewController] onSongChanged: Song(name=NIGHT DANCER, artist=Hanser, duration=210337,
    lyrics=[RichLyricLine(begin=1010, text=どうでもいいような夜だけど,
    secondary=虽是怎样都好的夜晚, words=[LyricWord...])])
[MGLyricProvider/Config] write statusbar_lyrics=false -> .../files/config.properties exists=true
[LyricViewController] onSongChanged: null                        # 关闭即清空
[MGLyricProvider/Github] cached 10 repos                         # 首次拉取并缓存
（再次进入关于页无任何请求日志 → 24h 内走缓存）
```

## 兼容性 / 已知限制

- 依赖目标应用未混淆的 `MusicViewModel` / `MusicItem` 字段名；毛怪助手大版本若改名需同步改 `Constants.VM_CLASS` 与 `MusicState`。
- 只在主进程（`com.sennagi.mgaide`）生效；播放服务与主进程同进程，后台播放时仍持续推送。
- GitHub 列表最多 30 个仓库（API `per_page=30`），fork 会带 `fork` 标记。
- 开关默认开启；如需默认关闭改 `ModulePrefs.isEnabled()` 的默认返回值。

## License

Apache-2.0（与 Lyricon / LyricProvider 一致）。
