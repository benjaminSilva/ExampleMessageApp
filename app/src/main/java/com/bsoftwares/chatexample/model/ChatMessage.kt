package com.bsoftwares.chatexample.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.LatestMessageDB
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val recieverId: String = "",
    val timeStamp: Long = -1,
    val fromUserName: String = "",
    val profilePhotoURL: String = "",
    val chatId : String = ""
)

fun List<ChatMessage>.toDatabase(): Array<ChatMessageDB> {
    return mapIndexed { idx , value ->
        ChatMessageDB(
            messageID = value.id,
            text = value.text,
            senderId = value.senderId,
            fromUserName = value.fromUserName,
            recieverId = value.recieverId,
            timeStamp = value.timeStamp,
            position = idx,
            chatId = value.chatId
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
        )
    }.toTypedArray()
}