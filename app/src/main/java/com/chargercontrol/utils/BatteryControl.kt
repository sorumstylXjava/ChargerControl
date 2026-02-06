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

    private var cachedControlPath: String? = null
    private var isInvertedLogic = false

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

    fun getBatteryLevel(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
    }

    fun getCurrentNow(): Int {
        val paths = listOf(
            "/sys/class/power_supply/battery/current_now",
            "/sys/class/power_supply/bms/current_now",
            "/sys/class/power_supply/main/current_now",
            "/sys/class/power_supply/battery/batt_current_now",
            "/sys/class/power_supply/usb/current_now"
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
        
        if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            if (currentMa > 0) currentMa *= -1
        } else {
            if (currentMa < 0) currentMa *= -1
        }
        
        return currentMa
    }

    fun getWattage(context: Context): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltageRaw = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val volt = voltageRaw.toFloat() / 1000f
        val amp = getFormattedCurrent(context) / 1000f
        
        return volt * amp
    }

    fun getBatteryTechnology(context: Context): String {
        val sysPath = "/sys/class/power_supply/battery/technology"
        val sysTech = readNodeRoot(sysPath)
        if (sysTech.isNotEmpty()) return sysTech

        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
    }

    fun getDesignedCapacity(context: Context): String {
        val paths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/bms/charge_full_design",
            "/sys/class/power_supply/battery/capacity_design_uah",
            "/sys/class/power_supply/main/charge_full_design"
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
            if (raw.isNotEmpty()) return raw
        }
        return "N/A"
    }

    fun getBatteryHealth(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return when (intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Good"
        }
    }

    private fun findControlPath() {
        if (cachedControlPath != null) return

        val potentialPaths = listOf(
            "/sys/class/power_supply/battery/charging_enabled",
            "/sys/class/power_supply/main/charging_enabled",
            "/sys/class/power_supply/battery/batt_slate_mode",
            "/sys/class/power_supply/battery/battery_charging_enabled",
            "/sys/class/power_supply/charger/charge_disable",
            "/sys/class/power_supply/battery/input_suspend",
            "/sys/module/qpnp_smb5/parameters/force_hvdcp_disable",
            "/sys/class/power_supply/pm8916-bms/battery_charging_enabled"
        )

        for (path in potentialPaths) {
            if (File(path).exists()) {
                cachedControlPath = path
                isInvertedLogic = path.contains("disable") || path.contains("suspend")
                return
            }
        }
    }

    fun setChargingLimit(enable: Boolean) {
        findControlPath()
        val path = cachedControlPath ?: return
        
        val value = if (isInvertedLogic) {
            if (enable) "0" else "1"
        } else {
            if (enable) "1" else "0"
        }

        Runtime.getRuntime().exec(arrayOf("su", "-c", "chmod 666 $path && echo $value > $path"))
    }

    fun setBypassLogic(onComplete: () -> Unit) {
        Thread {
            try {
                setChargingLimit(false)
                Thread.sleep(5000)
                setChargingLimit(true)
            } catch (e: Exception) {}
            onComplete()
        }.start()
    }
}
