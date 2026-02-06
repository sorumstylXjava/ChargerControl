package com.chargercontrol.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object BatteryControl {

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
            if (File(path).exists()) {
                try {
                    val value = readFile(path).trim().toIntOrNull() ?: 0
                    if (value == 0) continue
                    return value 
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return 0
    }

    fun getFormattedCurrent(context: Context): Float {
        val rawCurrent = getCurrentNow()
        val status = getBatteryStatus(context)
        
        var currentMa = rawCurrent.toFloat()
        
        if (Math.abs(currentMa) > 10000) {
            currentMa /= 1000
        }

        if (status == "Discharging" && currentMa > 0) {
            currentMa *= -1
        } else if (status == "Charging" && currentMa < 0) {
            currentMa *= -1
        }

        return currentMa
    }

    fun getWattage(context: Context): Float {
        val voltageMv = getVoltage(context).toFloat() 
        val currentMa = getFormattedCurrent(context)
        
        return (voltageMv * currentMa) / 1000000f
    }

    fun getDesignedCapacity(context: Context): String {
        val paths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/bms/charge_full_design",
            "/sys/class/power_supply/battery/capacity_design_uah",
            "/sys/class/power_supply/main/charge_full_design" 
        )

        for (path in paths) {
            if (File(path).exists()) {
                try {
                    val content = readFile(path).trim()
                    val value = content.toLongOrNull() ?: continue
                    
                    val capacityMah = if (value > 100000) value / 1000 else value
                    return "$capacityMah mAh"
                } catch (e: Exception) {
                    continue
                }
            }
        }

        val powerProfileClass = "com.android.internal.os.PowerProfile"
        try {
            val mPowerProfile = Class.forName(powerProfileClass)
                .getConstructor(Context::class.java)
                .newInstance(context)
            val batteryCapacity = Class.forName(powerProfileClass)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
            return "${batteryCapacity.toInt()} mAh"
        } catch (e: Exception) {
            return "N/A"
        }
    }

    fun getCycleCount(): String {
        val paths = listOf(
            "/sys/class/power_supply/battery/cycle_count",
            "/sys/class/power_supply/bms/cycle_count",
            "/sys/class/power_supply/main/cycle_count",
            "/sys/class/power_supply/battery/battery_cycle"
        )

        for (path in paths) {
            if (File(path).exists()) {
                try {
                    val count = readFile(path).trim()
                    if (count.isNotEmpty()) return count
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return "N/A"
    }

    fun getTemperature(context: Context): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10.0f
    }

    private fun readFile(path: String): String {
        return try {
            BufferedReader(FileReader(path)).use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    fun setChargingLimit(limit: Int, enable: Boolean) {
        val commands = listOf(
            "echo ${if (enable) 1 else 0} > /sys/class/power_supply/battery/charging_enabled",
            "echo ${if (enable) 1 else 0} > /sys/class/power_supply/battery/input_suspend",
            "echo ${if (enable) 0 else 1} > /sys/class/power_supply/battery/input_suspend", 
            "echo ${if (enable) 1 else 0} > /sys/class/power_supply/main/charging_enabled"
        )
        
        Thread {
            try {
                val p = Runtime.getRuntime().exec("su")
                val os = p.outputStream
                for (cmd in commands) {
                    os.write((cmd + "\n").toByteArray())
                }
                os.write("exit\n".toByteArray())
                os.flush()
                os.close()
                p.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun setBypass(enable: Boolean) {
         val commands = listOf(
            "echo ${if (enable) 1 else 0} > /sys/class/power_supply/battery/battery_charging_enabled",
            "echo ${if (enable) 1 else 0} > /sys/class/power_supply/battery/charging_enabled"
        )

        Thread {
            try {
                val p = Runtime.getRuntime().exec("su")
                val os = p.outputStream
                for (cmd in commands) {
                    os.write((cmd + "\n").toByteArray())
                }
                os.write("exit\n".toByteArray())
                os.flush()
                os.close()
                p.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
