package com.benyaminrasouli.phoenixprotocol.core.util

/**
 * Parses a comma-separated string of indices (e.g. "0,2,5") into a list of ints.
 * Blank entries are filtered out.
 */
fun String.parseCsvIndices(): List<Int> =
    split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }

/**
 * Converts total seconds into a human-readable focus time string like "2h 15m" or "45m".
 */
fun Int.toFocusTimeText(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun Long.toFocusTimeText(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
