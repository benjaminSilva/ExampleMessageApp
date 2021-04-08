package com.bsoftwares.chatexample.utils

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            //Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body)
            createNotification(notification = remoteMessage.notification!!)

        }
    }

    private fun createNotification(notification : RemoteMessage.Notification) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            if (token != null) {
                Log.d(TAG, token)
            }
        })
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        //sendRegistrationToServer(token)
    }
}