package hu.bme.hit.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages " +
            "WHERE contact_id = :id"
    )
    fun getMessages(id: Long): LiveData<List<RoomMessage>>

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