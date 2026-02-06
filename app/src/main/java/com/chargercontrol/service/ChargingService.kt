package com.chargercontrol.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chargercontrol.MainActivity
import com.chargercontrol.data.Prefs
import com.chargercontrol.utils.BatteryControl
import kotlinx.coroutines.*

class ChargingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefs: Prefs

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        startForegroundService()
        monitorBattery()
    }

    private fun startForegroundService() {
        val channelId = "charging_limit_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Battery Control Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Battery Control Active")
            .setContentText("Monitoring charging and bypass...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun monitorBattery() {
        serviceScope.launch {
            while (isActive) {
                val isEnabled = true 
                val limit = 80 
                val currentLevel = BatteryControl.getBatteryLevel(this@ChargingService)

                if (isEnabled) {
                    if (currentLevel >= limit) {
                        BatteryControl.setChargingLimit(false)
                    } else {
                        BatteryControl.setChargingLimit(true)
                    }
                }
                delay(30000)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
