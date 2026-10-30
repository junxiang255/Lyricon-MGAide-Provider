package io.github.proify.lyricon.mgaprovider

import android.app.Application
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Xposed 入口（assets/xposed_init 指向本类）。
 */
class HookEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != Constants.TARGET_PACKAGE) return
        if (lpparam.processName != Constants.TARGET_PACKAGE) return

        MLog.i("handleLoadPackage -> ${lpparam.packageName}/${lpparam.processName}")

        runCatching {
            XposedHelpers.findAndHookMethod(
                Application::class.java,
                "onCreate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val application = param.thisObject as? Application ?: return
                        runCatching {
                            MgRuntime.start(application, application.classLoader)
                        }.onFailure { MLog.e("start runtime failed", it) }
                    }
                },
            )
        }.onFailure {
            MLog.e("hook Application.onCreate failed", it)
            XposedBridge.log(it)
        }
    }
}
