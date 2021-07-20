package com.bsoftwares.chatexample.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.bsoftwares.chatexample.database.getDataBase
import com.bsoftwares.chatexample.model.DirectReply
import com.bsoftwares.chatexample.repository.Repository
import com.bsoftwares.chatexample.ui.chat.ChatActivity
import com.bsoftwares.chatexample.ui.chat.ChatViewModel
import com.bsoftwares.chatexample.utils.Constants


class DirectMessageReply : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val repository = Repository(it)
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput!=null){
                val title = remoteInput.getCharSequence(Constants.KEY_TEXT_REPLY).toString()
                val uid = intent!!.getStringExtra(Constants.USER_KEY)
                //ChatViewModel(it).loadMessagesFromBroadcast(context = it,message = title,otherUid = uid!!)
                repository.sendMessageInRepository(title,uid,it)
            }
        }
    }
}