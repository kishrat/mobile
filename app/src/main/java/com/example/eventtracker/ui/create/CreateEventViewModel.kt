package com.example.eventtracker.ui.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.data.repository.AuthRepository
import com.example.eventtracker.data.repository.EventRepository
import com.example.eventtracker.data.repository.StorageRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateEventState {
    object Idle : CreateEventState()
    object UploadingImage : CreateEventState()
    object CreatingEvent : CreateEventState()
    object Success : CreateEventState()
    data class Error(val message: String) : CreateEventState()
}

class CreateEventViewModel : ViewModel() {

    private val eventRepository = EventRepository()
    private val storageRepository = StorageRepository()
    private val authRepository = AuthRepository()

    private val _createEventState = MutableStateFlow<CreateEventState>(CreateEventState.Idle)
    val createEventState: StateFlow<CreateEventState> = _createEventState.asStateFlow()

    private var uploadedImageUrl: String? = null

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _createEventState.value = CreateEventState.UploadingImage

            val result = storageRepository.uploadImage(uri)

            if (result.isSuccess) {
                uploadedImageUrl = result.getOrNull()
                _createEventState.value = CreateEventState.Idle
            } else {
                _createEventState.value = CreateEventState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to upload image"
                )
            }
        }
    }

    fun createEvent(
        title: String,
        description: String,
        dateTime: Long,
        location: LatLng,
        venueName: String,
        maxAttendees: Int,
        targetAudience: String?
    ) {
        viewModelScope.launch {
            _createEventState.value = CreateEventState.CreatingEvent

            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _createEventState.value = CreateEventState.Error("Please login to create events")
                    return@launch
                }

                if (!user.isOrganizer) {
                    _createEventState.value = CreateEventState.Error("Only organizers can create events")
                    return@launch
                }

                val event = Event(
                    title = title,
                    description = description,
                    dateTime = dateTime,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    venueName = venueName,
                    maxAttendees = maxAttendees,
                    currentAttendees = 0,
                    imageUrl = uploadedImageUrl,
                    organizerId = user.userId,
                    organizerName = user.displayName,
                    targetAudience = targetAudience
                )

                val result = eventRepository.createEvent(event)

                if (result.isSuccess) {
                    _createEventState.value = CreateEventState.Success
                } else {
                    _createEventState.value = CreateEventState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to create event"
                    )
                }
            } catch (e: Exception) {
                _createEventState.value = CreateEventState.Error(
                    e.message ?: "Failed to create event"
                )
            }
        }
    }
}