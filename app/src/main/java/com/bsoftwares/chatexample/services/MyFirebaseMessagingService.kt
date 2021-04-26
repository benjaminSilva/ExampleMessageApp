package com.bsoftwares.chatexample.services

import android.app.*
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.broadcast.DirectMessageReply
import com.bsoftwares.chatexample.ui.chat.ChatActivity
import com.bsoftwares.chatexample.ui.newMessage.NewMessageActivity
import com.bsoftwares.chatexample.utils.Constants
import com.bsoftwares.chatexample.utils.Constants.Companion.KEY_TEXT_REPLY
import com.bsoftwares.chatexample.utils.getCircleBitmap
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val name = remoteMessage.data["title"]
        val message = remoteMessage.data["message"]
        val uid = remoteMessage.data["userUid"]
        val url = remoteMessage.data["profileURL"]
        val bitmap = Picasso.get().load(url).get()
        val circleBitmap = getCircleBitmap(bitmap)

        val openChatIntent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.USER_KEY, uid)
        }

        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(resources.getString(R.string.reply))
            build()
        }

        val openChatPendingIntent = PendingIntent.getActivity(this, 0, openChatIntent, 0)
        val replyPendingIntent = PendingIntent.getActivity(this, 0, openChatIntent, 0)


        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_reply_24,
            resources.getString(R.string.reply), replyPendingIntent
        ).addRemoteInput(remoteInput).build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }


        val builder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(name)
            .setAutoCancel(true)
            .setColor(applicationContext.resources.getColor(R.color.colorPrimary))
            .setContentText(message)
            .setLargeIcon(circleBitmap)
            .addAction(replyAction)
            .setContentIntent(openChatPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build())
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = "Chat Notifications"
        val descriptionText = "For new chat messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val mChannel = NotificationChannel("CHANNEL_ID", name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

}