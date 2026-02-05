package com.chargercontrol.utils

import java.io.DataOutputStream
import java.io.IOException

object BatteryControl {
    private val CHARGING_CONTROL_PATHS = listOf(
        "/sys/class/power_supply/battery/input_suspend", 
        "/sys/class/power_supply/battery/charging_enabled", 
        "/sys/class/power_supply/usb/device/charge", 
        "/sys/class/power_supply/battery/batt_slate_soc", 
        "/sys/class/power_supply/main/charge_control_limit", 
        "/sys/class/power_supply/charger/enable_charger", 
        "/sys/class/power_supply/battery/mmi_charging_enable",
        "/sys/module/qpnp_smb5/parameters/disable_input_suspend"
    )

    fun stopCharging(): Boolean {
        return executeRootCommand(
            "echo 1 > /sys/class/power_supply/battery/input_suspend", 
            "echo 0 > /sys/class/power_supply/battery/charging_enabled", 
            "echo 0 > /sys/class/power_supply/charger/enable_charger" 
        )
    }

    fun resumeCharging(): Boolean {
        return executeRootCommand(
            "echo 0 > /sys/class/power_supply/battery/input_suspend", 
            "echo 1 > /sys/class/power_supply/battery/charging_enabled", 
            "echo 1 > /sys/class/power_supply/charger/enable_charger" 
        )
    }
    private fun executeRootCommand(vararg commands: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            for (cmd in commands) {
                os.writeBytes("$cmd\n")
            }
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }
}
