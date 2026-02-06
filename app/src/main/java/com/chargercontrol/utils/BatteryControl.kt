package com.chargercontrol.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.File

object BatteryControl {

    init {
        try {
            System.loadLibrary("native-lib")
        } catch (e: Throwable) {}
    }

    external fun executeRoot(command: String): Int

    private fun readNodeRoot(path: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
            val result = process.inputStream.bufferedReader().use { it.readText() }.trim()
            if (result.isEmpty()) "" else result
        } catch (e: Exception) {
            ""
        }
    }

    fun getCurrentNow(): Int {
        val paths = listOf(
            "/sys/class/power_supply/battery/current_now",
            "/sys/class/power_supply/bms/current_now",
            "/sys/class/power_supply/main/current_now",
            "/sys/class/power_supply/battery/batt_current_now"
        )
        for (path in paths) {
            val raw = readNodeRoot(path)
            val value = raw.toIntOrNull() ?: 0
            if (value != 0) return value
        }
        return 0
    }

    fun getFormattedCurrent(context: Context): Float {
        val rawCurrent = getCurrentNow()
        var currentMa = rawCurrent.toFloat()
        
        if (Math.abs(currentMa) > 10000) {
            currentMa /= 1000f
        }
        
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        if (status == BatteryManager.BATTERY_STATUS_DISCHARGING && currentMa > 0) {
            currentMa *= -1
        }
        return currentMa
    }

    fun getWattage(context: Context): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val volt = (intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0).toFloat() / 1000f
        val amp = Math.abs(getFormattedCurrent(context)) / 1000f
        return volt * amp
    }

    fun getDesignedCapacity(context: Context): String {
        val paths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/bms/charge_full_design",
            "/sys/class/power_supply/battery/capacity_design_uah",
            "/sys/class/power_supply/battery/batt_full_capacity"
        )
        for (path in paths) {
            val raw = readNodeRoot(path)
            val value = raw.toLongOrNull() ?: continue
            if (value > 0) {
                val mah = if (value > 100000) value / 1000 else value
                return "$mah mAh"
            }
        }
        
        return try {
            val powerProfileClass = "com.android.internal.os.PowerProfile"
            val mPowerProfile = Class.forName(powerProfileClass)
                .getConstructor(Context::class.java)
                .newInstance(context)
            val capacity = Class.forName(powerProfileClass)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
            "${capacity.toInt()} mAh"
        } catch (e: Exception) {
            "N/A"
        }
    }

    fun getCycleCount(): String {
        val paths = listOf(
            "/sys/class/power_supply/battery/cycle_count",
            "/sys/class/power_supply/bms/cycle_count",
            "/sys/class/power_supply/battery/batt_cycle",
            "/sys/class/power_supply/main/cycle_count"
        )
        for (path in paths) {
            val raw = readNodeRoot(path)
            if (raw.isNotEmpty() && raw != "0") return raw
        }
        return "0"
    }

    fun getBatteryHealth(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return when (intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Normal"
        }
    }

    fun setChargingLimit(enable: Boolean) {
        val value = if (enable) "1" else "0"
        val nodes = listOf(
            "/sys/class/power_supply/battery/charging_enabled",
            "/sys/class/power_supply/main/charging_enabled",
            "/sys/class/power_supply/battery/battery_charging_enabled"
        )
        nodes.forEach { node ->
            Runtime.getRuntime().exec(arrayOf("su", "-c", "echo $value > $node"))
        }
    }

    fun setBypassLogic(onComplete: () -> Unit) {
        Thread {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "echo 0 > /sys/class/power_supply/battery/charging_enabled")).waitFor()
                Thread.sleep(5000)
                Runtime.getRuntime().exec(arrayOf("su", "-c", "echo 1 > /sys/class/power_supply/battery/charging_enabled")).waitFor()
            } catch (e: Exception) {}
            onComplete()
        }.start()
    }
}
