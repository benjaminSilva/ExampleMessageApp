package com.bsoftwares.chatexample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class NewChatAdapter() : RecyclerView.Adapter<NewChatAdapter.NewChatViewHolder>() {

    var data = listOf<JogadorModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewChatViewHolder {
        return NewChatViewHolder.from(
            parent
        )
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: NewChatViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class NewChatViewHolder private constructor (val binding : ItemJogadorBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item : JogadorModel
        ){
            binding.jogador = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): NewChatViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemJogadorBinding.inflate(layoutInflater, parent, false)
                return NewChatViewHolder(
                    binding
                )
            }
        }
    }
}