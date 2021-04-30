package com.bsoftwares.chatexample.database

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bsoftwares.chatexample.model.LatestChatMessage
import com.bsoftwares.chatexample.model.loadBitmap

@Entity
data class ChatMessageDB(
    @PrimaryKey
    val messageID : String,
    val text : String,
    val senderId : String,
    val recieverId : String,
    val timeStamp : Long,
    val fromUserName : String,
    val position : Int,
    val chatId : String
)

@Entity
data class LatestMessageDB(
    val messageID: String,
    val text: String,
    val myId: String,
    @PrimaryKey
    val otherId: String,
    val timeStamp: Long,
    val fromUserName: String
)

@Entity
data class UsersDB(
    @PrimaryKey
    val userUID: String,
    val userName : String,
    val userToken : String,
    val profilePhoto: Bitmap
)

suspend fun List<LatestChatMessage>.toProfileImage(context : Context) : Array<UsersDB> {
    return map {
        UsersDB(
            userUID = it.otherId,
            userName = it.fromUserName,
            userToken = "",
            profilePhoto = loadBitmap(it.profilePhotoURL,context = context)
        )
    }.toTypedArray()
}