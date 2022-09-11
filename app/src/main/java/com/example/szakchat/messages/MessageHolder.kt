package com.example.szakchat.messages

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.szakchat.R
import com.example.szakchat.databinding.MessageIncomingBinding
import com.example.szakchat.databinding.MessageOwnedBinding

abstract class MessageHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(message: Message)
}

class IncomingMessageHolder(private val binding: MessageIncomingBinding)
    : MessageHolder(binding.root) {

    override fun bind(message: Message){
        binding.msgView.text = message.text
        Log.d(
            "FECO", "Text: ${
                message.text
            } Sent: ${message.sent} incoming: ${message.incoming}"
        )
    }
}

class OwnedMessageHolder(private val binding: MessageOwnedBinding)
    : MessageHolder(binding.root) {

    override fun bind(message: Message) {
        binding.msgView.text = message.text
        if(!message.sent) {
            binding.msgView.setBackgroundResource(R.drawable.not_sent_message_ground)
        }
        else
            binding.msgView.setBackgroundResource(R.drawable.sent_message_border)
    }
}
