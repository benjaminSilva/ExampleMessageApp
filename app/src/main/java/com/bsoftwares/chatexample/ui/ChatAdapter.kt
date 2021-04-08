package com.bsoftwares.chatexample.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ChatAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatMessageDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_right_something, parent, false), interaction
    )

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<ChatMessage>) {
        submitList(data.toMutableList())
    }

    inner class ChatViewHolder(
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

        fun bind(item: ChatMessage) = with(itemView) {
            // TODO: Bind the data with View
        }
    }

    interface Interaction {

    }

    private class ChatMessageDC : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(
            oldItem: ChatMessage,
            newItem: ChatMessage
        ): Boolean {
            TODO(
                "not implemented"
            )
        }

        override fun areContentsTheSame(
            oldItem: ChatMessage,
            newItem: ChatMessage
        ): Boolean {
            TODO(
                "not implemented"
            )
        }
    }
}
