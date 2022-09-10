package com.example.szakchat.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.szakchat.contacts.Contact

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["uniqueId"], unique = true)]
)
data class RoomContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uniqueId: String,
    val name: String
)
