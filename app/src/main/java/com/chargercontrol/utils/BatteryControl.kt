package com.chargercontrol.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object BatteryControl {

    init {
        try {
            System.loadLibrary("native-lib")
        } catch (e: Throwable) {
        }
    }

    external fun executeRoot(command: String): Int
    external fun readNode(path: String): String

    fun getBatteryLevel(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    }

    fun getBatteryStatus(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }

    fun getBatteryHealth(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }
    }

    fun getBatteryTechnology(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-poly"
    }

    fun getVoltage(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    }

    fun getCurrentNow(): Int {
        val paths = listOf(
            "/sys/class/power_supply/battery/current_now",
            "/sys/class/power_supply/bms/current_now",
            "/sys/class/power_supply/main/current_now"
        )
        
        for (path in paths) {
            try {
                val f = File(path)
                if (f.exists()) {
                    val content = f.readText().trim()
                    val value = content.toIntOrNull() ?: 0
                    if (value != 0) return value
                }
            } catch (e: Exception) {
                continue
            }
        }
        return 0
    }

    fun getFormattedCurrent(context: Context): Float {
        val rawCurrent = getCurrentNow()
        var currentMa = rawCurrent.toFloat()
        
        if (Math.abs(currentMa) > 10000) {
            currentMa /= 1000f
        }

        return currentMa
    }

    fun getWattage(context: Context): Float {
        val volt = getVoltage(context).toFloat() / 1000f
        val amp = Math.abs(getFormattedCurrent(context)) / 1000f
        return volt * amp
    }

    fun getDesignedCapacity(context: Context): String {
        val paths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/bms/charge_full_design",
            "/sys/class/power_supply/battery/capacity_design_uah"
        )

        for (path in paths) {
            if (File(path).exists()) {
                try {
                    val value = File(path).readText().trim().toLongOrNull() ?: continue
                    val capacityMah = if (value > 100000) value / 1000 else value
                    return "$capacityMah mAh"
                } catch (e: Exception) { continue }
            }
        }
        return "N/A"
    }

    fun getCycleCount(): String {
        val paths = listOf(
            "/sys/class/power_supply/battery/cycle_count",
            "/sys/class/power_supply/bms/cycle_count"
        )
        for (path in paths) {
            if (File(path).exists()) {
                try {
                    val count = File(path).readText().trim()
                    if (count.isNotEmpty()) return count
                } catch (e: Exception) { continue }
            }
        }
        return "N/A"
    }

    fun getTemperature(context: Context): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0f
    }

    private fun readFile(path: String): String {
        return try {
            File(path).readText().trim()
        } catch (e: Exception) {
            ""
        }
    }

    fun setChargingLimit(enable: Boolean) {
        val value = if (enable) "1" else "0"
        val cmd = "su -c 'echo $value > /sys/class/power_supply/battery/charging_enabled || echo $value > /sys/class/power_supply/main/charging_enabled'"
        Runtime.getRuntime().exec(cmd)
    }

    fun setBypassLogic(onComplete: () -> Unit) {
        Thread {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "echo 0 > /sys/class/power_supply/battery/charging_enabled")).waitFor()
                Thread.sleep(5000)
                Runtime.getRuntime().exec(arrayOf("su", "-c", "echo 1 > /sys/class/power_supply/battery/charging_enabled")).waitFor()
                onComplete()
            } catch (e: Exception) {
                onComplete()
            }
        }.start()
    }
}
