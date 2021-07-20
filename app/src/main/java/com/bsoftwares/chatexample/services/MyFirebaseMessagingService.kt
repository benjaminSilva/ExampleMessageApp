package com.bsoftwares.chatexample.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toIcon
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.broadcast.DirectMessageReply
import com.bsoftwares.chatexample.broadcast.NotificationDismissed
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.database.getDataBase
import com.bsoftwares.chatexample.model.ChatUser
import com.bsoftwares.chatexample.model.NotificationData
import com.bsoftwares.chatexample.model.getBitmap
import com.bsoftwares.chatexample.repository.Repository
import com.bsoftwares.chatexample.services.MyFirebaseMessagingService.Companion.notificationsSent
import com.bsoftwares.chatexample.ui.chat.ChatActivity
import com.bsoftwares.chatexample.utils.Constants
import com.bsoftwares.chatexample.utils.Constants.Companion.KEY_TEXT_REPLY
import com.bsoftwares.chatexample.utils.getCircleImageFromBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File

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

        val message = remoteMessage.data["message"]!!
        val name = remoteMessage.data["title"]!!
        val uid = remoteMessage.data["userUid"]!!
        val url = remoteMessage.data["profileURL"]!!

        val notificationdata = NotificationData(
            title = name,
            message = message,
            userUid = uid,
            profileURL = url
        )


        val bitmap = Picasso.get().load(url).get()

        val person = Person.Builder().setIcon(getCircleImageFromBitmap(bitmap).toIcon())
            .setName(name)
            .setImportant(true)
            .build()

        notificationsSent[notificationdata] = person

        val messageStyle = Notification.MessagingStyle(person).apply {
            for (currentMessage in notificationsSent) {
                addMessage(
                    currentMessage.key.message,
                    System.currentTimeMillis(),
                    currentMessage.value
                )
            }
        }

        createNotificationChannel(channelName = name, channelID = uid)

        createNotification(baseContext,uid,messageStyle,person,message)

        //val bitmap = getCircleImageFromBitmap(userLiveData!!.profilePhoto,baseContext)

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

fun dismissIntent(context: Context): PendingIntent {
    val dismissIntent = Intent(context, NotificationDismissed::class.java)
    return PendingIntent.getBroadcast(context, 0, dismissIntent, 0)
}

private fun createNotification(context : Context,uid : String, messageStyle : Notification.MessagingStyle, person : Person, title: String) {

    val repository = Repository(context)

    repository.loadMessages(otherUid = uid)

    val builder = Notification.Builder(context, uid)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle(title)
        .setAutoCancel(true)
        .setStyle(messageStyle)
        .addPerson(person)
        .setShortcutId(uid)
        .setLocusId(LocusId(uid))
        .setColor(context.resources.getColor(R.color.colorPrimary))
        .addAction(replyAction(uid, context))
        .setContentIntent(openChatWithUid(uid, context))
        .setDeleteIntent(dismissIntent(context))

    with(NotificationManagerCompat.from(context)) {
        notify(0, builder.build())
    }
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
fun updateNotification(
    currentUser: UsersDB,
    otherUser: UsersDB,
    title: String,
    context: Context
) {

    val otherUserPic = getCircleImageFromBitmap(BitmapFactory.decodeFile(File(otherUser.profilePhoto).absolutePath))

    val person = Person.Builder().setIcon(otherUserPic.toIcon())
        .setName(otherUser.userName)
        .build()

    val currentUserPic = getCircleImageFromBitmap(BitmapFactory.decodeFile(File(currentUser.profilePhoto).absolutePath))

    val mySelf =
        Person.Builder().setIcon(currentUserPic.toIcon())
            .setName(currentUser.userName)
            .build()

    notificationsSent[NotificationData(currentUser.userName, title, currentUser.userUID, "")] =
        mySelf

    val messageStyle = Notification.MessagingStyle(person).apply {
        for (mensagem in notificationsSent) {
            addMessage(mensagem.key.message, System.currentTimeMillis(), mensagem.value)
        }
    }

    createNotification(context,otherUser.userUID,messageStyle,person,currentUser.userName)

    /*val builder = Notification.Builder(context, otherUser.userUID)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle(currentUser.userName)
        .setAutoCancel(true)
        .addAction(replyAction(otherUser.userName, context))
        .setDeleteIntent(dismissIntent(context))
        .setContentIntent(openChatWithUid(otherUser.userName, context))
        .setColor(context.resources.getColor(R.color.colorPrimary))
        .setStyle(messageStyle)

    with(NotificationManagerCompat.from(context)) {
        notify(0, builder.build())
    }*/


    /*Glide.with(context)
        .asBitmap()
        .load(currentUser.profileImageUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val mySelf =
                    Person.Builder().setIcon(getCircleImageFromBitmap(resource).toIcon())
                        .setName(currentUser.username)
                        .build()


                notificationsSent[NotificationData(currentUser.username, title, currentUser.uid, "")] =
                    mySelf

                val messageStyle = Notification.MessagingStyle(mySelf).apply {
                    for (mensagem in notificationsSent) {
                        addMessage(mensagem.key.message, System.currentTimeMillis(), mensagem.value)
                    }
                }

                val builder = Notification.Builder(context, otherUser.uid)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(currentUser.username)
                    .setAutoCancel(true)
                    .addAction(replyAction(otherUser.username, context))
                    .setDeleteIntent(dismissIntent(context))
                    .setContentIntent(openChatWithUid(otherUser.username, context))
                    .setColor(context.resources.getColor(R.color.colorPrimary))
                    .setStyle(messageStyle)

                with(NotificationManagerCompat.from(context)) {
                    notify(0, builder.build())
                }

            }
            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })*/

}