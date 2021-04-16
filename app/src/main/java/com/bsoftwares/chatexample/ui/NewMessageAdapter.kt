package com.bsoftwares.chatexample.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.model.ChatUser
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_new_chat.view.*
import java.lang.Exception

class NewMessageAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ChatUser, NewMessageAdapter.NewMessageViewModel>(ChatUserDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NewMessageViewModel(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_new_chat, parent, false), interaction
    )

    override fun onBindViewHolder(holder: NewMessageViewModel, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<ChatUser>) {
        submitList(data.toMutableList())
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

        fun bind(item: ChatUser) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemClicked(adapterPosition,item)
            }
            itemView.txt_userName.text = item.username
            Picasso.get().load(item.profileImageUrl).into(itemView.civ_userImage,object : Callback {
                override fun onSuccess() {
                    itemView.shimmer_test.hideShimmer()
                }

                override fun onError(e: Exception?) {
                }
            })
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int,item: ChatUser)
    }

    private class ChatUserDC : DiffUtil.ItemCallback<ChatUser>() {
        override fun areItemsTheSame(
            oldItem: ChatUser,
            newItem: ChatUser
        ): Boolean {

           return true
        }

        override fun areContentsTheSame(
            oldItem: ChatUser,
            newItem: ChatUser
        ): Boolean {
            return true
        }
    }
}
