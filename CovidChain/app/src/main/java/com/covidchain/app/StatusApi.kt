package com.covidchain.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class StatusApi {
    private suspend fun request(url: String, method: String, body: String?): String {
        val result = withContext(Dispatchers.IO) {
            val conn: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = method
            body?.let {
                conn.outputStream.write(body.toByteArray())
            }
            conn.connect()

            val inputStream = conn.inputStream
            inputStream
        }
        return result.toString()
    }

    private suspend fun get(url: String): String {
        return request(url, "GET", null)
    }

    private suspend fun post(url: String, body: String): String {
        return request(url, "POST", body)
    }

    suspend fun publish(publicKey: String, privateKey: String, sick: Boolean) {
        //TODO
    }

    suspend fun receive(publicKey: String) {
        //TODO
    }
}