package com.chargercontrol.utils

import android.util.Log
import kotlin.math.abs

object BatteryControl {
    init {
        try { System.loadLibrary("native-lib") } catch (e: Exception) {}
    }

    external fun executeRoot(command: String): Int
    
    private val nodes = listOf(
        "/sys/class/power_supply/battery/charging_enabled",
        "/sys/class/power_supply/battery/input_suspend", 
        "/sys/class/power_supply/battery/battery_charging_enabled",
        "/sys/class/power_supply/main/charging_enabled",
        "/sys/module/dwc3_msm/parameters/suspend_usb" 
    )

    fun checkRoot(): Boolean = executeRoot("su -c 'id'") == 0

    fun getActiveNode(): String {
        for (node in nodes) {
            if (executeRoot("su -c 'test -f $node'") == 0) return node
        }
        return nodes[0]
    }

    fun setCharging(enable: Boolean) {
        val node = getActiveNode()
        val value = if (node.contains("suspend")) {
            if (enable) "0" else "1" 
        } else {
            if (enable) "1" else "0"
        }
        executeRoot("su -c 'chmod 666 $node && echo $value > $node'")
    }

    fun formatCurrent(rawCurrent: Int): Int {
        val currentMA = rawCurrent / 1000
        return abs(currentMA) 
    }
}
