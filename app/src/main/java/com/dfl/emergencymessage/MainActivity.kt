package com.dfl.emergencymessage

import android.Manifest.permission.DISABLE_KEYGUARD
import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 123
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val channelDescription =
                "Canal de notificación para mostrar mensajes en la pantalla de bloqueo"
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            channel.description = channelDescription
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Verificar si los permisos ya han sido concedidos
        if (!checkPermissions()) {
            // Si no se han concedido los permisos, solicitarlos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    SYSTEM_ALERT_WINDOW,
                    DISABLE_KEYGUARD
                ),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            checkNotificationPermission()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Verificar si se concedieron los permisos
        if (requestCode == REQUEST_CODE_PERMISSIONS &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            // Si se concedieron los permisos, iniciar el servicio
            startService(Intent(this, LockedService::class.java))
        } else if (!Settings.canDrawOverlays(this)) {

            // Si el permiso no ha sido concedido, solicitar permisos al usuario
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName)
            )
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        } else {
            checkNotificationPermission()

        }
    }

    private fun checkPermissions(): Boolean {
        // Verificar si se concedieron los permisos
        val permissionCheck1 = ContextCompat.checkSelfPermission(
            this,
            SYSTEM_ALERT_WINDOW
        )
        val permissionCheck2 = ContextCompat.checkSelfPermission(
            this,
            DISABLE_KEYGUARD
        )
        return permissionCheck1 == PackageManager.PERMISSION_GRANTED &&
            permissionCheck2 == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            // Verificar si el permiso SYSTEM_ALERT_WINDOW fue concedido
            if (Settings.canDrawOverlays(this)) {
                checkNotificationPermission()
            } else {
                // Si el permiso no fue concedido, mostrar un mensaje de error o realizar otra acción
                Toast.makeText(
                    this,
                    "El permiso SYSTEM_ALERT_WINDOW es necesario para usar esta función",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkNotificationPermission() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)

            val areNotificationsEnabled = notificationManager.areNotificationsEnabled()

            if (!areNotificationsEnabled) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
            }else {
                // Si ya se concedieron los permisos, iniciar el servicio
               startForegroundService(Intent(this, LockedService::class.java))
            }
        }
    }
}