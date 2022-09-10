package com.example.szakchat.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages " +
            "WHERE contact_id = :id"
    )
    fun getMessages(id: Long): LiveData<List<RoomMessage>>

    @Insert
    fun insert(message: RoomMessage)

    @Insert
    fun insertAll(messages: List<RoomMessage>)

    @Update
    fun update(message: RoomMessage)

    @Query("UPDATE messages SET sent=1 WHERE id=:id")
    fun setSent(id: Long)
}