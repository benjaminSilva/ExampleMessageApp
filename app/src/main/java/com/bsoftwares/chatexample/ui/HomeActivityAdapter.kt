package com.bsoftwares.chatexample.ui

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.model.ChatMessage
import com.bsoftwares.chatexample.model.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_last_message.view.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivityAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ChatMessage, HomeActivityAdapter.ChatUserHomeViewModel>(ChatUserDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatUserHomeViewModel(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_last_message, parent, false), interaction
    )

    override fun onBindViewHolder(holder: ChatUserHomeViewModel, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<ChatMessage>) {
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

        fun bind(item: ChatMessage) = with(itemView) {
            itemView.txt_message_latest.text = item.text
            val sdf = SimpleDateFormat("EEEE")
            val date = Date(item.timeStamp)
            itemView.txt_when_message_was_sent.text = sdf.format(date)
            val chatPartnerId = if (item.fromID == FirebaseAuth.getInstance().uid) item.toID else item.fromID
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatPartnerUser = snapshot.getValue(ChatUser::class.java) ?: return
                    itemView.txt_userName_latest.text = chatPartnerUser.username
                    Picasso.get().load(chatPartnerUser.profileImageUrl)
                        .into(itemView.civ_user_profile_image_latest_message)
                    itemView.setOnClickListener {
                        interaction?.onItemClicked(adapterPosition, item, chatPartnerUser)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int, item: ChatMessage, user: ChatUser)
    }

    private class ChatUserDC : DiffUtil.ItemCallback<ChatMessage>() {
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
            return oldItem == newItem
        }
    }
}
