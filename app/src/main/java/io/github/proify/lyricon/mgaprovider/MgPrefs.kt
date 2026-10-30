package io.github.proify.lyricon.mgaprovider

import android.content.Context
import android.net.Uri
import android.os.SystemClock

/**
 * hook 侧读取模块设置。
 *
 * 模块 App 把开关写进自己的 SharedPreferences，并通过 [ConfigProvider]
 * 以 ContentProvider 暴露；这里跨进程读取并做 800ms 缓存。
 */
object MgPrefs {

    @Volatile
    private var cachedEnabled = true

    @Volatile
    private var cachedAt = 0L

    fun isEnabled(context: Context?): Boolean {
        val ctx = context ?: return cachedEnabled
        val now = SystemClock.elapsedRealtime()
        if (now - cachedAt < 800L) return cachedEnabled

        val value = runCatching {
            val uri = Uri.parse("content://${ConfigProvider.AUTHORITY}/config")
            val bundle = ctx.contentResolver.call(uri, ConfigProvider.METHOD_EXPORT, null, null)
            bundle?.getBoolean(Constants.KEY_ENABLED, cachedEnabled) ?: cachedEnabled
        }.getOrDefault(cachedEnabled)

        cachedEnabled = value
        cachedAt = now
        return value
    }
}
