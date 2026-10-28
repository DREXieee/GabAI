package com.gabai.presentation.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// These helpers predate core-library desugaring (enabled since for the booking
// calendar's java.time), hence SimpleDateFormat. Server timestamps are always
// UTC ISO-8601 ("...Z"); parsing pins UTC while display formatting uses the
// device's default timezone/locale to render local time.
private val isoParserWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun parseIsoInstant(iso: String): Date? {
    val trimmed = iso.removeSuffix("Z")
    return runCatching { isoParserWithMillis.parse(trimmed) }.getOrNull()
        ?: runCatching { isoParser.parse(trimmed) }.getOrNull()
}

// The device-local calendar day a server timestamp falls on.
fun localDateOf(iso: String): LocalDate? {
    val date = parseIsoInstant(iso) ?: return null
    return Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate()
}

fun formatFriendlyDateTime(iso: String): String {
    val date = parseIsoInstant(iso) ?: return iso
    return SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault()).format(date)
}

fun formatFriendlyDate(iso: String): String {
    val date = parseIsoInstant(iso) ?: return iso
    return SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(date)
}

fun formatFriendlyTime(iso: String): String {
    val date = parseIsoInstant(iso) ?: return iso
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}
