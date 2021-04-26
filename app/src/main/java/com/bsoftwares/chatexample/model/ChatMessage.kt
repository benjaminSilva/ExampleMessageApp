package com.bsoftwares.chatexample.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.LatestMessageDB


data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val recieverId: String = "",
    val timeStamp: Long = -1,
    val position: Int = -1,
    val fromUserName: String = "",
    val toUserToken: String = "",
    val profilePhotoURL: String = "",
    val chatId : String = ""
)

fun ChatMessage.toDatabase(): ChatMessageDB {
    return ChatMessageDB(
        messageID = id,
        text = text,
        senderId = senderId,
        fromUserName = fromUserName,
        recieverId = recieverId,
        timeStamp = timeStamp,
        position = position,
        sendTokenTo = toUserToken,
        chatId = chatId
    )
}

fun List<ChatMessage>.toDatabase(): Array<ChatMessageDB> {
    return map {
        ChatMessageDB(
            messageID = it.id,
            text = it.text,
            senderId = it.senderId,
            fromUserName = it.fromUserName,
            recieverId = it.recieverId,
            timeStamp = it.timeStamp,
            sendTokenTo = it.toUserToken,
            position = it.position,
            chatId = it.chatId
        )
    }.toTypedArray()
}

suspend fun List<LatestChatMessage>.toLatestDatabase(): Array<LatestMessageDB> {
    return map {
        LatestMessageDB(
            messageID = it.messageID,
            text = it.text,
            myId = it.myId,
            fromUserName = it.fromUserName,
            otherId = it.otherId,
            timeStamp = it.timeStamp
            //profilePhoto = loadBitmap(it.profilePhotoURL, context)
        )
    }.toTypedArray()
}

suspend fun loadBitmap(imageUrl: String, context: Context): Bitmap {
    val loading = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .build()

    val result = (loading.execute(request) as SuccessResult).drawable
    return (result as BitmapDrawable).bitmap
}

