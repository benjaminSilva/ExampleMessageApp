package com.bsoftwares.chatexample.ui.home

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.database.LatestMessageDB
import com.bsoftwares.chatexample.database.UsersDB
import kotlinx.android.synthetic.main.layout_last_message.view.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivityAdapter(private val interaction: Interaction? = null) :
    ListAdapter<LatestMessageDB, HomeActivityAdapter.ChatUserHomeViewModel>(ChatUserDC()) {

    var pictures : List<UsersDB>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatUserHomeViewModel(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_last_message, parent, false), interaction
    )

    override fun onBindViewHolder(holder: ChatUserHomeViewModel, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<LatestMessageDB>,profilePictures : List<UsersDB>) {
        submitList(data.toMutableList())
        pictures = profilePictures
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

        @SuppressLint("SimpleDateFormat")
        fun bind(item: LatestMessageDB?) = with(itemView) {
            itemView.txt_message_latest.text = item!!.text
            val sdf = SimpleDateFormat("EEEE")
            val date = Date(item.timeStamp)
            itemView.setOnClickListener {
                interaction?.onItemClicked(adapterPosition, item,item.otherId)
            }
            val user = pictures!!.find { it.userUID == item.otherId }
            //val bitmap = user!!.profilePhoto
            itemView.civ_user_profile_image_latest_message.setImageURI(Uri.parse(user!!.profilePhoto))
            itemView.txt_when_message_was_sent.text = sdf.format(date)
            itemView.txt_userName_latest.text = item.fromUserName
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int, item: LatestMessageDB, toUid: String)
    }

    private class ChatUserDC : DiffUtil.ItemCallback<LatestMessageDB>() {
        override fun areItemsTheSame(
            oldItem: LatestMessageDB,
            newItem: LatestMessageDB
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: LatestMessageDB,
            newItem: LatestMessageDB
        ): Boolean {
            return oldItem == newItem
        }
    }
}
