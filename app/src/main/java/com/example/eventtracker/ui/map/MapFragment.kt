package com.example.eventtracker.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.eventtracker.R
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.databinding.FragmentMapBinding
import com.example.eventtracker.util.Constants
import com.example.eventtracker.util.DateTimeUtils
import com.example.eventtracker.util.gone
import com.example.eventtracker.util.showErrorSnackbar
import com.example.eventtracker.util.visible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private val markers = mutableMapOf<Marker, Event>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        observeViewModel()
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configure map
        map.apply {
            uiSettings.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = false
                isMapToolbarEnabled = true
            }

            // Set initial camera position to Ontario Tech campus
            val campusLocation = LatLng(
                Constants.CAMPUS_LATITUDE,
                Constants.CAMPUS_LONGITUDE
            )
            moveCamera(CameraUpdateFactory.newLatLngZoom(campusLocation, 15f))

            // Set marker click listener
            setOnMarkerClickListener { marker ->
                val event = markers[marker]
                event?.let { showEventInfo(it) }
                true
            }

            // Set info window click listener
            setOnInfoWindowClickListener { marker ->
                val event = markers[marker]
                event?.let { navigateToEventDetail(it) }
            }
        }

        // Load events after map is ready
        viewModel.loadEvents()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mapState.collect { state ->
                when (state) {
                    is MapState.Loading -> {
                        binding.progressBar.visible()
                    }
                    is MapState.Success -> {
                        binding.progressBar.gone()
                        displayEventsOnMap(state.events)
                    }
                    is MapState.Error -> {
                        binding.progressBar.gone()
                        showErrorSnackbar(state.message)
                    }
                }
            }
        }
    }

    private fun displayEventsOnMap(events: List<Event>) {
        googleMap?.let { map ->
            // Clear existing markers
            map.clear()
            markers.clear()

            // Add markers for each event
            events.forEach { event ->
                val location = LatLng(event.latitude, event.longitude)

                val markerOptions = MarkerOptions()
                    .position(location)
                    .title(event.title)
                    .snippet(buildMarkerSnippet(event))

                // Use different marker colors based on event capacity
                when {
                    event.isFull -> markerOptions.icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                    event.currentAttendees > event.maxAttendees / 2 -> markerOptions.icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                    else -> markerOptions.icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                val marker = map.addMarker(markerOptions)
                marker?.let {
                    markers[it] = event
                }
            }

            // Show count
            binding.tvEventCount.text = getString(
                R.string.events_on_map,
                events.size
            )
            binding.tvEventCount.visible()
        }
    }

    private fun buildMarkerSnippet(event: Event): String {
        return buildString {
            append(DateTimeUtils.formatDateTime(event.dateTime))
            append(" • ")
            append(event.venueName)
            append("\n")
            append("${event.currentAttendees}/${event.maxAttendees} attendees")
        }
    }

    private fun showEventInfo(event: Event) {
        // Info window is shown automatically by Google Maps
        // You can add custom logic here if needed
    }

    private fun navigateToEventDetail(event: Event) {
        val action = MapFragmentDirections
            .actionMapFragmentToEventDetailFragment(event.eventId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}