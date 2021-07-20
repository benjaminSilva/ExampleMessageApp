package com.bsoftwares.chatexample.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.bsoftwares.chatexample.database.*
import com.bsoftwares.chatexample.model.*
import com.bsoftwares.chatexample.services.updateNotification
import com.bsoftwares.chatexample.utils.sendNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

class Repository(context: Context) {

    val database = getDataBase(context)

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val latestMessages: LiveData<List<LatestMessageDB>> = database.dao.getLatestMessages()

    val users: LiveData<List<UsersDB>> = database.dao.getImagesList()

    val chatId = MutableLiveData<String>()

    val otherUserUid = MutableLiveData<String?>()

    val currentUserUid = MutableLiveData<String?>()

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


    val currentUserAndOtherUser: LiveData<Pair<ChatUser, ChatUser>> =
        object : MediatorLiveData<Pair<ChatUser, ChatUser>>() {
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


    val currenOtherAndMessages: LiveData<Triple<UsersDB, UsersDB, List<ChatMessageDB>>> =
        object : MediatorLiveData<Triple<UsersDB, UsersDB, List<ChatMessageDB>>>() {
            var current: UsersDB? = null
            var other: UsersDB? = null
            var mensagens : List<ChatMessageDB>? = null
            fun checkNull(){
                if (current!= null && other!= null && !mensagens.isNullOrEmpty()){
                    value = Triple(current!!,other!!,mensagens!!)
                }
            }
            init {
                addSource(getCurrentUser){ result ->
                    current = result
                    checkNull()
                }
                addSource(getOtherUser) { result ->
                    other = result
                    checkNull()
                }
                addSource(chatMessages) { result ->
                    mensagens = result
                    checkNull()
                }
            }
        }

    val currentUserAndOtherUserDB: LiveData<Pair<UsersDB, UsersDB>> =
        object : MediatorLiveData<Pair<UsersDB, UsersDB>>() {
            var user: UsersDB? = null
            var message: UsersDB? = null

            init {
                addSource(getCurrentUser) { users ->
                    this.user = users
                    message?.let {
                        value = users to it

                    }
                    addSource(getOtherUser) { message ->
                        this.message = message
                        user?.let {
                            value = it to message
                        }
                    }
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
                val user = users
                database.dao.insertImages(*user)
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

    fun sendMessage(message: String) {
        try {
            val myUser = currentUser.value!!
            val otherUser = otherUser.value!!

            val senderRef = FirebaseDatabase.getInstance()
                .getReference("/messages/${myUser.uid}/${otherUser.uid}").push()
            val receiverRef = FirebaseDatabase.getInstance()
                .getReference("/messages/${otherUser.uid}/${myUser.uid}").push()
            val latestMessageRef = FirebaseDatabase.getInstance()
                .getReference("/latestMessage/${myUser.uid}/${otherUser.uid}")
            val latestMessageToRef = FirebaseDatabase.getInstance()
                .getReference("/latestMessage/${otherUser.uid}/${myUser.uid}")
            val chatMessageSender = ChatMessage(
                id = senderRef.key!!,
                text = message,
                senderId = myUser.uid,
                recieverId = otherUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = myUser.username,
                chatId = "${myUser.uid}${otherUser.uid}"
            )
            val chatMessageReciever = ChatMessage(
                id = senderRef.key!!,
                text = message,
                senderId = myUser.uid,
                recieverId = otherUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = myUser.username,
                chatId = "${otherUser.uid}${myUser.uid}"
            )
            val latestMessageSender = LatestChatMessage(
                messageID = senderRef.key!!,
                text = message,
                myId = myUser.uid,
                otherId = otherUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = otherUser.username
            )
            val latestMessageReciever = LatestChatMessage(
                messageID = senderRef.key!!,
                text = message,
                myId = otherUser.uid,
                otherId = myUser.uid,
                timeStamp = System.currentTimeMillis(),
                fromUserName = myUser.username
            )
            senderRef.setValue(chatMessageSender)
            receiverRef.setValue(chatMessageReciever)
            latestMessageRef.setValue(latestMessageSender)
            latestMessageToRef.setValue(latestMessageReciever)
            PushNotification(
                NotificationData(
                    myUser.username,
                    message,
                    myUser.uid,
                    myUser.profileImageUrl
                ), otherUser.token
            )
                .also {
                    sendNotification(it)
                }
        } catch (t: IOException) {
            Log.e("ERROR", t.message.toString())
        }

    }

    fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        currentUserUid.postValue(uid!!)
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser.value = snapshot.getValue(ChatUser::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun loadMessages(otherUid: String) {
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

    fun fetchOtherUser(otherUid: String) {
        otherUserUid.postValue(otherUid)
        FirebaseDatabase.getInstance().getReference("users/$otherUid")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    otherUser.value = snapshot.getValue(ChatUser::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun sendMessageInRepository(message: String, uid: String?, context: Context) {
        fetchCurrentUser()
        fetchOtherUser(uid!!)
        currentUserAndOtherUser.observeForever { (current, other) ->
            chatId.postValue(current.uid.plus(other.uid))
            sendMessage(message)
        }
        currentUserAndOtherUserDB.observeForever { (current, other) ->
            updateNotification(current, other, message, context)
        }
    }
}