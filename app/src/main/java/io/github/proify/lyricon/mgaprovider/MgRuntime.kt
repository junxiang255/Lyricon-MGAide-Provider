package io.github.proify.lyricon.mgaprovider

import android.app.Application
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 运行时装配：hook MusicViewModel、启动轮询并向词慕推送。
 */
object MgRuntime {

    @Volatile
    private var started = false

    private var executor: ScheduledExecutorService? = null

    @Volatile
    private var clearedWhileDisabled = false

    fun start(application: Application, classLoader: ClassLoader) {
        if (started) return
        started = true

        hookMusicViewModel(classLoader)
        startPoller(application)

        MLog.i("runtime started, enabled=${MgPrefs.isEnabled(application)}")
    }

    private fun hookMusicViewModel(classLoader: ClassLoader) {
        runCatching {
            val cls = XposedHelpers.findClass(Constants.VM_CLASS, classLoader)
            XposedBridge.hookAllConstructors(cls, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.thisObject?.let {
                        MusicState.capture(it)
                        MLog.i("MusicViewModel captured")
                    }
                }
            })
        }.onFailure { MLog.e("hook MusicViewModel failed", it) }
    }

    private fun startPoller(application: Application) {
        val ex = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "mg-lyric-poller").apply { isDaemon = true }
        }
        executor = ex
        ex.scheduleWithFixedDelay(
            { tick(application) },
            400L,
            Constants.POLL_INTERVAL_MS,
            TimeUnit.MILLISECONDS,
        )
    }

    private fun tick(application: Application) {
        runCatching {
            if (!MgPrefs.isEnabled(application)) {
                if (!clearedWhileDisabled) {
                    ProviderBridge.clear()
                    clearedWhileDisabled = true
                }
                return@runCatching
            }
            clearedWhileDisabled = false

            val snapshot = MusicState.snapshot()
            if (snapshot == null || snapshot.song == null) {
                ProviderBridge.clear()
                return@runCatching
            }
            ProviderBridge.ensure(application)
            ProviderBridge.push(snapshot)
        }.onFailure { MLog.e("tick failed", it) }
    }
}
