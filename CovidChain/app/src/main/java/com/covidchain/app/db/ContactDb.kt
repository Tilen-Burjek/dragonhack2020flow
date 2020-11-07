package com.covidchain.app.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Contact::class], version = 1)
abstract class ContactDb: RoomDatabase() {
    abstract fun contactDao(): ContactDao
}