package com.covidchain.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.covidchain.app.db.ContactRepository
import com.covidchain.app.db.DatabaseProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val HEALTHY = 0
const val SICK = 1
const val SCAN_INTERVAL = 10 * 1000L // 10s
const val RECEIVE_INTERVAL = 10 * 1000L  // 10s for testing

class MainService: Service() {
    lateinit var contactRepository: ContactRepository
    lateinit var notifier: Notifier
    lateinit var handler: Handler
    lateinit var api: StatusApi
    private var status: Int = HEALTHY
    private var keyPair: KeyPair? = null

    override fun onCreate() {
        super.onCreate()
        notifier = Notifier(this)
        contactRepository = ContactRepository(DatabaseProvider(this))
        handler = Handler(Looper.getMainLooper())
        api = StatusApi()
        scheduleScan()
        GlobalScope.launch {
            keyPair = loadKeyPair()
            scheduleReceive()
        }
    }

    private suspend fun loadKeyPair(): KeyPair {
        val pref = getSharedPreferences("CovidChain", Context.MODE_PRIVATE)
        val publicKey = pref.getString("publicKey", null)
        val privateKey = pref.getString("privateKey", null)
        return if(publicKey != null && privateKey != null) {
            KeyPair(publicKey, privateKey)
        }
        else {
            val newKeyPair = api.getKeys()
            saveKeys(newKeyPair)
            newKeyPair
        }
    }

    private fun saveKeys(newKeyPair: KeyPair) {
        val edit = getSharedPreferences("CovidChain", Context.MODE_PRIVATE).edit()
        edit.putString("publicKey", newKeyPair.publicKey)
        edit.putString("privateKey", newKeyPair.privateKey)
        edit.apply()
    }

    private fun scheduleScan() {
        handler.postDelayed(scanTask, SCAN_INTERVAL)
    }

    private val scanTask = Runnable {
        fun run() {
            GlobalScope.launch {
                val list = listOf("1", "2") // = ble.scan()
                list.forEach {
                    contactRepository.insert(it)
                    if(status == SICK && keyPair != null) api.publish(keyPair!!, it)
                }
                scheduleScan()
            }
        }
    }

    private fun scheduleReceive() {
        handler.postDelayed(receiveTask, RECEIVE_INTERVAL)
    }

    private val receiveTask = Runnable {
        fun run() {
            GlobalScope.launch {
                val transactions = api.receive(keyPair!!.publicKey)
                transactions.forEach {
                    val timestamp = contactRepository.latest(it)
                    if(timestamp != null) {
                        notifier.notify(timestamp)
                    }
                }
                scheduleReceive()
            }
        }
    }

    private fun publishToAll() {
        GlobalScope.launch {
            contactRepository.all().forEach {
                api.publish(keyPair!!, it)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.extras?.let {
            status = it.getInt("Status")
            val update = it.getBoolean("Update")
            if(update && status == SICK) publishToAll()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}