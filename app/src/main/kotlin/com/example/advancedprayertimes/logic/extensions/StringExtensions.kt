package com.example.advancedprayertimes.logic.extensions

fun String?.isNotNullOrBlank(): Boolean {
    return !this.isNullOrBlank()
}

fun String?.isNotNullOrEmpty(): Boolean {
    return !this.isNullOrEmpty()
}