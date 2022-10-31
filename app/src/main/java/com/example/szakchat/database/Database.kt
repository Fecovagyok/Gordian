package com.example.szakchat.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 10,
    entities = [RoomContact::class, RoomMessage::class]
)
abstract class Database : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao
}