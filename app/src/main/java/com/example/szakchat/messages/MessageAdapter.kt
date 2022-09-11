package com.example.szakchat.messages

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.szakchat.R
import com.example.szakchat.databinding.MessageIncomingBinding
import com.example.szakchat.databinding.MessageOwnedBinding

class MessageAdapter(private val listener: Listener)
    : ListAdapter<Message, MessageHolder>(itemCallback){


    companion object {
        object itemCallback : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
        const val INCOMING = 1
        const val OWNED = 0
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item.incoming)
            INCOMING
        else
            OWNED
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        return when(viewType) {
            OWNED -> OwnedMessageHolder(
                MessageOwnedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
            else -> IncomingMessageHolder(
                MessageIncomingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener {

    }
}