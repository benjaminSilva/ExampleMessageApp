package com.bsoftwares.chatexample.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.bsoftwares.chatexample.database.LatestMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.model.ChatUser
import com.bsoftwares.chatexample.model.LatestChatMessage
import com.bsoftwares.chatexample.model.toLatestDatabase
import com.bsoftwares.chatexample.model.toUserDB
import com.bsoftwares.chatexample.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class HomeViewModel(val application: Context) : ViewModel() {

    val job = SupervisorJob()

    private val viewModelScope = CoroutineScope(job + Dispatchers.Main)
    private val repository = Repository(application)

    val latestMessages = repository.latestMessages

    val profilePictures = repository.users

    val chatId = repository.chatId

    val messages = repository.chatMessages

    val data = repository.currenOtherAndMessages

    val currentUserUid = repository.currentUserUid

    val otherUserUid = repository.otherUserUid




    val picturesAndMessages: LiveData<Pair<List<UsersDB>, List<LatestMessageDB>>> =
        object : MediatorLiveData<Pair<List<UsersDB>, List<LatestMessageDB>>>() {
            var pictures: List<UsersDB>? = null
            var messages: List<LatestMessageDB>? = null

            init {
                addSource(profilePictures) { pictures ->
                    this.pictures = pictures
                    messages?.let { value = pictures to it }
                }
                addSource(latestMessages) { latestMessages ->
                    messages = latestMessages
                    pictures?.let { value = it to latestMessages }
                }
            }
        }

    fun loadLatestMessages() {
        try {
            val chats = mutableListOf<LatestChatMessage>()
            val fromID = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/latestMessage/$fromID")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //chats.clear()
                    snapshot.children.forEach {
                        val chat = it.getValue(LatestChatMessage::class.java)
                        if (chat != null) {
                            chats.add(chat)
                        }
                    }
                    viewModelScope.launch {
                        repository.loadLatestMessages(chats.toLatestDatabase())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", error.message)
                }
            })
        } catch (t: Throwable) {
            Log.e("Error", t.message.toString())
        }
    }

    fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<ChatUser>()
                snapshot.children.forEach {
                    val user = it.getValue(ChatUser::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                viewModelScope.launch {
                    repository.loadUsers(users.toUserDB(context = application))
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    fun updateToken() {
        try {
            val uid = FirebaseAuth.getInstance().uid
            Log.d("TOKEN", FirebaseMessaging.getInstance().token.result!!)
            FirebaseDatabase.getInstance().reference.child("users").child(uid.toString())
                .child("token").setValue(FirebaseMessaging.getInstance().token.result!!)
        } catch (t: Throwable) {
            Log.d("DEU RUIM", t.message.toString())
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }
}