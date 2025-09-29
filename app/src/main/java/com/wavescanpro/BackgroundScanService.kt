package com.wavescanpro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

class BackgroundScanService : Service() {

    private lateinit var wifiManager: WifiManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "WaveScanProChannel")
            .setContentTitle("WaveScan Pro")
            .setContentText("Monitoring signal strength")
            .setSmallIcon(R.drawable.ic_wave)
            .build()
        startForeground(1, notification)

        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        startMonitoring()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "WaveScanProChannel",
                "WaveScan Pro Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startMonitoring() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val sharedPrefs = getSharedPreferences("WaveScanProPrefs", Context.MODE_PRIVATE)
                if (!sharedPrefs.getBoolean("notifications_enabled", true)) return@run
                if (ActivityCompat.checkSelfPermission(this@BackgroundScanService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@run
                wifiManager.startScan()
                val results = wifiManager.scanResults
                results.forEach { result ->
                    if (result.level < -80) {
                        val notification = NotificationCompat.Builder(this@BackgroundScanService, "WaveScanProChannel")
                            .setContentTitle("Signal Drop Alert")
                            .setContentText("Weak signal on ${result.SSID.takeIf { it.isNotEmpty() } ?: "Hidden SSID"}: ${result.level} dBm")
                            .setSmallIcon(R.drawable.ic_wave)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .build()
                        getSystemService(NotificationManager::class.java).notify(result.SSID.hashCode(), notification)
                    }
                }
                handler.postDelayed(this, 10000)
            }
        }, 10000)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
