package com.example.szakchat.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [RoomContact::class]
)
abstract class Database : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}