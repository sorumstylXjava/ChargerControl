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
import kotlinx.coroutines.flow.first

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
            .setContentText("Monitoring charging limit...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun monitorBattery() {
        serviceScope.launch {
            while (isActive) {
                val isEnabled = prefs.enabledFlow.first()
                val limit = prefs.limitFlow.first()
                val currentLevel = BatteryControl.getBatteryLevel(this@ChargingService)

                if (isEnabled) {
                    if (currentLevel >= limit) {
                        BatteryControl.setChargingLimit(false)
                    } else if (currentLevel <= (limit - 2)) {
                        BatteryControl.setChargingLimit(true)
                    }
                } else {
                    stopSelf()
                }
                delay(10000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        BatteryControl.setChargingLimit(true)
        super.onDestroy()
    }
}
