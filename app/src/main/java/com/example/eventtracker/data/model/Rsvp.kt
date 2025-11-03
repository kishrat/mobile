package com.example.eventtracker.data.model

data class Rsvp(
    val rsvpId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "rsvpId" to rsvpId,
            "eventId" to eventId,
            "userId" to userId,
            "userName" to userName,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Rsvp {
            return Rsvp(
                rsvpId = map["rsvpId"] as? String ?: "",
                eventId = map["eventId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}