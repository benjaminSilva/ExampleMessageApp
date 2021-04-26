package com.bsoftwares.chatexample.model

import android.content.Context
import android.os.Parcelable
import com.bsoftwares.chatexample.database.UsersDB
import kotlinx.android.parcel.Parcelize

data class ChatUser(
    val uid: String = "",
    val username : String = "",
    val profileImageUrl : String = "",
    val token : String = ""
)

suspend fun List<ChatUser>.toUserDB(context : Context) : Array<UsersDB> {
    return map {
        UsersDB(
            userUID = it.uid,
            userToken = it.token,
            profilePhoto = loadBitmap(it.profileImageUrl,context),
            userName = it.username
        )
    }.toTypedArray()
}