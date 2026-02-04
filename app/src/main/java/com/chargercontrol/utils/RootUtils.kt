package com.chargercontrol.utils

import com.topjohnwu.superuser.Shell

object RootUtils {
    private val KNOWN_CONTROL_PATHS = listOf(
        "/sys/class/power_supply/battery/charging_enabled",
        "/sys/class/power_supply/battery/input_suspend",
        "/sys/class/power_supply/battery/battery_charging_enabled",
        "/sys/class/power_supply/main/charging_enabled",
        "/sys/class/power_supply/battery/mmi_charging_enable",
        "/sys/class/power_supply/battery/store_mode",
        "/sys/class/power_supply/batt_slate_mode/enabled",
        "/sys/class/power_supply/battery/op_disable_charge"
    )
    private val CURRENT_PATHS = listOf("/sys/class/power_supply/battery/current_now", "/sys/class/power_supply/battery/batt_current")
    private val VOLT_PATHS = listOf("/sys/class/power_supply/battery/voltage_now", "/sys/class/power_supply/battery/batt_vol")
    private val TEMP_PATHS = listOf("/sys/class/power_supply/battery/temp", "/sys/class/power_supply/battery/batt_temp")

    fun isRootGranted(): Boolean {
        return Shell.getShell().isRoot
    }

    fun setCharging(enable: Boolean): Boolean {
        val value = if (enable) "1" else "0"
        val altValue = if (enable) "0" else "1" 
        
        val commands = mutableListOf<String>()
        
        KNOWN_CONTROL_PATHS.forEach { path ->
            if (Shell.cmd("ls $path").exec().isSuccess) {
                commands.add("chmod 644 $path") 
                commands.add("echo $value > $path")
               
                if (path.contains("suspend")) {
                    commands.add("echo $altValue > $path")
                }
            }
        }

        return Shell.cmd(*commands.toTypedArray()).exec().isSuccess
    }
    fun readSmart(type: String): String {
        val paths = when (type) {
            "current" -> CURRENT_PATHS
            "volt" -> VOLT_PATHS
            "temp" -> TEMP_PATHS
            else -> listOf("/sys/class/power_supply/battery/capacity")
        }

        for (path in paths) {
            val output = Shell.cmd("cat $path").exec().out
            if (output.isNotEmpty()) return output[0].trim()
        }
        return "0"
    }
}
