package com.example.eventtracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class EventsState {
    object Loading : EventsState()
    data class Success(val events: List<Event>) : EventsState()
    data class Error(val message: String) : EventsState()
}

class HomeViewModel : ViewModel() {

    private val eventRepository = EventRepository()

    private val _eventsState = MutableStateFlow<EventsState>(EventsState.Loading)
    val eventsState: StateFlow<EventsState> = _eventsState.asStateFlow()

    private var allEvents = listOf<Event>()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            eventRepository.getUpcomingEvents()
                .catch { e ->
                    _eventsState.value = EventsState.Error(
                        e.message ?: "Failed to load events"
                    )
                }
                .collect { events ->
                    allEvents = events
                    _eventsState.value = EventsState.Success(events)
                }
        }
    }

    fun searchEvents(query: String) {
        if (query.isEmpty()) {
            _eventsState.value = EventsState.Success(allEvents)
            return
        }

        val filtered = allEvents.filter { event ->
            event.title.contains(query, ignoreCase = true) ||
                    event.description.contains(query, ignoreCase = true) ||
                    event.venueName.contains(query, ignoreCase = true)
        }

        _eventsState.value = EventsState.Success(filtered)
    }
}