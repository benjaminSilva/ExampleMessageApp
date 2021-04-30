package com.bsoftwares.chatexample.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bsoftwares.chatexample.database.ChatDataBase
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.LatestMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.model.*
import com.bsoftwares.chatexample.services.updateNotification
import com.bsoftwares.chatexample.utils.sendNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*

class Repository(private val database: ChatDataBase) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val latestMessages: LiveData<List<LatestMessageDB>> = database.dao.getLatestMessages()

    val users: LiveData<List<UsersDB>> = database.dao.getImagesList()

    val chatId = MutableLiveData<String>()

    val otherUserUid = MutableLiveData<String>()

    val currentUserUid = MutableLiveData<String>()

    val otherUser = MutableLiveData<ChatUser>()

    val currentUser = MutableLiveData<ChatUser>()

    val chatMessages = Transformations.switchMap(chatId) { chatId ->
        database.dao.getMessagesWithThisUser(chatId)
    }

    val getOtherUser = Transformations.switchMap(otherUserUid) { userUid ->
        database.dao.getUserUid(userUid)
    }

    val getCurrentUser = Transformations.switchMap(currentUserUid) { currentUserUid ->
        database.dao.getUserUid(currentUserUid)
    }

    val currentUserAndOtherUser : LiveData<Pair<ChatUser, ChatUser>> =
        object: MediatorLiveData<Pair<ChatUser, ChatUser>>() {
            var user: ChatUser? = null
            var message: ChatUser? = null
            init {
                addSource(currentUser) { users ->
                    this.user = users
                    message?.let { value = users to it }
                }
                addSource(otherUser) { message ->
                    this.message = message
                    user?.let { value = it to message }
                }
            }
        }

    val currentUserAndOtherUserDB : LiveData<Pair<UsersDB, UsersDB>> =
        object: MediatorLiveData<Pair<UsersDB, UsersDB>>() {
            var user: UsersDB? = null
            var message: UsersDB? = null
            init {
                addSource(getCurrentUser) { users ->
                    this.user = users
                    message?.let { value = users to it }
                }
                addSource(getOtherUser) { message ->
                    this.message = message
                    user?.let { value = it to message }
                }
            }
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

    fun sendMessage(message : String){
        try {
            val messages = chatMessages.value
            val myUser = currentUser.value!!
            val otherUser = otherUser.value!!
            val senderRef = FirebaseDatabase.getInstance().getReference("/messages/${myUser.uid}/${otherUser.uid}").push()
            val receiverRef = FirebaseDatabase.getInstance().getReference("/messages/${otherUser.uid}/${myUser.uid}").push()
            val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latestMessage/${myUser.uid}/${otherUser.uid}")
            val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latestMessage/${otherUser.uid}/${myUser.uid}")
            val chatMessageSender = ChatMessage(
                senderRef.key!!,
                text = message,
                senderId = myUser.uid,
                recieverId = otherUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = myUser.username,
                chatId = "$myUser.uid$otherUser.uid"
            )
            val chatMessageReciever = ChatMessage(
                senderRef.key!!,
                text = message,
                senderId = myUser.uid,
                recieverId = otherUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = myUser.username,
                chatId = "$otherUser.uid$myUser.uid"
            )
            val latestMessageSender = LatestChatMessage(
                senderRef.key!!,
                text = message,
                myId = myUser.uid,
                otherId = otherUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = otherUser.username,
                profilePhotoURL = otherUser.profileImageUrl
            )
            val latestMessageReciever = LatestChatMessage(
                senderRef.key!!,
                text = message,
                myId = otherUser.uid,
                otherId = myUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = myUser.username,
                profilePhotoURL = myUser.profileImageUrl
            )
            senderRef.setValue(chatMessageSender)
            receiverRef.setValue(chatMessageReciever)
            latestMessageRef.setValue(latestMessageSender)
            latestMessageToRef.setValue(latestMessageReciever)
            PushNotification(NotificationData(myUser.username, message,myUser.profileImageUrl,myUser.uid), otherUser.token)
                .also {
                    sendNotification(it)
                }
        }catch (t:Throwable){
            Log.e("ERROR",t.message.toString())
        }

    }

    fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser.value = snapshot.getValue(ChatUser::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun loadMessages(otherUid : String){
        val myId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance().getReference("/messages/$myId/$otherUid")
        val messages = mutableListOf<ChatMessage>()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                snapshot.children.forEach {
                    val message = it.getValue(ChatMessage::class.java) ?: return
                    messages.add(message)
                }
                //chatAdapter.swapData(messages)
                viewModelScope.launch {
                    loadMessages(messages.toDatabase())
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchOtherUser(otherUid : String){
        FirebaseDatabase.getInstance().getReference("users/$otherUid").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                otherUser.value = snapshot.getValue(ChatUser::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun sendMessageInRepository(title: String, uid: String?, context: Context) {
        fetchCurrentUser()
        fetchOtherUser(uid!!)
        currentUserAndOtherUser.observeForever { (current,other) ->
            otherUserUid.postValue(otherUser.value!!.uid)
            currentUserUid.postValue(current.uid)
            chatId.postValue(currentUser.value!!.uid.plus(otherUser.value!!.uid))
            sendMessage(title)
        }
        currentUserAndOtherUserDB.observeForever { (current,other) ->
            updateNotification(current,other,title,context)
        }
    }
}