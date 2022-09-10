package com.example.szakchat.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class RoomMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "contact_id")
    val contactId: Long,
    val text: String,
    val incoming: Boolean,
    val sent: Boolean?,
)
