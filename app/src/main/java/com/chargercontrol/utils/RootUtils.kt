package com.chargercontrol.utils

import com.topjohnwu.superuser.Shell

object RootUtils {

    fun isRootGranted(): Boolean {
        return Shell.getShell().isRoot
    }

    fun setCharging(enable: Boolean): Boolean {
        val value = if (enable) "1" else "0"
        val commands = arrayOf(
            "chmod 644 /sys/class/power_supply/battery/charging_enabled",
            "echo $value > /sys/class/power_supply/battery/charging_enabled",
            "chmod 644 /sys/class/power_supply/battery/input_suspend",
            "echo ${if (enable) "0" else "1"} > /sys/class/power_supply/battery/input_suspend"
        )
        
        return Shell.cmd(*commands).exec().isSuccess
    }

    fun readSmart(type: String): String {
        val path = when (type) {
            "volt" -> "/sys/class/power_supply/battery/voltage_now"
            "current" -> "/sys/class/power_supply/battery/current_now"
            "temp" -> "/sys/class/power_supply/battery/temp"
            "level" -> "/sys/class/power_supply/battery/capacity"
            else -> ""
        }
        
        val output = Shell.cmd("cat $path").exec().out
        return if (output.isNotEmpty()) output[0].trim() else "0"
    }
}
