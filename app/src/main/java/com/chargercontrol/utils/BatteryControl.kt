package com.chargercontrol.utils

import android.util.Log

object BatteryControl {
    init {
        try {
            System.loadLibrary("native-lib")
        } catch (e: Exception) {
            Log.e("RDX8_ENGINE", "Gagal memuat native-lib: ${e.message}")
        }
    }

    external fun executeRoot(command: String): Int
    external fun readNode(path: String): String
    private val nodes = listOf(
        "/sys/class/power_supply/battery/charging_enabled",
        "/sys/class/power_supply/battery/input_suspend",
        "/sys/class/power_supply/battery/battery_charging_enabled",
        "/sys/class/power_supply/main/charging_enabled"
    )

    fun checkRoot(): Boolean {
        return try {
            executeRoot("su -c 'id'") == 0
        } catch (e: Exception) {
            false
        }
    }

    fun getActiveNode(): String {
        for (node in nodes) {
            if (executeRoot("test -f $node") == 0) {
                return node
            }
        }
        return nodes[0]
    }

    fun setCharging(enable: Boolean) {
        val node = getActiveNode()
        
        val value = if (node.contains("input_suspend")) {
            if (enable) "0" else "1"
        } else {
            if (enable) "1" else "0"
        }

        try {
            executeRoot("su -c 'echo $value > $node'")
            Log.d("RDX8_ENGINE", "Set node $node to $value")
        } catch (e: Exception) {
            Log.e("RDX8_ENGINE", "Gagal set charging: ${e.message}")
        }
    }
}
