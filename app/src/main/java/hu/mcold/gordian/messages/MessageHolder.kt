package hu.mcold.gordian.messages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import hu.mcold.gordian.R
import hu.mcold.gordian.databinding.MessageIncomingBinding
import hu.mcold.gordian.databinding.MessageOwnedBinding

abstract class MessageHolder(view: View,) : RecyclerView.ViewHolder(view) {
    protected var message: hu.mcold.gordian.messages.Message? = null
    open fun bind(message: hu.mcold.gordian.messages.Message){
        this.message = message
    }
}

class IncomingMessageHolder(private val binding: MessageIncomingBinding)
    : hu.mcold.gordian.messages.MessageHolder(binding.root) {


    override fun bind(message: hu.mcold.gordian.messages.Message){
        binding.msgView.text = message.text
    }
}

class OwnedMessageHolder(private val binding: MessageOwnedBinding, private val adapter: hu.mcold.gordian.messages.MessageAdapter)
    : hu.mcold.gordian.messages.MessageHolder(binding.root) {

    init {
        binding.msgView.setOnLongClickListener {
            adapter.longClickedItem = message
            false
        }
    }

    override fun bind(message: hu.mcold.gordian.messages.Message) {
        super.bind(message)
        binding.msgView.text = message.text
        if(!message.sent) {
            binding.msgView.setBackgroundResource(R.drawable.not_sent_message_ground)
        }
        else
            binding.msgView.setBackgroundResource(R.drawable.sent_message_border)
    }
}
