package com.chargercontrol.utils

import java.io.DataOutputStream

object BatteryControl {
    fun setChargingLimit(limit: Int): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("echo $limit > /sys/class/power_supply/battery/charge_control_limit\n")
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
