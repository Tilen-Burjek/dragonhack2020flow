package com.covidchain.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.covidchain.app.db.ContactRepository
import com.covidchain.app.db.DatabaseProvider

const val HEALTHY = 0
const val SICK = 1

class MainService: Service() {
    lateinit var contactRepository: ContactRepository
    lateinit var notifier: Notifier

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        contactRepository = ContactRepository(DatabaseProvider(this))
        notifier = Notifier(this)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}