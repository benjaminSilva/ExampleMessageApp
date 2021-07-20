package com.bsoftwares.chatexample.ui.newMessage

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bsoftwares.chatexample.database.getDataBase
import com.bsoftwares.chatexample.model.ChatUser
import com.bsoftwares.chatexample.model.toUserDB
import com.bsoftwares.chatexample.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*

class NewMessageViewModel(context: Application) : AndroidViewModel(context) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val repository = Repository(context)

    val users = repository.users

    fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<ChatUser>()
                snapshot.children.forEach {
                    val user = it.getValue(ChatUser::class.java)
                    //val uid = FirebaseAuth.getInstance().uid
                    //if (user != null && user.uid != uid)
                    if (user != null) {
                        users.add(user)
                    }
                }
                viewModelScope.launch {
                        repository.loadUsers(users.toUserDB(context = getApplication()))
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewMessageViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewMessageViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }


}