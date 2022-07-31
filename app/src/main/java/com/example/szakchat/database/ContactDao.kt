package com.example.szakchat.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ContactDao {

    @Insert
    fun insertContact(contact: RoomContact)

    @Query("SELECT * from contacts")
    fun getContacts(): LiveData<List<RoomContact>>

    @Update
    fun updateContact(contact: RoomContact)

    @Delete
    fun deleteContact(contact: RoomContact)

}