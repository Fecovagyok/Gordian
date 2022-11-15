package hu.bme.gordian.hu.mcold.gordian.messages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.szakchat.R
import com.example.szakchat.databinding.MessageIncomingBinding
import com.example.szakchat.databinding.MessageOwnedBinding

abstract class MessageHolder(view: View,) : RecyclerView.ViewHolder(view) {
    protected var message: Message? = null
    open fun bind(message: Message){
        this.message = message
    }
}

class IncomingMessageHolder(private val binding: MessageIncomingBinding)
    : MessageHolder(binding.root) {


    override fun bind(message: Message){
        binding.msgView.text = message.text
    }
}

class OwnedMessageHolder(private val binding: MessageOwnedBinding, private val adapter: MessageAdapter)
    : MessageHolder(binding.root) {

    init {
        binding.msgView.setOnLongClickListener {
            adapter.longClickedItem = message
            false
        }
    }

    override fun bind(message: Message) {
        super.bind(message)
        binding.msgView.text = message.text
        if(!message.sent) {
            binding.msgView.setBackgroundResource(R.drawable.not_sent_message_ground)
        }
        else
            binding.msgView.setBackgroundResource(R.drawable.sent_message_border)
    }
}
