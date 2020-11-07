package com.covidchain.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact WHERE `key` = :key ORDER BY timestamp DESC LIMIT 1")
    fun latest(key: String): Contact?
    @Insert
    fun insert(contact: Contact)
    @Query("DELETE FROM contact WHERE timestamp < :before")
    fun purge(before: Long)
}