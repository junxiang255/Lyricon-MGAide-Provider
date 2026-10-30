package io.github.proify.lyricon.mgaprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

/**
 * 把模块设置以 ContentProvider 暴露给毛怪助手进程（hook 侧读取）。
 *
 * 使用 provider 而不是 XSharedPreferences：跨进程读取不受 SELinux / 目录权限影响。
 */
class ConfigProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val ctx = context ?: return Bundle.EMPTY
        val out = Bundle()
        when (method) {
            METHOD_EXPORT -> {
                out.putBoolean(Constants.KEY_ENABLED, ModulePrefs.isEnabled(ctx))
            }
        }
        return out
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        const val AUTHORITY = "io.github.proify.lyricon.mgaprovider.config"
        const val METHOD_EXPORT = "export"
    }
}
