package com.bsoftwares.chatexample.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.RemoteInput
import androidx.lifecycle.MutableLiveData
import com.bsoftwares.chatexample.model.DirectReply
import com.bsoftwares.chatexample.ui.chat.ChatActivity
import com.bsoftwares.chatexample.utils.Constants


class DirectMessageReply : BroadcastReceiver() {

    val directReply = MutableLiveData<DirectReply>()

    val viewModel = ChatActivity().viewModel

    override fun onReceive(context: Context?, intent: Intent?) {

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput!=null){
            val title = remoteInput.getCharSequence(Constants.KEY_TEXT_REPLY).toString()
            Log.d("Texto Enviado", title)
            val uid = intent!!.getStringExtra(Constants.USER_KEY)
            Log.d("UID Enviado", uid.toString())
            viewModel.checkUserBeforeMessaging(uid!!,title)
            //directReply.postValue(DirectReply(title, uid!!))
        }
    }

}