package com.covidchain.app

import java.time.Duration
import java.time.Instant

class DateProvider {
    companion object {
        fun now(): Long {
            return Instant.now().toEpochMilli()
        }

        fun timedOut(): Long {
            return Instant.now().minus(TIME_OUT).toEpochMilli()
        }

        private val TIME_OUT = Duration.ofDays(14)
    }
}