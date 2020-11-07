package com.covidchain.app.ble

import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

class BleProvider(private val context: Context) {
    private val TAG = BluetoothLeService::class.java.simpleName
    private val bluetoothManager = context.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    suspend fun readCharacteristic(address: String): String {
        val device = bluetoothAdapter.getRemoteDevice(address)
        return suspendCoroutine { continuation ->
            device.connectGatt(context, false, GattCallback(continuation))
        }
    }
}