package com.bsoftwares.chatexample.services

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toIcon
import coil.imageLoader
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.broadcast.DirectMessageReply
import com.bsoftwares.chatexample.broadcast.NotificationDismissed
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.model.NotificationData
import com.bsoftwares.chatexample.services.MyFirebaseMessagingService.Companion.notificationsSent
import com.bsoftwares.chatexample.ui.chat.ChatActivity
import com.bsoftwares.chatexample.utils.Constants
import com.bsoftwares.chatexample.utils.Constants.Companion.KEY_TEXT_REPLY
import com.bsoftwares.chatexample.utils.getCircleBitmap
import com.bsoftwares.chatexample.utils.getImageUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    companion object {
        var notificationsSent = LinkedHashMap<NotificationData, Person>()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationdata = NotificationData(
            title = remoteMessage.data["title"]!!,
            message = remoteMessage.data["message"]!!,
            profileURL = remoteMessage.data["userUid"]!!,
            userUid = remoteMessage.data["profileURL"]!!
        )

        val name = remoteMessage.data["title"]!!
        val uid = remoteMessage.data["userUid"]!!
        val url = remoteMessage.data["profileURL"]!!
        val bitmap = getCircleBitmap(Picasso.get().load(url).get())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelName = name, channelID = uid)
        }

        val person = Person.Builder().setIcon(bitmap!!.toIcon())
            .setName(name)
            .setImportant(true)
            .build()

        notificationsSent[notificationdata] = person

        val messageStyle = Notification.MessagingStyle(person).apply {
            for (message in notificationsSent) {
                addMessage(message.key.message, System.currentTimeMillis(), message.value)
            }
        }

        val builder = Notification.Builder(this, uid)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(name)
            .setAutoCancel(true)
            .setStyle(messageStyle)
            .addPerson(person)
            .setShortcutId(uid)
            .setLocusId(LocusId(uid))
            .setColor(applicationContext.resources.getColor(R.color.colorPrimary))
            .addAction(replyAction(uid, this))
            .setBubbleMetadata(createBubbleNotification(bitmap, this))
            .setContentIntent(openChatWithUid(uid, this))
            .setDeleteIntent(dismissIntent(this))

        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createNotificationChannel(channelName: String, channelID: String) {
        val descriptionText = "For new chat messages"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val mChannel = NotificationChannel(channelID, channelName, importance).apply {
            description = descriptionText
            setAllowBubbles(true)
            setShowBadge(true)
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun createBubbleNotification(image: Bitmap, context: Context): Notification.BubbleMetadata {

    val intent = Intent(context, ChatActivity::class.java)

    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

    return Notification.BubbleMetadata.Builder(pendingIntent, image.toIcon())
        .setDesiredHeight(600)
        .setAutoExpandBubble(true)
        .setSuppressNotification(true)
        .build()
}

fun openChatWithUid(uid: String, context: Context): PendingIntent {
    val openChatIntent = Intent(context, ChatActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(Constants.USER_KEY, uid)
    }

    return PendingIntent.getActivity(context, 0, openChatIntent, 0)

}

@RequiresApi(Build.VERSION_CODES.Q)
fun createShortcut(context: Context, userData: NotificationData, bitmap: Bitmap,shortcutManager : ShortcutManager) {
    // Create a dynamic shortcut for each of the contacts.
    // The same shortcut ID will be used when we show a bubble notification.


    val build = ShortcutInfo.Builder(context, userData.userUid)
        .setLocusId(LocusId(userData.userUid))
        .setActivity(ComponentName(context, ChatActivity::class.java))
        .setShortLabel(userData.title)
        .setIcon(bitmap.toIcon())
        .setLongLived(true)
        .setCategories(setOf("com.example.android.bubbles.category.TEXT_SHARE_TARGET"))
        .setIntent(
            Intent(context, ChatActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
        )
        .setPerson(
            Person.Builder()
                .setName(userData.title)
                .setIcon(bitmap.toIcon())
                .build()
        )
        .build()

    val shortcuts = mutableListOf(build)

    shortcutManager.addDynamicShortcuts(shortcuts)

}

fun dismissIntent(context: Context): PendingIntent {
    val dismissIntent = Intent(context, NotificationDismissed::class.java)
    return PendingIntent.getBroadcast(context, 0, dismissIntent, 0)
}

@RequiresApi(Build.VERSION_CODES.N)
fun replyAction(uid: String, context: Context): Notification.Action {

    val remoteInput = android.app.RemoteInput.Builder(KEY_TEXT_REPLY).run {
        setLabel(context.resources.getString(R.string.reply))
        build()
    }

    val directReplyIntent = Intent(context, DirectMessageReply::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(Constants.USER_KEY, uid)
        action = "quick.reply.input"
    }

    val replyPendingIntent = PendingIntent.getBroadcast(context, 0, directReplyIntent, 0)

    return Notification.Action.Builder(
        R.drawable.ic_baseline_reply_24,
        context.resources.getString(R.string.reply), replyPendingIntent
    ).addRemoteInput(remoteInput).setAllowGeneratedReplies(true).build()
}

@RequiresApi(Build.VERSION_CODES.P)
fun updateNotification(currentUser: UsersDB, otherUser: UsersDB, title: String, context: Context) {

    val person = Person.Builder().setIcon(getCircleBitmap(otherUser.profilePhoto)!!.toIcon())
        .setName(otherUser.userName)
        .build()


    val mySelf = Person.Builder().setIcon(getCircleBitmap(currentUser.profilePhoto)!!.toIcon())
        .setName(currentUser.userName)
        .build()

    notificationsSent[NotificationData(currentUser.userName, title, "", currentUser.userUID)] =
        mySelf

    val messageStyle = Notification.MessagingStyle(person).apply {
        for (mensagem in notificationsSent) {
            addMessage(mensagem.key.message, System.currentTimeMillis(), mensagem.value)
        }
    }

    val builder = Notification.Builder(context, Constants.CHANNEL_ID)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle(currentUser.userName)
        .setAutoCancel(true)
        .addAction(replyAction(otherUser.userUID, context))
        .setDeleteIntent(dismissIntent(context))
        .setContentIntent(openChatWithUid(otherUser.userUID, context))
        .setColor(context.resources.getColor(R.color.colorPrimary))
        .setStyle(messageStyle)

    with(NotificationManagerCompat.from(context)) {
        notify(0, builder.build())
    }
}