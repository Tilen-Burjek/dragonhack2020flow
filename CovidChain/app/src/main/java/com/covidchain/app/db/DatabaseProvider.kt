package com.covidchain.app.db

import android.content.Context
import androidx.room.Room

class DatabaseProvider(private val context: Context) {
    public fun contactDb(): ContactDb {
        return Room.databaseBuilder(
            context,
            ContactDb::class.java,
            "contact-db"
        ).build()
    }
}