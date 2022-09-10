package com.example.szakchat

import android.app.Application
import androidx.room.Room
import com.example.szakchat.database.Database

class ChatApplication : Application() {
    companion object {
        lateinit var database: Database
            private set
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            "chat_data"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}