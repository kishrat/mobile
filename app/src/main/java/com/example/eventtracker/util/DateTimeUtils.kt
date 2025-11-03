package com.example.eventtracker.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
    private val timeFormat = SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun parseDateTime(dateString: String, timeString: String): Long {
        return try {
            val combinedString = "$dateString $timeString"
            val parser = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault())
            parser.parse(combinedString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun isUpcoming(timestamp: Long): Boolean {
        return timestamp > System.currentTimeMillis()
    }

    fun isPast(timestamp: Long): Boolean {
        return timestamp < System.currentTimeMillis()
    }

    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val eventDate = Calendar.getInstance().apply { timeInMillis = timestamp }

        return today.get(Calendar.YEAR) == eventDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == eventDate.get(Calendar.DAY_OF_YEAR)
    }
}