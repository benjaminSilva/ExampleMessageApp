package com.bsoftwares.chatexample.ui.chat

import android.app.Application
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.database.getDataBase
import com.bsoftwares.chatexample.model.*
import com.bsoftwares.chatexample.repository.Repository
import com.bsoftwares.chatexample.utils.Constants
import com.bsoftwares.chatexample.utils.sendNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.core.app.NotificationManagerCompat
import com.bsoftwares.chatexample.utils.observeOnce

class ChatViewModel (application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val dataBase = getDataBase(application)
    private val repository = Repository(dataBase)

    val users = repository.users

    /*val otherUser = MutableLiveData<ChatUser>()

    val currentUser = MutableLiveData<ChatUser>()*/

    val otherUser = repository.otherUser

    val currentUser = repository.currentUser

    val messageSent = MutableLiveData<Boolean>()

    val messages = repository.chatMessages


    fun fetchOtherUser(otherUid: String) {
        repository.fetchOtherUser(otherUid)
        /*FirebaseDatabase.getInstance().getReference("users/$otherUid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                otherUser.value = snapshot.getValue(ChatUser::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })*/
    }

    val currentAndOtherUserAndMessages : LiveData<Triple<ChatUser, ChatUser,List<ChatMessageDB>>> =
        object: MediatorLiveData<Triple<ChatUser, ChatUser, List<ChatMessageDB>>>() {
            var currentUser: ChatUser? = null
            var otherUser: ChatUser? = null
            var messages : List<ChatMessageDB>? = null
            fun update() {
                if (currentUser!= null && otherUser!= null && messages != null){
                    this.value = Triple(currentUser!!,otherUser!!,messages!!)
                }
            }
            init {
                addSource(this@ChatViewModel.currentUser) { currentUser ->
                    this.currentUser = currentUser
                    update()
                    //otherUser?.let { value = users to it }
                }
                addSource(this@ChatViewModel.otherUser) { otherUser ->
                    this.otherUser = otherUser
                    update()
                }
                addSource(this@ChatViewModel.messages) { messages ->
                    this.messages = messages
                    update()
                }
            }
        }

    val usersAndMessages : LiveData<Pair<List<UsersDB>, List<ChatMessageDB>>> =
        object: MediatorLiveData<Pair<List<UsersDB>, List<ChatMessageDB>>>() {
            var user: List<UsersDB>? = null
            var message: List<ChatMessageDB>? = null
            init {
                addSource(users) { users ->
                    this.user = users
                    message?.let { value = users to it }
                }
                addSource(messages) { message ->
                    this.message = message
                    user?.let { value = it to message }
                }
            }
        }

    fun fetchCurrentUser() {
        repository.fetchCurrentUser()
        /*val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser.value = snapshot.getValue(ChatUser::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })*/
    }

    fun loadMessages(otherUid: String) {
        repository.loadMessages(otherUid)
        /*val myId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance().getReference("/messages/$myId/$otherUid")
        val messages = mutableListOf<ChatMessage>()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                snapshot.children.forEach {
                    val message = it.getValue(ChatMessage::class.java) ?: return
                    messages.add(0, message)
                }
                //chatAdapter.swapData(messages)
                viewModelScope.launch {
                    repository.loadMessages(messages.toDatabase())
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })*/
    }

    fun sendMessage(message : String){
        repository.sendMessage(message)
    }

    /*fun sendMessage(message : String) {
        val myUser = currentUser.value
        val otherUser = otherUser.value
        val myId = FirebaseAuth.getInstance().uid!!
        val otherId = otherUser!!.uid
        val userName = myUser!!.username
        val token = otherUser.token
        val senderRef =
            FirebaseDatabase.getInstance().getReference("/messages/$myId/$otherId").push()
        val receiverRef =
            FirebaseDatabase.getInstance().getReference("/messages/$otherId/$myId").push()
        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/latestMessage/$myId/$otherId")
        val latestMessageToRef =
            FirebaseDatabase.getInstance().getReference("/latestMessage/$otherId/$myId")

        val chatMessageSender = ChatMessage(
            senderRef.key!!,
            text = message,
            senderId = myId,
            recieverId = otherId,
            System.currentTimeMillis(),
            position = if (messages.value==null) 0 else messages.value!!.size,
            userName,
            token,
            chatId = "$myId$otherId"
        )
        val chatMessageReciever = ChatMessage(
            senderRef.key!!,
            text = message,
            senderId = myId,
            recieverId = otherId,
            System.currentTimeMillis(),
            position = if (messages.value==null) 0 else messages.value!!.size,
            userName,
            token,
            chatId = "$otherId$myId"
        )
        val latestMessageSender = LatestChatMessage(
            senderRef.key!!,
            text = message,
            myId = myId,
            otherId = otherId,
            System.currentTimeMillis(),
            otherUser.username,
            profilePhotoURL = otherUser.profileImageUrl
        )
        val latestMessageReciever = LatestChatMessage(
            senderRef.key!!,
            text = message,
            myId = otherId,
            otherId = myId,
            System.currentTimeMillis(),
            userName,
            profilePhotoURL = myUser.profileImageUrl
        )
        senderRef.setValue(chatMessageSender)
        receiverRef.setValue(chatMessageReciever)
        latestMessageRef.setValue(latestMessageSender)
        latestMessageToRef.setValue(latestMessageReciever)
        PushNotification(NotificationData(userName, message,myUser.profileImageUrl,myUser.uid), token)
            .also {
                sendNotification(it)
            }
        messageSent.postValue(true)
    }*/

    fun updateNotification() {
        val builder = NotificationCompat.Builder(getApplication(),Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentText("Message Sent")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        with(NotificationManagerCompat.from(getApplication())) {
            notify(0, builder.build())
        }
    }



    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
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

}