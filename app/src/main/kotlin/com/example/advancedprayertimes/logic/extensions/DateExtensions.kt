package com.example.advancedprayertimes.logic.extensions

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun String.parseToTime(format: String): LocalTime {

    val timeFormatter = DateTimeFormatter.ofPattern(format)
    return LocalTime.parse(this, timeFormatter)
}

fun String.parseToDateTime(format: String): LocalDateTime {

    val timeFormatter = DateTimeFormatter.ofPattern(format)
    return LocalDateTime.parse(this, timeFormatter)
}

fun LocalDateTime.toStringByFormat(format: String): String {
    return DateTimeFormatter.ofPattern(format).format(this)
}