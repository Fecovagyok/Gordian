package com.example.szakchat.network

import android.util.Log
import com.example.szakchat.contacts.Contact
import com.example.szakchat.identity.UserID
import com.example.szakchat.messages.Message
import com.example.szakchat.security.GcmMessage
import com.example.szakchat.security.MySecurityProtocol

// Assuming it was sent, and we need the src as contact
fun convertToMessages(contacts: List<Contact>, list: List<GcmMessage>, security: MySecurityProtocol): List<Message> {
    val mappedContacts = HashMap<UserID, Contact>(contacts.size)
    contacts.forEach {
        mappedContacts[it.uniqueId!!] = it
    }
    val messages = mutableListOf<Message>()
    list.forEach {
        val contact = mappedContacts[it.src]
        contact?: run {
            Log.w("FECO", "Received message was not among contacts")
            return@forEach
        }
        val text = security.decode(contact.keys!!.receiver, it)
        val message = Message(
            id = 0,
            text = text,
            contact = contact,
            incoming = true,
            date = it.date,
            owner = it.dst,
        )
        messages.add(message)
    }
    return messages
}