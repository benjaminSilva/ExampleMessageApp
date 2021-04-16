package com.bsoftwares.chatexample.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.model.ChatUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.layout_new_chat.view.*
import java.lang.Exception

class NewMessageActivity : AppCompatActivity(), NewMessageAdapter.Interaction {

    companion object {
        val USER_KEY = "USER_KEY"
    }

    lateinit var newMessageAdapter : NewMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        initRecyclerView()

        fetchUsers()
    }

    private fun initRecyclerView() {
        rv_newMessages.apply {
            newMessageAdapter = NewMessageAdapter(this@NewMessageActivity)
            adapter = newMessageAdapter
        }
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<ChatUser>()
                snapshot.children.forEach {
                    val user = it.getValue(ChatUser::class.java)
                    val uid = HomeActivity.currentUser!!.uid
                    if (user!=null&& user.uid != uid) {
                        users.add(user)
                    }
                }
                newMessageAdapter.swapData(users)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onItemClicked(position: Int, item: ChatUser) {
        startActivity(Intent(this@NewMessageActivity,ChatActivity::class.java).apply {
            putExtra(USER_KEY,item)
        })
        finish()
    }
}