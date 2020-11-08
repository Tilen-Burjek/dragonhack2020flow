package com.covidchain.app.db

import com.covidchain.app.DateProvider

class ContactRepository(private val databaseProvider: DatabaseProvider) {
    private val dao = databaseProvider.contactDb().contactDao()

    suspend fun insert(key: String) {
        purge()
        dao.insert(Contact(0, key, DateProvider.now()))
    }

    suspend fun all(): List<String> {
        return dao.all(DateProvider.timedOut()).map { it.key }
    }

    suspend fun latest(key: String): Long? {
        return dao.latest(key)?.timestamp
    }

    suspend fun purge() {
        dao.purge(DateProvider.timedOut())
    }
}