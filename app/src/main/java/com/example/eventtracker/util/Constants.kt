package com.example.eventtracker.util

object Constants {
    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_EVENTS = "events"
    const val COLLECTION_RSVPS = "rsvps"

    // Storage Paths
    const val STORAGE_IMAGES = "images"
    const val STORAGE_VIDEOS = "videos"

    // Limits
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_VIDEO_SIZE_MB = 50

    // Default Campus Location (Ontario Tech University)
    const val CAMPUS_LATITUDE = 43.9454
    const val CAMPUS_LONGITUDE = -78.8966

    // Target Audiences
    val TARGET_AUDIENCES = listOf(
        "All Students",
        "1st Year",
        "2nd Year",
        "3rd Year",
        "4th Year",
        "Graduate Students",
        "Engineering",
        "Computer Science",
        "Business",
        "Science",
        "Education",
        "Health Sciences"
    )

    // Date Format
    const val DATE_FORMAT = "MMM dd, yyyy"
    const val TIME_FORMAT = "h:mm a"
    const val DATETIME_FORMAT = "MMM dd, yyyy • h:mm a"
}