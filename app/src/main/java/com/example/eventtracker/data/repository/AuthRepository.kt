package com.example.eventtracker.data.repository

import com.example.eventtracker.data.model.User
import com.example.eventtracker.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Get current user ID
    val currentUserId: String?
        get() = auth.currentUser?.uid

    // Login with email and password
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            val user = userDoc.data?.let { User.fromMap(it) }
                ?: throw Exception("User data not found")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign up with email, password, and display name
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        isOrganizer: Boolean
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val user = User(
                userId = userId,
                username = email.substringBefore("@"),
                email = email,
                displayName = displayName,
                isOrganizer = isOrganizer
            )

            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(user.toMap())
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get current user from Firestore
    suspend fun getCurrentUser(): User? {
        return try {
            val userId = currentUserId ?: return null
            val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            userDoc.data?.let { User.fromMap(it) }
        } catch (e: Exception) {
            null
        }
    }

    // Logout
    fun logout() {
        auth.signOut()
    }

    // Check if user is logged in
    val isLoggedIn: Boolean
        get() = auth.currentUser != null
}