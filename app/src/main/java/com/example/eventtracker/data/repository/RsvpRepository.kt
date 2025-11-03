package com.example.eventtracker.data.repository

import com.example.eventtracker.data.model.Event
import com.example.eventtracker.data.model.Rsvp
import com.example.eventtracker.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RsvpRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val rsvpsCollection = firestore.collection(Constants.COLLECTION_RSVPS)
    private val eventRepository = EventRepository()

    // RSVP to event
    suspend fun rsvpToEvent(eventId: String, userId: String, userName: String): Result<Unit> {
        return try {
            // Check if already RSVP'd
            val existing = rsvpsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("Already RSVP'd to this event"))
            }

            // Check if event is full
            val event = eventRepository.getEventById(eventId)
            if (event != null && event.isFull) {
                return Result.failure(Exception("Event is full"))
            }

            // Create RSVP
            val rsvpId = UUID.randomUUID().toString()
            val rsvp = Rsvp(
                rsvpId = rsvpId,
                eventId = eventId,
                userId = userId,
                userName = userName
            )

            rsvpsCollection.document(rsvpId).set(rsvp.toMap()).await()

            // Increment attendee count
            eventRepository.incrementAttendees(eventId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cancel RSVP
    suspend fun cancelRsvp(eventId: String, userId: String): Result<Unit> {
        return try {
            val rsvps = rsvpsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (rsvps.isEmpty) {
                return Result.failure(Exception("RSVP not found"))
            }

            // Delete RSVP
            rsvps.documents.forEach { doc ->
                rsvpsCollection.document(doc.id).delete().await()
            }

            // Decrement attendee count
            eventRepository.decrementAttendees(eventId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if user has RSVP'd
    suspend fun isUserRsvped(eventId: String, userId: String): Boolean {
        return try {
            val rsvps = rsvpsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            !rsvps.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Get user's RSVPs as Flow
    fun getUserRsvps(userId: String): Flow<List<Event>> = callbackFlow {
        val listener = rsvpsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val eventIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.getString("eventId")
                } ?: emptyList()

                // Fetch events for these RSVPs
                if (eventIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    firestore.collection(Constants.COLLECTION_EVENTS)
                        .whereIn("eventId", eventIds)
                        .get()
                        .addOnSuccessListener { eventSnapshot ->
                            val events = eventSnapshot.documents.mapNotNull { doc ->
                                doc.data?.let { Event.fromMap(it) }
                            }
                            trySend(events)
                        }
                }
            }

        awaitClose { listener.remove() }
    }

    // Get attendees for an event
    suspend fun getEventAttendees(eventId: String): List<Rsvp> {
        return try {
            val snapshot = rsvpsCollection
                .whereEqualTo("eventId", eventId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { Rsvp.fromMap(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}