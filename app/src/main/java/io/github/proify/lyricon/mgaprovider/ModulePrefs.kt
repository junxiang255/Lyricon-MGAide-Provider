package io.github.proify.lyricon.mgaprovider

import android.content.Context
import android.util.Log
import java.io.File
import java.util.Properties

/**
 * 模块 App 侧的开关存储。
 *
 * 使用 filesDir 下的 properties 文件（而不是 SharedPreferences），
 * 避免部分 ROM 的隔离存储导致 prefs 不落地的问题。
 */
object ModulePrefs {

    private const val LOG_TAG = "MGLyricProvider/Config"
    private const val FILE_NAME = "config.properties"
    private const val KEY_ENABLED = "statusbar_lyrics"

    fun isEnabled(context: Context): Boolean = try {
        val file = configFile(context)
        if (!file.exists()) {
            true
        } else {
            val props = Properties()
            file.inputStream().use { props.load(it) }
            props.getProperty(KEY_ENABLED, "true").toBoolean()
        }
    } catch (t: Throwable) {
        Log.w(LOG_TAG, "read config failed", t)
        true
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        try {
            val file = configFile(context)
            val props = Properties()
            if (file.exists()) {
                runCatching { file.inputStream().use { props.load(it) } }
            }
            props.setProperty(KEY_ENABLED, enabled.toString())
            file.outputStream().use { props.store(it, "lyricon mgaprovider") }
            Log.i(LOG_TAG, "write statusbar_lyrics=$enabled -> ${file.absolutePath} exists=${file.exists()}")
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "write config failed", t)
        }
    }

    private fun configFile(context: Context): File = File(context.filesDir, FILE_NAME)
}
