package com.example.eventtracker.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.data.repository.AuthRepository
import com.example.eventtracker.data.repository.RsvpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class MyEventsState {
    object Loading : MyEventsState()
    data class Success(val events: List<Event>) : MyEventsState()
    data class Error(val message: String) : MyEventsState()
}

class MyEventsViewModel : ViewModel() {

    private val rsvpRepository = RsvpRepository()
    private val authRepository = AuthRepository()

    private val _myEventsState = MutableStateFlow<MyEventsState>(MyEventsState.Loading)
    val myEventsState: StateFlow<MyEventsState> = _myEventsState.asStateFlow()

    init {
        loadMyEvents()
    }

    private fun loadMyEvents() {
        val userId = authRepository.currentUserId
        if (userId == null) {
            _myEventsState.value = MyEventsState.Error("Please login to view your events")
            return
        }

        viewModelScope.launch {
            rsvpRepository.getUserRsvps(userId)
                .catch { e ->
                    _myEventsState.value = MyEventsState.Error(
                        e.message ?: "Failed to load your events"
                    )
                }
                .collect { events ->
                    _myEventsState.value = MyEventsState.Success(events)
                }
        }
    }

    fun cancelRsvp(eventId: String) {
        val userId = authRepository.currentUserId ?: return

        viewModelScope.launch {
            val result = rsvpRepository.cancelRsvp(eventId, userId)
            if (result.isFailure) {
                _myEventsState.value = MyEventsState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to cancel RSVP"
                )
            }
            // Events will auto-update via Flow
        }
    }
}