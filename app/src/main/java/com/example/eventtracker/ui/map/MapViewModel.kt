package com.example.eventtracker.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class MapState {
    object Loading : MapState()
    data class Success(val events: List<Event>) : MapState()
    data class Error(val message: String) : MapState()
}

class MapViewModel : ViewModel() {

    private val eventRepository = EventRepository()

    private val _mapState = MutableStateFlow<MapState>(MapState.Loading)
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    fun loadEvents() {
        viewModelScope.launch {
            _mapState.value = MapState.Loading

            eventRepository.getUpcomingEvents()
                .catch { e ->
                    _mapState.value = MapState.Error(
                        e.message ?: "Failed to load events"
                    )
                }
                .collect { events ->
                    _mapState.value = MapState.Success(events)
                }
        }
    }
}