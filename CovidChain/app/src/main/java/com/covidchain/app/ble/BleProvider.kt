package com.covidchain.app.ble

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.covidchain.app.MainActivity
import kotlinx.coroutines.*
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.coroutineContext
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
        }
    }
}

class BleScan(
    private val bleScanner: BluetoothLeScanner,
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) {
    private val scanResults: MutableList<ScanResult> = mutableListOf()
    private lateinit var co: Continuation<List<ScanResult>>
    private val filter = ScanFilter.Builder().setServiceUuid(
        ParcelUuid.fromString(UUID_COVID_CHAIN_KEY.toString())
    ).build()

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) {
                scanResults[indexQuery] = result
            } else {
                scanResults.add(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            if(results != null) co.resume(results)
            else co.resume(listOf())
        }
    }

    private suspend fun scan(): List<ScanResult> {
        return suspendCoroutine {
            bleScanner.startScan(listOf(filter), scanSettings, scanCallback)
        }
    }

    suspend fun scanKeys(): List<String> {
        val tasks: List<Deferred<String>> = scan().map {
            GlobalScope.async<String> { readCharacteristic(it.device.address) }
        }
        return tasks.awaitAll()

    }

    private suspend fun readCharacteristic(address: String): String {
        val device = bluetoothAdapter.getRemoteDevice(address)
        return suspendCoroutine { continuation ->
            device.connectGatt(context, false, GattCallback(continuation))
        }
    }
}

class BleProvider(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    private val bleScanner = bluetoothAdapter.bluetoothLeScanner

    suspend fun scan():List<String> {
        return BleScan(bleScanner, bluetoothAdapter, context).scanKeys()
    }
}