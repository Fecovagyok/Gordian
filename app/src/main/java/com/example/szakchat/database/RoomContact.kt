package com.example.szakchat.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.szakchat.contacts.Contact

@Entity(tableName = "contacts")
data class RoomContact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val secret: String,
    val name: String
)
