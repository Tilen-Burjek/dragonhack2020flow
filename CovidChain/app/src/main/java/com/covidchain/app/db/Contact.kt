package com.covidchain.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact (
    @PrimaryKey val id: Int,
    val key: String,
    val timestamp: Long
)