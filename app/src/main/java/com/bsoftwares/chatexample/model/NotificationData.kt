package com.bsoftwares.chatexample.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationData (
    val title : String,
    val message : String,
    val profileURL : String,
    val userUid : String
) : Parcelable