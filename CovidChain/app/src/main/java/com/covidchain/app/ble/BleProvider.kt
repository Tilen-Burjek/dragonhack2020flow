package com.covidchain.app.ble

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.covidchain.app.MainActivity
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1

class GattCallback(private val c: Continuation<String>): BluetoothGattCallback() {
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            c.resume(characteristic.getStringValue(0))
            // write to DB?
        }
    }
}

class BleProvider(private val context: Context) {
    private val TAG = BluetoothLeService::class.java.simpleName
    private val bluetoothManager = context.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanResults = mutableListOf<ScanResult>()


    private val filter = ScanFilter.Builder().setServiceUuid(
        ParcelUuid.fromString(UUID_COVID_CHAIN_KEY.toString())
    ).build()

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i("ScanCallback", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
            }
        }
    }

    private fun startBleScan() {
        scanResults.clear()
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    suspend fun readCharacteristic(address: String): String {
        val device = bluetoothAdapter.getRemoteDevice(address)
        return suspendCoroutine { continuation ->
            device.connectGatt(context, false, GattCallback(continuation))
        }
    }


}