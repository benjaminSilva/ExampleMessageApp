package com.bsoftwares.chatexample.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bsoftwares.chatexample.services.MyFirebaseMessagingService

class NotificationDismissed : BroadcastReceiver() {
    override fun onReceive(contenxt : Context?, intent: Intent?) {
        MyFirebaseMessagingService.notificationsSent.clear()
    }
}