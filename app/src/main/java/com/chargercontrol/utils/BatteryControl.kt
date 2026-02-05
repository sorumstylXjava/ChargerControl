package com.chargercontrol.utils

object BatteryControl {
    init {
        System.loadLibrary("native-lib")
    }

    external fun executeRoot(command: String): Int
    external fun readNode(path: String): String

    private val nodes = listOf(
        "/sys/class/power_supply/battery/charging_enabled",
        "/sys/class/power_supply/battery/input_suspend",
        "/sys/class/power_supply/battery/battery_charging_enabled"
    )

    fun getActiveNode(): String {
        for (node in nodes) {
            if (executeRoot("test -f $node") == 0) return node
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
        executeRoot("su -c 'echo $value > $node'")
    }
}
