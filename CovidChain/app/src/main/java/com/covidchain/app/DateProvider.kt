package com.covidchain.app

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

private val TIME_OUT = Duration.ofDays(14)

class DateProvider {
    companion object {
        fun now(): Long {
            return Instant.now().toEpochMilli()
        }

        fun timedOut(): Long {
            return Instant.now().minus(TIME_OUT).toEpochMilli()
        }

        fun format(timestamp: Long, locale: Locale): String {
            val instant = Instant.ofEpochMilli(timestamp)
            val formatter: DateTimeFormatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(locale)
                    .withZone(ZoneId.systemDefault())
            return formatter.format(instant)
        }
    }
}