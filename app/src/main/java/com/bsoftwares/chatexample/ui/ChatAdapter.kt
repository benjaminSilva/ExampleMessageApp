package com.bsoftwares.chatexample.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_chat_left_message.view.*
import kotlinx.android.synthetic.main.layout_chat_right_message.view.*

class ChatAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            0 -> {
                ChatRightViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_chat_right_message, parent, false), interaction
                )
            }
            else -> {
                ChatLeftViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_chat_left_message, parent, false), interaction
                )
            }
        }
    }

    val uid = FirebaseAuth.getInstance().uid

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).fromID == uid)
            0
        else
            1
        /*val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                left_right = if (chatMessage.toID== FirebaseAuth.getInstance().uid) 0 else 1
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })*/
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        //holder.bind(getItem(position))
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

    var datas = listOf<ChatMessage>()

    fun swapData(data: List<ChatMessage>) {
        datas = data
        submitList(data.toMutableList())
    }

    inner class ChatRightViewHolder(
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
            itemView.txt_message_right.text = item.text
            Picasso.get().load(HomeActivity.currentUser!!.profileImageUrl).into(civ_userImage_chat_message_right)
        }
    }

    override fun getItemId(position: Int): Long {
        return datas[position].position.toLong()
    }


    inner class ChatLeftViewHolder(
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
            itemView.txt_message_left.text = item.text
            Picasso.get().load(ChatActivity.toUser!!.profileImageUrl).into(civ_userImage_chat_message_left)
        }
    }



    interface Interaction {

    }

    private class ChatMessageDC : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(
            oldItem: ChatMessage,
            newItem: ChatMessage
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ChatMessage,
            newItem: ChatMessage
        ): Boolean {
            return oldItem.equals(newItem)
        }
    }
}
