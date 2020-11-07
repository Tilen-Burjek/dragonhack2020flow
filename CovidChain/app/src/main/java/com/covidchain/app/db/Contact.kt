package com.covidchain.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val key: String,
    val timestamp: Long
)