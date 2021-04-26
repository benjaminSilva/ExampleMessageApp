package com.bsoftwares.chatexample.ui.newMessage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.ui.chat.ChatActivity
import com.bsoftwares.chatexample.utils.Constants
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.layout_new_chat.view.*

class NewMessageActivity : AppCompatActivity(), NewMessageAdapter.Interaction {

    private val viewModel: NewMessageViewModel by lazy {
        val activity = requireNotNull(this)
        ViewModelProvider(
            this,
            NewMessageViewModel.Factory(activity.application)
        ).get(NewMessageViewModel::class.java)
    }

    lateinit var newMessageAdapter : NewMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        initRecyclerView()

        viewModel.fetchUsers()

        viewModel.users.observe(this, Observer {
            newMessageAdapter.swapData(it)
        })

    }

    private fun initRecyclerView() {
        rv_newMessages.apply {
            newMessageAdapter = NewMessageAdapter(this@NewMessageActivity)
            adapter = newMessageAdapter
        }
    }


    override fun onItemClicked(position: Int, uid: String) {
        startActivity(Intent(this@NewMessageActivity, ChatActivity::class.java).apply {
            putExtra(Constants.USER_KEY,uid)
        })
        finish()
    }
}