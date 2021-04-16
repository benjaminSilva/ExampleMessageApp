package com.bsoftwares.chatexample.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatUser(
    val uid: String = "",
    val username : String = "",
    val profileImageUrl : String = "",
    val token : String = ""
) : Parcelable