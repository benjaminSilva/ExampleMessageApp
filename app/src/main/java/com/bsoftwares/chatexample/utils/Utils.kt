/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bsoftwares.chatexample.utils

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bsoftwares.chatexample.model.PushNotification
import com.bsoftwares.chatexample.network.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean


private val tmpIntArr = IntArray(2)

/**
 * Function which updates the given [rect] with this view's position and bounds in its window.
 */
fun View.copyBoundsInWindow(rect: Rect) {
    if (isLaidOut && isAttachedToWindow) {
        rect.set(0, 0, width, height)
        getLocationInWindow(tmpIntArr)
        rect.offset(tmpIntArr[0], tmpIntArr[1])
    } else {
        throw IllegalArgumentException(
            "Can not copy bounds as view is not laid out" +
                    " or attached to window"
        )
    }
}

/**
 * Provides access to the hidden ViewGroup#suppressLayout method.
 */
fun ViewGroup.suppressLayoutCompat(suppress: Boolean) {
    if (Build.VERSION.SDK_INT >= 29) {
        suppressLayout(suppress)
    } else {
        hiddenSuppressLayout(this, suppress)
    }
}

/**
 * False when linking of the hidden suppressLayout method has previously failed.
 */
private var tryHiddenSuppressLayout = true

@SuppressLint("NewApi") // Lint doesn't know about the hidden method.
private fun hiddenSuppressLayout(group: ViewGroup, suppress: Boolean) {
    if (tryHiddenSuppressLayout) {
        // Since this was an @hide method made public, we can link directly against it with
        // a try/catch for its absence instead of doing the same through reflection.
        try {
            group.suppressLayout(suppress)
        } catch (e: NoSuchMethodError) {
            tryHiddenSuppressLayout = false
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun getCircleImageFromBitmap(bitmap: Bitmap): Bitmap {
    val output: Bitmap
    val srcRect: Rect
    val dstRect: Rect
    val r: Float
    val width = bitmap.width
    val height = bitmap.height
    if (width > height) {
        output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888)
        val left = (width - height) / 2
        val right = left + height
        srcRect = Rect(left, 0, right, height)
        dstRect = Rect(0, 0, height, height)
        r = (height / 2).toFloat()
    } else {
        output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
        val top = (height - width) / 2
        val bottom = top + width
        srcRect = Rect(0, top, width, bottom)
        dstRect = Rect(0, 0, width, width)
        r = (width / 2).toFloat()
    }
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(r, r, r, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
    bitmap.recycle()
    return output
}

fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
    try {
        val response = RetrofitInstance.api.postNotification(notification)
        if(response.isSuccessful) {
            Log.d(TAG, "Response: ${Gson().toJson(response.message())}")
        } else {
            Log.e(TAG, response.errorBody().toString())
        }
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
    }
}

fun saveImageToInternalStorage(bitmap: Bitmap, context: Context, userUid: String): String {
    // Get the image from drawable resource as drawable object

    // Get the bitmap from drawable object

    // Get the context wrapper instance
    val wrapper = ContextWrapper(context)

    // Initializing a new file
    // The bellow line return a directory in internal storage
    var file = wrapper.getDir("images", Context.MODE_PRIVATE)


    // Create a file to save the image
    file = File(file, "${userUid}.jpg")

    try {
        // Get the file output stream
        val stream: OutputStream = FileOutputStream(file)

        // Compress bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        // Flush the stream
        stream.flush()

        // Close stream
        stream.close()
    } catch (e: IOException){ // Catch the exception
        e.printStackTrace()
    }

    // Return the saved image uri
    return file.absolutePath
    //return Uri.parse(file.absolutePath)
}