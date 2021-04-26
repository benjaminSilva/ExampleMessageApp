package com.bsoftwares.chatexample.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bsoftwares.chatexample.database.ChatDataBase
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.LatestMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val database: ChatDataBase) {

    val latestMessages: LiveData<List<LatestMessageDB>> = database.dao.getLatestMessages()

    val users: LiveData<List<UsersDB>> = database.dao.getImagesList()

    private val chatId = MutableLiveData<String>()

    val chatMessages = Transformations.switchMap(chatId) { chatId ->
        database.dao.getMessagesWithThisUser(chatId)
    }

    suspend fun loadLatestMessages(chats: Array<LatestMessageDB>) {
        withContext(Dispatchers.IO) {
            if (chats.isNotEmpty()) {
                database.dao.insertLatestMessages(*chats)
            }
        }

    }

    suspend fun loadUsers(users: Array<UsersDB>) {
        withContext(Dispatchers.IO) {
            if (users.isNotEmpty()) {
                database.dao.insertImages(*users)
            }
        }
    }

    suspend fun loadMessages(messages: Array<ChatMessageDB>) {
        withContext(Dispatchers.IO) {
            database.dao.inserirMessages(*messages)
            if (messages.isNotEmpty())
                chatId.postValue(messages[0].chatId)
        }
    }

}