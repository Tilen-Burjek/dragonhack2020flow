package com.covidchain.app.ble

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

var SERVICE_STRING = "786b5388-1a7a-4505-a782-b48ec7897e39"
var INPUT_STRING = "535b3a5d-4a1e-4939-bc2c-e76d38fbe1a4"
val SERVICE_UUID = UUID.fromString(SERVICE_STRING)
val INPUT_UUID = UUID.fromString(INPUT_STRING)

class GattCallback(
    private val c: Continuation<String>,
    private val publicKey: String
): BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if(gatt == null) return
        if(newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices()
        }
        val characteristic = BluetoothGattCharacteristic(
            INPUT_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PROPERTY_WRITE
        )
        characteristic.setValue(publicKey)
        gatt.writeCharacteristic(characteristic)
    }
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if(gatt == null) return
        val characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(INPUT_UUID)
        c.resume(characteristic.getStringValue(0))
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
            co.resume(listOf())
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            if(results != null) co.resume(results)
            else co.resume(listOf())
        }
    }

    private suspend fun scan(): List<ScanResult> {
        GlobalScope.launch {
            delay(1000L)
            bleScanner.stopScan(scanCallback)
            co.resume(scanResults)
        }
        return suspendCoroutine { continuation ->
            co = continuation
            bleScanner.startScan(listOf(filter), scanSettings, scanCallback) // listOf(filter)
        }

    }

    suspend fun scanKeys(publicKey: String): List<String> {
        val tasks: List<Deferred<String>> = scan().map {
            GlobalScope.async { exchangeKeys(it.device.address, publicKey) }
        }
        return tasks.awaitAll()

    }

    private suspend fun exchangeKeys(address: String, publicKey: String): String {
        val device = bluetoothAdapter.getRemoteDevice(address)
        return suspendCoroutine { continuation ->
            device.connectGatt(context, false, GattCallback(continuation, publicKey))
        }
    }
}

class BleProvider(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    private val bleScanner = bluetoothAdapter.bluetoothLeScanner

    suspend fun scan(publicKey: String):List<String> {
        return BleScan(bleScanner, bluetoothAdapter, context).scanKeys(publicKey)
    }
}