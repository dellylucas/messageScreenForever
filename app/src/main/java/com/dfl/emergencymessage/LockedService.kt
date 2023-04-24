package com.dfl.emergencymessage

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class LockedService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var view: View
    private val screenStateReceiver = ScreenOnbrodcast()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(screenStateReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        registerReceiver(screenStateReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        view = LayoutInflater.from(this).inflate(R.layout.activity_main, null)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(view, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(view)
        unregisterReceiver(screenStateReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "my_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tu mensaje aquí")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "My Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardLocked) {
            startForeground(1, notificationBuilder.build())
        }

        return START_STICKY
    }
}