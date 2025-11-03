package com.example.eventtracker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.data.repository.AuthRepository
import com.example.eventtracker.data.repository.EventRepository
import com.example.eventtracker.data.repository.RsvpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EventDetailState {
    object Loading : EventDetailState()
    data class Success(
        val event: Event,
        val isRsvped: Boolean
    ) : EventDetailState()
    data class Error(val message: String) : EventDetailState()
}

sealed class RsvpState {
    object Idle : RsvpState()
    object Loading : RsvpState()
    data class Success(val message: String) : RsvpState()
    data class Error(val message: String) : RsvpState()
}

class EventDetailViewModel : ViewModel() {

    private val eventRepository = EventRepository()
    private val rsvpRepository = RsvpRepository()
    private val authRepository = AuthRepository()

    private val _eventState = MutableStateFlow<EventDetailState>(EventDetailState.Loading)
    val eventState: StateFlow<EventDetailState> = _eventState.asStateFlow()

    private val _rsvpState = MutableStateFlow<RsvpState>(RsvpState.Idle)
    val rsvpState: StateFlow<RsvpState> = _rsvpState.asStateFlow()

    private var currentEventId: String = ""

    fun loadEvent(eventId: String) {
        currentEventId = eventId
        viewModelScope.launch {
            _eventState.value = EventDetailState.Loading

            try {
                val event = eventRepository.getEventById(eventId)
                if (event == null) {
                    _eventState.value = EventDetailState.Error("Event not found")
                    return@launch
                }

                val userId = authRepository.currentUserId ?: ""
                val isRsvped = rsvpRepository.isUserRsvped(eventId, userId)

                _eventState.value = EventDetailState.Success(event, isRsvped)
            } catch (e: Exception) {
                _eventState.value = EventDetailState.Error(
                    e.message ?: "Failed to load event"
                )
            }
        }
    }

    fun toggleRsvp() {
        val currentState = _eventState.value
        if (currentState !is EventDetailState.Success) return

        val userId = authRepository.currentUserId
        if (userId == null) {
            _rsvpState.value = RsvpState.Error("Please login to RSVP")
            return
        }

        viewModelScope.launch {
            _rsvpState.value = RsvpState.Loading

            try {
                val user = authRepository.getCurrentUser()
                val userName = user?.displayName ?: "Anonymous"

                val result = if (currentState.isRsvped) {
                    // Cancel RSVP
                    rsvpRepository.cancelRsvp(currentEventId, userId)
                } else {
                    // Create RSVP
                    rsvpRepository.rsvpToEvent(currentEventId, userId, userName)
                }

                if (result.isSuccess) {
                    val message = if (currentState.isRsvped) {
                        "RSVP cancelled"
                    } else {
                        "RSVP successful!"
                    }
                    _rsvpState.value = RsvpState.Success(message)

                    // Reload event to update RSVP status
                    loadEvent(currentEventId)
                } else {
                    _rsvpState.value = RsvpState.Error(
                        result.exceptionOrNull()?.message ?: "RSVP failed"
                    )
                }
            } catch (e: Exception) {
                _rsvpState.value = RsvpState.Error(
                    e.message ?: "RSVP failed"
                )
            } finally {
                // Reset state after 2 seconds
                kotlinx.coroutines.delay(2000)
                _rsvpState.value = RsvpState.Idle
            }
        }
    }
}