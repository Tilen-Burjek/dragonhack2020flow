package com.covidchain.app.ble

import java.util.*


/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
object SampleGattAttributes {
    private val attributes: HashMap<Any?, Any?> = HashMap<Any?, Any?>()
    var UUID_COVID_CHAIN_KEY = "786b5388-1a7a-4505-a782-b48ec7897e39"
    var CLIENT_CHARACTERISTIC_CONFIG = "535b3a5d-4a1e-4939-bc2c-e76d38fbe1a4"
    fun lookup(uuid: String?, defaultName: String): String {
        val name = attributes[uuid]
        return name as String? ?: defaultName
    }

    init {
        // Sample Services.
        attributes[CLIENT_CHARACTERISTIC_CONFIG] = "Key Service"
        // Sample Characteristics.
        attributes[UUID_COVID_CHAIN_KEY] = "Bitcoin Address"
    }
}