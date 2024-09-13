package hu.mcold.gordian.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import hu.mcold.gordian.databinding.MessageIncomingBinding
import hu.mcold.gordian.databinding.MessageOwnedBinding

class MessageAdapter(private val listener: Listener)
    : ListAdapter<Message, hu.mcold.gordian.messages.MessageHolder>(itemCallback){


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

    var subscribed: (() -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item.incoming)
            INCOMING
        else
            OWNED
    }

    var longClickedItem: Message? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): hu.mcold.gordian.messages.MessageHolder {
        return when(viewType) {
            OWNED -> hu.mcold.gordian.messages.OwnedMessageHolder(
                MessageOwnedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ),
                this
            )
            else -> hu.mcold.gordian.messages.IncomingMessageHolder(
                MessageIncomingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: hu.mcold.gordian.messages.MessageHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Message>,
        currentList: MutableList<Message>
    ) {
        subscribed?.invoke()
        super.onCurrentListChanged(previousList, currentList)
    }

    interface Listener {

    }
}