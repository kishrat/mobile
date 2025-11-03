package com.example.eventtracker.data.model

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val isOrganizer: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Convert to Map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "displayName" to displayName,
            "avatarUrl" to avatarUrl,
            "isOrganizer" to isOrganizer,
            "createdAt" to createdAt
        )
    }

    companion object {
        // Create from Firestore document
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                userId = map["userId"] as? String ?: "",
                username = map["username"] as? String ?: "",
                email = map["email"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                avatarUrl = map["avatarUrl"] as? String,
                isOrganizer = map["isOrganizer"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}