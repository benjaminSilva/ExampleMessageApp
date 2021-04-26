package com.bsoftwares.chatexample.ui.chat

import android.app.RemoteInput
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.utils.keyboardanimation.ControlFocusInsetsAnimationCallback
import com.bsoftwares.chatexample.utils.keyboardanimation.RootViewDeferringInsetsCallback
import com.bsoftwares.chatexample.utils.keyboardanimation.TranslateDeferringInsetsAnimationCallback
import com.bsoftwares.chatexample.utils.Constants
import com.bsoftwares.chatexample.utils.createString
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

    lateinit var otherUid : String

    var isRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        otherUid = intent.getStringExtra(Constants.USER_KEY)!!

        viewModel.loadMessages(otherUid)
        viewModel.fetchOtherUser(otherUid)
        viewModel.fetchCurrentUser()
        createObservers()
        handleKeyboardAnimation()
        setupRecyclerView()
        setupClicksAndWindow()

    }

    private fun setupClicksAndWindow() {
        et_txtMessage.setOnKeyListener { _, keycode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keycode == KeyEvent.KEYCODE_ENTER)) {
                viewModel.sendMessage(createString(et_txtMessage))
            }
            false
        }

        btn_sendMessage.setOnClickListener {
            viewModel.sendMessage(createString(et_txtMessage))
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
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

    private fun createObservers() {
        viewModel.usersAndMessages.observe(this, {(users , messages) ->
            chatAdapter.swapData(messages,users)
        })

        viewModel.messageSent.observe(this, { mensagemEnviada ->
            if (mensagemEnviada)
                et_txtMessage.text.clear()
        })

        viewModel.messages.observe(this, {
            if (!viewModel.users.value.isNullOrEmpty())
                chatAdapter.swapData(it,viewModel.users.value!!)
        })
        viewModel.currentAndOtherUserAndMessages.observe(this,{
            handleIntent()
        })

        viewModel.otherUser.observe(this, Observer {
            supportActionBar!!.title = it.username
        })




    }

    fun handleKeyboardAnimation(){
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

    fun handleIntent(){
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput!= null){
            val inputString = remoteInput.getCharSequence(Constants.KEY_TEXT_REPLY).toString()
            viewModel.sendMessage(inputString)
            viewModel.updateNotification()
        }
    }
}