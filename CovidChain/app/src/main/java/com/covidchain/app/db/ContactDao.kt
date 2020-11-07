package com.covidchain.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact WHERE `key` = :key ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(key: String): Contact?
    @Insert(onConflict = IGNORE)
    suspend fun insert(contact: Contact): Long
    @Query("DELETE FROM contact WHERE timestamp < :before")
    suspend fun purge(before: Long)
}