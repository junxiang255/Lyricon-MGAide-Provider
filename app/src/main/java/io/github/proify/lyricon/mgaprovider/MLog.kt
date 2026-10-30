package io.github.proify.lyricon.mgaprovider

import android.util.Log
import de.robv.android.xposed.XposedBridge

/**
 * 日志：在毛怪助手进程里走 XposedBridge（可在 LSPosed 日志中查看），
 * 在本模块自身进程里退化为 android.util.Log。
 */
object MLog {

    fun d(msg: String) = write(Log.DEBUG, msg, null)

    fun i(msg: String) = write(Log.INFO, msg, null)

    fun e(msg: String, tr: Throwable? = null) = write(Log.ERROR, msg, tr)

    private fun write(priority: Int, msg: String, tr: Throwable?) {
        val line = "[${Constants.TAG}] $msg" + (tr?.let { " | ${it.javaClass.simpleName}: ${it.message}" } ?: "")
        runCatching { XposedBridge.log(line) }
            .recoverCatching { Log.println(priority, Constants.TAG, line) }
    }
}
