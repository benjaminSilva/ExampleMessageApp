package com.bsoftwares.chatexample.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.utils.saveImageToInternalStorage
import com.squareup.picasso.Picasso
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatUser(
    val uid: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val token: String = ""
)

suspend fun List<ChatUser>.toUserDB(context: Context): Array<UsersDB> {
    return map {
        UsersDB(
            userUID = it.uid,
            userToken = it.token,
            profilePhoto = getBitmap(it.profileImageUrl, context,it.uid),
            userName = it.username
        )
    }.toTypedArray()
}

suspend fun getBitmap(url: String, context: Context,userUid: String): String {
    val loading = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()
    val result = (loading.execute(request) as SuccessResult).drawable
    return saveImageToInternalStorage((result as BitmapDrawable).bitmap,context,userUid)
}

suspend fun getBitmap(url: String, context: Context): Bitmap {
    val loading = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()
    val result = (loading.execute(request) as SuccessResult).drawable
    return (result as BitmapDrawable).bitmap
}