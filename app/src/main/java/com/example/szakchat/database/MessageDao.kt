package com.example.szakchat.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages " +
            "WHERE contact_id = :id"
    )
    fun getMessages(id: Long): LiveData<List<RoomMessage>>

    /*@Transaction
    @Query("SELECT * FROM contacts " +
            "WHERE id = :other and contacts.owner = :owner")
    fun getMessagesWithContact(owner: String, other: Long): LiveData<List<ContactWithMessage>>*/

    @Insert
    fun insert(message: RoomMessage): Long

    @Insert
    fun insertAll(messages: List<RoomMessage>)

    @Update
    fun update(message: RoomMessage): Int

    @Query("UPDATE messages SET sent=1 WHERE id=:id")
    fun setSent(id: Long)

    @Delete
    fun delete(msg: RoomMessage)
}