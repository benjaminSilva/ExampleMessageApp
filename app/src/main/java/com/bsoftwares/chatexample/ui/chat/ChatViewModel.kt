package com.bsoftwares.chatexample.ui.chat

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.repository.Repository
import com.bsoftwares.chatexample.services.updateNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : ViewModel() {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val repository = Repository(application)

    val users = repository.users

    //val currentUser = repository.getCurrentUser

    val otherUser = repository.otherUser

    val messages = repository.chatMessages

    //val getOtherUser = repository.getOtherUser

    val chatId = repository.chatId

    fun fetchOtherUser(otherUid: String) {
        repository.fetchOtherUser(otherUid)
    }

    val currentOtherAndMessages = repository.currenOtherAndMessages

    /*val currentUserOtherUserAndMessages: LiveData<Triple<UsersDB, UsersDB, List<ChatMessageDB>>> =
        object : MediatorLiveData<Triple<UsersDB, UsersDB, List<ChatMessageDB>>>() {
            var currentUserLocal: UsersDB? = null
            var otherUserLocal: UsersDB? = null
            var chatMessages: List<ChatMessageDB>? = null
            fun update() {
                val firstUser = currentUserLocal
                val seconduser = otherUserLocal
                val messages = chatMessages
                if (firstUser != null && seconduser != null && !messages.isNullOrEmpty()) {
                    this.value = Triple(firstUser, seconduser, messages)
                }
            }

            init {
                addSource(currentUser) {
                    this.currentUserLocal = it
                    update()
                }
                addSource(getOtherUser) {
                    this.otherUserLocal = it
                    update()
                }
                addSource(messages) {
                    this.chatMessages = it
                    update()
                }
            }
        }*/

    val usersAndMessages: LiveData<Pair<List<UsersDB>, List<ChatMessageDB>>> =
        object : MediatorLiveData<Pair<List<UsersDB>, List<ChatMessageDB>>>() {
            var usuarios: List<UsersDB>? = null
            var mensagens : List<ChatMessageDB>? = null
            init {
                addSource(users){ users ->
                    usuarios = users
                    mensagens?.let { value = users to it }
                }
                addSource(messages) { mess ->
                    mensagens = mess
                    usuarios?.let { value = it to mess }
                }
            }
        }

    fun fetchCurrentUser() {
        repository.fetchCurrentUser()
    }

    fun loadMessages(otherUid: String) {
        repository.loadMessages(otherUid)
    }

    fun sendMessage(message: String) {
        repository.sendMessage(message)
    }

    /*fun loadMessagesFromBroadcast(context: Context, otherUid: String, message: String) {
        viewModelScope.launch {
            fetchCurrentUser()
            fetchOtherUser(otherUid)
        }

        currentAndOtherUser.observeForever { (current, other) ->
            repository.otherUserUid.postValue(other.uid)
            repository.currentUserUid.postValue(current.uid)
            repository.chatId.postValue(current.uid.plus(other.uid))
            sendMessage(message)
        }
        repository.currentUserAndOtherUserDB.observeForever { (current, other) ->
            updateNotification(current, other, message, context)
        }
    }*/


    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }
}