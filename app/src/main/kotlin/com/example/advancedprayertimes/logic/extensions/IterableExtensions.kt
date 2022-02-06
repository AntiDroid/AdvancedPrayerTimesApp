package com.example.advancedprayertimes.logic.extensions

fun <T> Iterable<T>.notAll(predicate: (T) -> Boolean): Boolean {
    return !this.all(predicate)
}

fun <T> Iterable<T>.notContains(element: T): Boolean {
    return !this.contains(element)
}