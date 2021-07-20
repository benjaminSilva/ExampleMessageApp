package com.bsoftwares.chatexample.ui.chat

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import com.bsoftwares.chatexample.ui.home.HomeActivity
import com.bsoftwares.chatexample.ui.home.HomeViewModel
import com.bsoftwares.chatexample.utils.keyboardanimation.ControlFocusInsetsAnimationCallback
import com.bsoftwares.chatexample.utils.keyboardanimation.RootViewDeferringInsetsCallback
import com.bsoftwares.chatexample.utils.keyboardanimation.TranslateDeferringInsetsAnimationCallback
import com.bsoftwares.chatexample.utils.Constants
import com.bsoftwares.chatexample.utils.createString
import com.bsoftwares.chatexample.utils.getCircleImageFromBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*


class ChatActivity : AppCompatActivity() {

    val viewModel: ChatViewModel by lazy {
        val activity = requireNotNull(this)
        ViewModelProvider(
            this,
            ChatViewModel.Factory(activity.application)
        ).get(ChatViewModel::class.java)
    }


    lateinit var chatAdapter: ChatAdapter

    lateinit var otherUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        otherUid = intent.getStringExtra(Constants.USER_KEY)!!
        /*val messages = intent.getParcelableArrayListExtra<ChatMessageDB>(Constants.MESSAGES_KEY)!!
        val other = intent.getParcelableExtra<UsersDB>(Constants.OTHER_KEY)!!
        val current = intent.getParcelableExtra<UsersDB>(Constants.CURRENT_KEY)!!*/

        viewModel.chatId.postValue(FirebaseAuth.getInstance().uid.plus(otherUid))

        supportActionBar?.hide()
        viewModel.loadMessages(otherUid)
        viewModel.fetchOtherUser(otherUid)
        viewModel.fetchCurrentUser()
        createObservers()
        handleKeyboardAnimation()
        setupRecyclerView()
        setupClicksAndWindow()
        //chatAdapter.swapData(messages,current = current,other = other)
    }

    private fun setupClicksAndWindow() {
        et_txtMessage.setOnKeyListener { _, keycode, event ->
            if (keycode == KeyEvent.KEYCODE_ENTER && (event.action == KeyEvent.ACTION_DOWN)) {
                viewModel.sendMessage(createString(et_txtMessage))
                et_txtMessage.text.clear()
            }
            false
        }

        btn_sendMessage.setOnClickListener {
            viewModel.sendMessage(createString(et_txtMessage))
            et_txtMessage.text.clear()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        ic_backButton.setOnClickListener {
            this.finish()
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(null)
        chatAdapter.setHasStableIds(true)

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                rv_chatLog.scrollToPosition(0)
            }
        })
        rv_chatLog.adapter = chatAdapter
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createObservers() {

        /*viewModel.currenOtherAndMessages.observe(this,{ (current,other,messages) ->
            if (messages != null && current != null && other != null) {
                chatAdapter.swapData(messages.reversed(),current,other)
            }
        })*/

        /*viewModel.usersAndMessages.observe(this, { (users, messages) ->
            chatAdapter.swapData(messages.reversed(), users)
        })*/

        viewModel.currentOtherAndMessages.observe(this,{ (current,other,messages) ->
            chatAdapter.swapData(messages.reversed(),current,other)
            image_toolbar__userImage.setImageURI(Uri.parse(other.profilePhoto))
        })

        /*viewModel.getOtherUser.observe(this, {
            image_toolbar__userImage.setImageURI(Uri.parse(it.profilePhoto))
        })*/

        viewModel.otherUser.observe(this, Observer {
            tv_toolbar_userName.text = it.username
            clearNotifications(it.uid)
        })
    }

    private fun clearNotifications(uid: String) {
        NotificationManagerCompat.from(this).cancelAll()
    }


    private fun handleKeyboardAnimation() {
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

    override fun onBackPressed() {
        //super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
    }
}