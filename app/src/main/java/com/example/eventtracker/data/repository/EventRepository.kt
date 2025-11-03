package com.example.eventtracker.data.repository

import com.example.eventtracker.data.model.Event
import com.example.eventtracker.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection(Constants.COLLECTION_EVENTS)

    // Get all events as Flow (real-time updates)
    fun getAllEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .orderBy("dateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Event.fromMap(it) }
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }

    // Get upcoming events
    fun getUpcomingEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .whereGreaterThan("dateTime", System.currentTimeMillis())
            .orderBy("dateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Event.fromMap(it) }
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }

    // Get event by ID
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = eventsCollection.document(eventId).get().await()
            doc.data?.let { Event.fromMap(it) }
        } catch (e: Exception) {
            null
        }
    }

    // Create event
    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val eventId = UUID.randomUUID().toString()
            val eventWithId = event.copy(eventId = eventId)

            eventsCollection.document(eventId)
                .set(eventWithId.toMap())
                .await()

            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update event
    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            eventsCollection.document(event.eventId)
                .set(event.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete event
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Search events
    fun searchEvents(query: String): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .orderBy("title")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Event.fromMap(it) }
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }

    // Get events by organizer
    fun getEventsByOrganizer(organizerId: String): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .whereEqualTo("organizerId", organizerId)
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Event.fromMap(it) }
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }

    // Increment attendee count
    suspend fun incrementAttendees(eventId: String): Result<Unit> {
        return try {
            val eventRef = eventsCollection.document(eventId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = (snapshot.getLong("currentAttendees") ?: 0).toInt()
                transaction.update(eventRef, "currentAttendees", currentCount + 1)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Decrement attendee count
    suspend fun decrementAttendees(eventId: String): Result<Unit> {
        return try {
            val eventRef = eventsCollection.document(eventId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = (snapshot.getLong("currentAttendees") ?: 0).toInt()
                if (currentCount > 0) {
                    transaction.update(eventRef, "currentAttendees", currentCount - 1)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}