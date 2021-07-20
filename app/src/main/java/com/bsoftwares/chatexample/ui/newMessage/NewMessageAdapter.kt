package com.bsoftwares.chatexample.ui.newMessage

import android.net.Uri
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.database.UsersDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import kotlinx.android.synthetic.main.layout_new_chat.view.*
import java.util.function.Predicate

class NewMessageAdapter(private val interaction: Interaction? = null) :
    ListAdapter<UsersDB, NewMessageAdapter.NewMessageViewModel>(ChatUserDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NewMessageViewModel(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_new_chat, parent, false), interaction
    )

    override fun onBindViewHolder(holder: NewMessageViewModel, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<UsersDB>) {
        var newList = data.toMutableList()
        newList.removeIf {
            it.userUID == FirebaseAuth.getInstance().uid
        }
        submitList(newList)
    }

    inner class NewMessageViewModel(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = getItem(adapterPosition)
        }

        fun bind(item: UsersDB) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemClicked(adapterPosition,item.userUID)
            }

            shimmer_test.hideShimmer()
            itemView.txt_userName.text = item.userName
            civ_userImage.setImageURI(Uri.parse(item.profilePhoto))
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int,uid: String)
    }

    private class ChatUserDC : DiffUtil.ItemCallback<UsersDB>() {
        override fun areItemsTheSame(
            oldItem: UsersDB,
            newItem: UsersDB
        ): Boolean {

           return true
        }

        override fun areContentsTheSame(
            oldItem: UsersDB,
            newItem: UsersDB
        ): Boolean {
            return true
        }
    }
}
