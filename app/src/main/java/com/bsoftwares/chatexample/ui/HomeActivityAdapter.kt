package com.bsoftwares.chatexample.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class HomeActivityAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ChatUser, HomeActivityAdapter.ChatUserHomeViewModel>(ChatUserDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatUserHomeViewModel(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_latest_message, parent, false), interaction
    )

    override fun onBindViewHolder(holder: ChatUserHomeViewModel, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<ChatUser>) {
        submitList(data.toMutableList())
    }

    inner class ChatUserHomeViewModel(
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
            // TODO: Bind the data with View
        }
    }

    interface Interaction {

    }

    private class ChatUserDC : DiffUtil.ItemCallback<ChatUser>() {
        override fun areItemsTheSame(
            oldItem: ChatUser,
            newItem: ChatUser
        ): Boolean {
            TODO(
                "not implemented"
            )
        }

        override fun areContentsTheSame(
            oldItem: ChatUser,
            newItem: ChatUser
        ): Boolean {
            TODO(
                "not implemented"
            )
        }
    }
}
