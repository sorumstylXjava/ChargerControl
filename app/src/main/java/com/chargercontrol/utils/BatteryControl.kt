package com.chargercontrol.utils

import java.io.File
import java.io.DataOutputStream

object BatteryControl {
    // List saklar charger untuk Samsung, Xiaomi, Infinix, Itel
    private val PATHS = listOf(
        "/sys/class/power_supply/battery/input_suspend",
        "/sys/class/power_supply/battery/charging_enabled",
        "/sys/class/power_supply/charger/enable_charger",
        "/sys/class/power_supply/battery/batt_slate_soc",
        "/sys/class/power_supply/battery/store_mode"
    )

    fun checkRoot(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec("su -c id")
            p.waitFor() == 0
        } catch (e: Exception) { false }
    }

    fun setCharging(on: Boolean): Boolean {
        val value = if (on) "1" else "0"
        val altValue = if (on) "0" else "1" 
        
        return try {
            val p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            PATHS.forEach { path ->
                if (File(path).exists()) {
                    os.writeBytes("chmod 666 $path\n")
                    os.writeBytes("echo $value > $path\n")
                    os.writeBytes("echo $altValue > $path\n")
                }
            }
            os.writeBytes("exit\n")
            os.flush()
            p.waitFor() == 0
        } catch (e: Exception) { false }
    }
}
