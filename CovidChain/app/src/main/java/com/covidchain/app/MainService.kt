package com.covidchain.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.covidchain.app.db.ContactRepository
import com.covidchain.app.db.DatabaseProvider
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient

class MainService: Service() {
    val bleProvider = com.covidchain.app.ble.BleProvider(context = applicationContext)

    lateinit var contactRepository: ContactRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bleProvider.rxBleClient = RxBleClient.create(this)
        RxBleClient.updateLogOptions(
            LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        )
        contactRepository = ContactRepository(DatabaseProvider(this))
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun onResume() {
        if (!bleProvider.bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

}