package com.bsoftwares.chatexample.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.ui.chat.ChatActivity
import com.bsoftwares.chatexample.MainActivity
import com.bsoftwares.chatexample.database.LatestMessageDB
import com.bsoftwares.chatexample.ui.newMessage.NewMessageActivity
import com.bsoftwares.chatexample.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_last_message.view.*

class HomeActivity : AppCompatActivity(), HomeActivityAdapter.Interaction {

    private val viewModel: HomeViewModel by lazy {
        val activity = requireNotNull(this)
        ViewModelProvider(
            this,
            HomeViewModel.Factory(activity.application)
        ).get(HomeViewModel::class.java)
    }

    lateinit var homeAdapter : HomeActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel.loadLatestMessages()
        viewModel.fetchUsers()
        viewModel.updateToken()

        rv_latest_messages.apply {
            homeAdapter =  HomeActivityAdapter(this@HomeActivity)
            homeAdapter.setHasStableIds(true)
            adapter = homeAdapter
        }

        fab_newMessage.setOnClickListener {
            startActivity(Intent(this@HomeActivity, NewMessageActivity::class.java))
        }

        /*viewModel.latestMessages.observe(this, Observer {
            homeAdapter.swapData(it as List<LatestMessageDB>)
        })*/

        viewModel.picturesAndMessages.observe(this, Observer { (pictures , messages) ->
            if (pictures.isNotEmpty() && messages.isNotEmpty())
                homeAdapter.swapData(messages,pictures)
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
                startActivity(Intent(this, MainActivity::class.java))
                //Navigation.findNavController(fab_newMessage).navigate(R.id.mainActivity)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(position: Int, item: LatestMessageDB, toUid: String) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(Constants.USER_KEY,toUid)
        })
    }

    override fun onResume() {
        super.onResume()

    }

}