package com.covidchain.app.ble.util

import android.bluetooth.BluetoothGattCharacteristic

fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean = (properties and property) > 0