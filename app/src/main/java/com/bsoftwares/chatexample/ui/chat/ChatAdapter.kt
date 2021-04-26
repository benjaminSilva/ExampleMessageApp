package com.bsoftwares.chatexample.ui.chat

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.database.ChatMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.layout_chat_left_message.view.*
import kotlinx.android.synthetic.main.layout_chat_right_message.view.*

class ChatAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ChatMessageDB, RecyclerView.ViewHolder>(ChatMessageDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            0 -> {
                ChatRightViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_chat_right_message, parent, false)
                )
            }
            else -> {
                ChatLeftViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_chat_left_message, parent, false)
                )
            }
        }
    }

    val uid = FirebaseAuth.getInstance().uid

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == uid)
            0
        else
            1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        when (holder.itemViewType){
            0 -> {
                val viewHolder = holder as ChatRightViewHolder
                viewHolder.bind(getItem(position))
            }
            else ->{
                val viewHolder = holder as ChatLeftViewHolder
                viewHolder.bind(getItem(position))
            }
        }
    }

    var datas = listOf<ChatMessageDB>()
    var localUsers = listOf<UsersDB>()

    fun swapData(data: List<ChatMessageDB>, users : List<UsersDB>) {
        datas = data
        localUsers = users
        submitList(data.toMutableList())
    }

    inner class ChatRightViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition == RecyclerView.NO_POSITION) return
        }

        fun bind(item: ChatMessageDB) = with(itemView) {
            itemView.txt_message_right.text = item.text
            val user =  localUsers.find { it.userUID == uid }
            val bitmap = user?.profilePhoto
            civ_userImage_chat_message_right.setImageBitmap(bitmap)
        }
    }

    override fun getItemId(position: Int): Long {
        return datas[position].position.toLong()
    }


    inner class ChatLeftViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return }

        fun bind(item: ChatMessageDB) = with(itemView) {
            itemView.txt_message_left.text = item.text
            val user =  localUsers.find { it.userUID == item.senderId }
            val bitmap = user?.profilePhoto
            civ_userImage_chat_message_left.setImageBitmap(bitmap)
        }
    }



    interface Interaction {

    }

    private class ChatMessageDC : DiffUtil.ItemCallback<ChatMessageDB>() {
        override fun areItemsTheSame(
            oldItem: ChatMessageDB,
            newItem: ChatMessageDB
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ChatMessageDB,
            newItem: ChatMessageDB
        ): Boolean {
            return oldItem == newItem
        }
    }
}
