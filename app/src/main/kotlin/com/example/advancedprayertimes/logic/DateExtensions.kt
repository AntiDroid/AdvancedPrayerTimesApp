package com.example.advancedprayertimes.logic

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.parse(format: String): LocalDateTime {

    val timeFormatter = DateTimeFormatter.ofPattern(format)
    return LocalDateTime.parse(this, timeFormatter)
}