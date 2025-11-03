package com.example.eventtracker.data.model

data class Event(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val dateTime: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val venueName: String = "",
    val maxAttendees: Int = 0,
    val currentAttendees: Int = 0,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val organizerId: String = "",
    val organizerName: String = "",
    val targetAudience: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Check if event is full
    val isFull: Boolean
        get() = currentAttendees >= maxAttendees

    // Check if event has media
    val hasImage: Boolean
        get() = !imageUrl.isNullOrEmpty()

    val hasVideo: Boolean
        get() = !videoUrl.isNullOrEmpty()

    // Convert to Map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "eventId" to eventId,
            "title" to title,
            "description" to description,
            "dateTime" to dateTime,
            "latitude" to latitude,
            "longitude" to longitude,
            "venueName" to venueName,
            "maxAttendees" to maxAttendees,
            "currentAttendees" to currentAttendees,
            "imageUrl" to imageUrl,
            "videoUrl" to videoUrl,
            "organizerId" to organizerId,
            "organizerName" to organizerName,
            "targetAudience" to targetAudience,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Event {
            return Event(
                eventId = map["eventId"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                dateTime = map["dateTime"] as? Long ?: 0L,
                latitude = map["latitude"] as? Double ?: 0.0,
                longitude = map["longitude"] as? Double ?: 0.0,
                venueName = map["venueName"] as? String ?: "",
                maxAttendees = (map["maxAttendees"] as? Long)?.toInt() ?: 0,
                currentAttendees = (map["currentAttendees"] as? Long)?.toInt() ?: 0,
                imageUrl = map["imageUrl"] as? String,
                videoUrl = map["videoUrl"] as? String,
                organizerId = map["organizerId"] as? String ?: "",
                organizerName = map["organizerName"] as? String ?: "",
                targetAudience = map["targetAudience"] as? String,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}