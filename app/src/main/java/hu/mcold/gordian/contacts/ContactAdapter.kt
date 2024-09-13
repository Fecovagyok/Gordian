package hu.mcold.gordian.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mcold.gordian.databinding.RowContactBinding

class ContactAdapter(private val listener: ContactListener)
    : ListAdapter<Contact, ContactAdapter.ContactHolder>(itemCallback){

    companion object {
        object itemCallback : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface ContactListener {
        fun onItemClick(contact: Contact)
        fun onLongClick()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return ContactHolder(
            RowContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class ContactHolder(private val binding: RowContactBinding)
        : RecyclerView.ViewHolder(binding.root) {

        var contact: Contact? = null

        fun bind(contact: Contact){
            this.contact = contact
            binding.nameContact.text = contact.name
        }

        init {
            binding.root.setOnClickListener {
                contact?.let {
                    listener.onItemClick(it)
                }
            }
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Contact>,
        currentList: MutableList<Contact>
    ) {
        super.onCurrentListChanged(previousList, currentList)
    }
}