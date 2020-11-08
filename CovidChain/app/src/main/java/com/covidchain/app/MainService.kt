package com.covidchain.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.covidchain.app.db.ContactRepository
import com.covidchain.app.db.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val HEALTHY = 0
const val SICK = 1

class MainService: Service() {
    lateinit var contactRepository: ContactRepository
    lateinit var notifier: Notifier
    lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        notifier = Notifier(this)
        contactRepository = ContactRepository(DatabaseProvider(this))
        handler = Handler(Looper.getMainLooper())
    }

    private val runnable = Runnable() {
        fun run() {
            GlobalScope.launch {
                //ble.scan
                val list = listOf<String>("1", "2")
                list.forEach {
                    contactRepository.insert(it)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}