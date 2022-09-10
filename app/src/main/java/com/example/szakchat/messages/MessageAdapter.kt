package com.example.szakchat.messages

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.szakchat.R
import com.example.szakchat.database.RoomMessage
import com.example.szakchat.databinding.MessageBinding

class MessageAdapter(private val listener: Listener)
    : ListAdapter<Message, MessageAdapter.MessageHolder>(itemCallback){


    companion object {
        object itemCallback : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class MessageHolder(private val binding: MessageBinding)
        : RecyclerView.ViewHolder(binding.root) {

        var message: Message? = null

        fun bind(message: Message){
            this.message = message
            binding.msgView.text = message.text
            if(message.incoming)
                binding.root.gravity = Gravity.START
            else
                if(message.sent == false)
                    binding.msgView.setBackgroundResource(R.drawable.not_sent_message_ground)
        }

        init {
            binding.root.setOnClickListener {
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        return MessageHolder(
            MessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener {

    }
}