package com.chargercontrol.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chargercontrol.MainActivity
import com.chargercontrol.R
import com.chargercontrol.data.Prefs
import com.chargercontrol.utils.RootUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ChargingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var prefs: Prefs
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(applicationContext)
        createNotificationChannel()
        startForeground(1, buildNotification("Monitoring Battery..."))
        isRunning = true
        startMonitoring()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isRunning) {
                val limit = prefs.limitFlow.first()
                val isEnabled = prefs.enabledFlow.first()
                val batteryLevel = getBatteryLevel()

                if (isEnabled) {
                    if (batteryLevel >= limit) {
                        RootUtils.setCharging(false)
                        updateNotification("Charging Stopped at $batteryLevel%")
                    } else if (batteryLevel < limit - 5) { 
                        RootUtils.setCharging(true)
                        updateNotification("Charging Active ($batteryLevel%)")
                    }
                }
                delay(3000) 
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return (level * 100 / scale.toFloat()).toInt()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "CHARGER_CHANNEL")
            .setContentTitle("Charger Control")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("CHARGER_CHANNEL", "Charging Control", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        isRunning = false
        serviceScope.cancel()
        super.onDestroy()
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, ChargingService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
