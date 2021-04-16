package com.bsoftwares.chatexample.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.model.ChatMessage
import com.bsoftwares.chatexample.model.ChatUser
import com.bsoftwares.chatexample.viewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_last_message.view.*

class HomeActivity : AppCompatActivity(),HomeActivityAdapter.Interaction {

    companion object{
        var currentUser : ChatUser? = null
    }

    private val viewModel by viewModels<UserViewModel>()

    lateinit var homeAdapter : HomeActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fetchCurrentUser()
        listenForLatestMessages()

        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications")

        rv_latest_messages.apply {
            homeAdapter =  HomeActivityAdapter(this@HomeActivity)
            homeAdapter.setHasStableIds(true)
            adapter = homeAdapter
        }

        fab_newMessage.setOnClickListener {
            startActivity(Intent(this@HomeActivity,NewMessageActivity::class.java))
        }

    }

    private fun listenForLatestMessages() {
        val chats = mutableListOf<ChatMessage>()
        val fromID = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latestMessage/$fromID")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chats.clear()
                snapshot.children.forEach {
                    val chat = it.getValue(ChatMessage::class.java)
                    if (chat!=null) {
                        chats.add(chat)
                    }
                }
                homeAdapter.swapData(chats)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(ChatUser::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this,MainActivity::class.java))
                //Navigation.findNavController(fab_newMessage).navigate(R.id.mainActivity)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(position: Int, item: ChatMessage, user: ChatUser) {
        startActivity(Intent(this,ChatActivity::class.java).apply {
            putExtra(NewMessageActivity.USER_KEY,user)
        })
    }

}