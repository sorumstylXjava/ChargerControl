package com.chargercontrol.utils

import android.os.Build

object BatteryControl {
    init {
        System.loadLibrary("native-lib")
    }

    external fun executeRoot(command: String): Int

    fun setChargingEnabled(enable: Boolean) {
        val value = if (enable) "1" else "0"
        val path = if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            "/sys/class/power_supply/battery/input_suspend" 
        } else {
            "/sys/class/power_supply/battery/charging_enabled"
        }
        
        val cmd = if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
             "su -c 'echo ${if(enable) 0 else 1} > $path'"
        } else {
             "su -c 'echo $value > $path'"
        }
        executeRoot(cmd)
    }
}
