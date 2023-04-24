package com.dfl.emergencymessage

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Aquí se gestiona la notificación al ser publicada en el sistema
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Aquí se gestiona la notificación al ser eliminada del sistema
    }
}