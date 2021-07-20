package com.bsoftwares.chatexample.database

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bsoftwares.chatexample.model.LatestChatMessage
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
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
) : Parcelable

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
@Parcelize
data class UsersDB(
    @PrimaryKey
    val userUID: String,
    val userName : String,
    val userToken : String,
    val profilePhoto: String
) : Parcelable