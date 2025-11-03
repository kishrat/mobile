package com.example.eventtracker.data.repository

import android.net.Uri
import com.example.eventtracker.util.Constants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()

    // Upload image
    suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference
                .child(Constants.STORAGE_IMAGES)
                .child(filename)

            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload video
    suspend fun uploadVideo(uri: Uri): Result<String> {
        return try {
            val filename = "${UUID.randomUUID()}.mp4"
            val ref = storage.reference
                .child(Constants.STORAGE_VIDEOS)
                .child(filename)

            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete file by URL
    suspend fun deleteFile(url: String): Result<Unit> {
        return try {
            val ref = storage.getReferenceFromUrl(url)
            ref.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}