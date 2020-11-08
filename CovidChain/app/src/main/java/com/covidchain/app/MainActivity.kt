package com.covidchain.app

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusButton: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusButton = findViewById(R.id.statusButton)

        status = loadStatus()
    }

    private var status: Int = HEALTHY
        set(value) {
            field = value
            updateButton()
            saveStatus(value)
        }

    private fun loadStatus(): Int {
        val pref = getSharedPreferences("CovidChain", Context.MODE_PRIVATE)
        return pref.getInt("Status", HEALTHY)
    }

    private fun saveStatus(newStatus: Int) {
        getSharedPreferences("CovidChain", Context.MODE_PRIVATE)
            .edit().putInt("Status", newStatus)
            .apply()
    }

    fun toggleStatus(view: View) {
        status = 1 - status
    }

    private fun updateButton() {
        if (status == SICK) {
            statusButton.text = getString(R.string.sick)
        }
        else if (status == HEALTHY) {
            statusButton.text = getString(R.string.healthy)
        }
    }
}