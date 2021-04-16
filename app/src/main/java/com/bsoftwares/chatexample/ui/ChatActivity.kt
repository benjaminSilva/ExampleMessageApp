package com.bsoftwares.chatexample.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.utils.keyboardanimation.ControlFocusInsetsAnimationCallback
import com.bsoftwares.chatexample.utils.keyboardanimation.RootViewDeferringInsetsCallback
import com.bsoftwares.chatexample.utils.keyboardanimation.TranslateDeferringInsetsAnimationCallback
import com.bsoftwares.chatexample.model.ChatMessage
import com.bsoftwares.chatexample.model.ChatUser
import com.bsoftwares.chatexample.model.NotificationData
import com.bsoftwares.chatexample.model.PushNotification
import com.bsoftwares.chatexample.network.RetrofitInstance
import com.bsoftwares.chatexample.utils.createString
import com.bsoftwares.chatexample.utils.sendNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatActivity : AppCompatActivity() {

    lateinit var chatAdapter: ChatAdapter

    companion object {
        var toUser: ChatUser? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toUser = intent.getParcelableExtra<ChatUser>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        et_txtMessage.setOnKeyListener { _, keycode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keycode == KeyEvent.KEYCODE_ENTER)) {
                sendMessage()
            }
            false
        }
        btn_sendMessage.setOnClickListener {
            sendMessage()
        }

        chatAdapter = ChatAdapter(null)
        chatAdapter.setHasStableIds(true)

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                rv_chatLog.scrollToPosition(0)
            }
        })
        //adapter.setHasStableIds(true)
        rv_chatLog.apply {
            adapter = chatAdapter
        }
        loadMessages()

        /*rv_chatLog.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                isAtTheBottom = !recyclerView.canScrollVertically(1)
            }
        })*/


        /*KeyboardVisibilityEvent.setEventListener(this, object : KeyboardVisibilityEventListener {
            override fun onVisibilityChanged(isOpen: Boolean) {
                if (isAtTheBottom&&chatAdapter.itemCount > 0)
                rv_chatLog.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        })*/

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        val deferringInsetsListener = RootViewDeferringInsetsCallback(
            persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
            deferredInsetTypes = WindowInsetsCompat.Type.ime()
        )
        ViewCompat.setWindowInsetsAnimationCallback(chat_root, deferringInsetsListener)
        ViewCompat.setOnApplyWindowInsetsListener(chat_root, deferringInsetsListener)

        ViewCompat.setWindowInsetsAnimationCallback(
            msg_holder,
            TranslateDeferringInsetsAnimationCallback(
                view = msg_holder,
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime(),
                dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
            )
        )
        ViewCompat.setWindowInsetsAnimationCallback(
            rv_chatLog,
            TranslateDeferringInsetsAnimationCallback(
                view = rv_chatLog,
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
            )
        )

        ViewCompat.setWindowInsetsAnimationCallback(
            et_txtMessage,
            ControlFocusInsetsAnimationCallback(et_txtMessage)
        )
    }

    private fun loadMessages() {
        val fromID = FirebaseAuth.getInstance().uid!!
        val toID = toUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/messages/$fromID/$toID")
        val messages = mutableListOf<ChatMessage>()
        /*ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java) ?: return
                messages.add(message)
                adapter.notifyItemInserted(adapter.itemCount)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })*/
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()

                snapshot.children.forEach {
                    val message = it.getValue(ChatMessage::class.java) ?: return
                    messages.add(0, message)
                }
                chatAdapter.swapData(messages)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun sendMessage() {
        val fromID = FirebaseAuth.getInstance().uid!!
        val toID = toUser!!.uid
        val userName = HomeActivity.currentUser!!.username
        val token = toUser!!.token
        val senderRef =
            FirebaseDatabase.getInstance().getReference("/messages/$fromID/$toID").push()
        val receiverRef =
            FirebaseDatabase.getInstance().getReference("/messages/$toID/$fromID").push()
        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/latestMessage/$fromID/$toID")
        val latestMessageToRef =
            FirebaseDatabase.getInstance().getReference("/latestMessage/$toID/$fromID")
        val chatMessage = ChatMessage(
            senderRef.key!!,
            createString(et_txtMessage),
            fromID,
            toID,
            System.currentTimeMillis(),
            chatAdapter.itemCount,
            userName,
            token
        )
        senderRef.setValue(chatMessage)
        receiverRef.setValue(chatMessage)
        latestMessageRef.setValue(chatMessage)
        latestMessageToRef.setValue(chatMessage)
        PushNotification(NotificationData(userName, createString(et_txtMessage)), token)
            .also {
                sendNotification(it)
            }
        et_txtMessage.text.clear()
    }
}