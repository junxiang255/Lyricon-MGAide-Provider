package io.github.proify.lyricon.mgaprovider

/**
 * 常量集中定义。
 *
 * 目标应用：毛怪助手 MG Aide（com.sennagi.mgaide）
 */
object Constants {

    /** 目标应用包名 */
    const val TARGET_PACKAGE = "com.sennagi.mgaide"

    /** 本模块包名 */
    const val MODULE_PACKAGE = "io.github.proify.lyricon.mgaprovider"

    /** 日志 TAG */
    const val TAG = "MGLyricProvider"

    /** 模块自身偏好文件名（XSharedPreferences 读取） */
    const val PREFS_NAME = "config"

    /** 状态栏歌词开关 */
    const val KEY_ENABLED = "statusbar_lyrics"

    /** MusicViewModel 类名 */
    const val VM_CLASS = "com.sennagiapp.mgaide.viewmodel.MusicViewModel"

    /** 轮询间隔（毫秒） */
    const val POLL_INTERVAL_MS = 200L

    /** 关于页信息 */
    const val AUTHOR = "junxiang255"
    const val AUTHOR_NAME = "俊翔不会玩"
    const val GITHUB_URL = "https://github.com/junxiang255"
    const val PROVIDER_DESCRIPTION = "提供毛怪助手的歌词"
}
