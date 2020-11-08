package com.covidchain.app

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.covidchain.app.ble.ScanReceiver
import com.covidchain.app.ble.util.isLocationPermissionGranted
import com.covidchain.app.ble.util.requestLocationPermission
import com.covidchain.app.ble.util.showError
import com.covidchain.app.ble.util.showSnackbarShort
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import kotlinx.android.synthetic.main.activity_example1a.*

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2


class MainActivity : AppCompatActivity() {
    private lateinit var callbackIntent: PendingIntent
    private var hasClickedScan = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        if (!bleProvider.bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }
}