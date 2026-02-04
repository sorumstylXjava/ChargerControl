package com.chargercontrol.utils

import com.topjohnwu.superuser.Shell

object RootUtils {
    
    init {
        Shell.enableVerboseLogging = false
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR))
    }

    private val CHARGING_PATHS = listOf(
        "/sys/class/power_supply/battery/charging_enabled",
        "/sys/class/power_supply/battery/input_suspend",
        "/sys/class/power_supply/batt_slate_mode/enabled"
    )

    fun isRootGranted(): Boolean {
        return try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            false
        }
    }

    fun setCharging(enable: Boolean): Boolean {
        val path = getWorkingPath() ?: return false
        val value = if (enable) "1" else "0" 
        val cmd = "echo $value > $path"
        return Shell.cmd(cmd).exec().isSuccess
    }

    fun getWorkingPath(): String? {
        return CHARGING_PATHS.find { path ->
            Shell.cmd("ls $path").exec().isSuccess
        }
    }

    fun readSystemFile(path: String): String {
        return try {
            Shell.cmd("cat $path").exec().out.joinToString("").trim()
        } catch (e: Exception) {
            "0"
        }
    }
}
