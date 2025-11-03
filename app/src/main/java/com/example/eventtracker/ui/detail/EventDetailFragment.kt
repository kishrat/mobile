package com.example.eventtracker.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.eventtracker.R
import com.example.eventtracker.databinding.FragmentEventDetailBinding
import com.example.eventtracker.util.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class EventDetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventDetailViewModel by viewModels()
    private val args: EventDetailFragmentArgs by navArgs()

    private var googleMap: GoogleMap? = null
    private var eventLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupClickListeners()
        observeViewModel()

        // Load event details
        viewModel.loadEvent(args.eventId)
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        eventLocation?.let { location ->
            showLocationOnMap(location)
        }
    }

    private fun showLocationOnMap(location: LatLng) {
        googleMap?.apply {
            val marker = MarkerOptions()
                .position(location)
                .title(viewModel.eventState.value.event?.venueName ?: "Event Location")

            addMarker(marker)
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    private fun setupClickListeners() {
        binding.btnRsvp.setOnClickListener {
            viewModel.toggleRsvp()
        }

        binding.btnNavigate.setOnClickListener {
            eventLocation?.let { location ->
                openGoogleMapsNavigation(location)
            }
        }

        binding.btnShare.setOnClickListener {
            shareEvent()
        }
    }

    private fun openGoogleMapsNavigation(location: LatLng) {
        val uri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web maps if Google Maps app not installed
            val webUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
            )
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun shareEvent() {
        val event = viewModel.eventState.value.event ?: return

        val shareText = """
            Check out this event: ${event.title}
            
            📅 ${DateTimeUtils.formatDateTime(event.dateTime)}
            📍 ${event.venueName}
            
            ${event.description}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, event.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(intent, "Share Event"))
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventState.collect { state ->
                when (state) {
                    is EventDetailState.Loading -> {
                        binding.progressBar.visible()
                        binding.contentGroup.gone()
                    }
                    is EventDetailState.Success -> {
                        binding.progressBar.gone()
                        binding.contentGroup.visible()
                        displayEventDetails(state)
                    }
                    is EventDetailState.Error -> {
                        binding.progressBar.gone()
                        showErrorSnackbar(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rsvpState.collect { state ->
                when (state) {
                    is RsvpState.Idle -> {
                        binding.btnRsvp.isEnabled = true
                    }
                    is RsvpState.Loading -> {
                        binding.btnRsvp.isEnabled = false
                    }
                    is RsvpState.Success -> {
                        binding.btnRsvp.isEnabled = true
                        showSuccessSnackbar(state.message)
                    }
                    is RsvpState.Error -> {
                        binding.btnRsvp.isEnabled = true
                        showErrorSnackbar(state.message)
                    }
                }
            }
        }
    }

    private fun displayEventDetails(state: EventDetailState.Success) {
        val event = state.event

        binding.apply {
            // Load cover image
            imgCover.loadUrl(event.imageUrl)

            // Event details
            tvTitle.text = event.title
            tvDateTime.text = DateTimeUtils.formatDateTime(event.dateTime)
            tvVenue.text = event.venueName
            tvOrganizer.text = "Hosted by ${event.organizerName}"
            tvCapacity.text = getString(
                R.string.capacity_format,
                event.currentAttendees,
                event.maxAttendees
            )
            tvDescription.text = event.description

            // Target audience (optional)
            if (!event.targetAudience.isNullOrEmpty()) {
                tvAudience.visible()
                tvAudience.text = "Target: ${event.targetAudience}"
            } else {
                tvAudience.gone()
            }

            // RSVP button state
            updateRsvpButton(state.isRsvped, event.isFull)

            // Show location on map
            eventLocation = LatLng(event.latitude, event.longitude)
            googleMap?.let { map ->
                showLocationOnMap(eventLocation!!)
            }
        }
    }

    private fun updateRsvpButton(isRsvped: Boolean, isFull: Boolean) {
        binding.btnRsvp.apply {
            when {
                isRsvped -> {
                    text = getString(R.string.cancel_rsvp)
                    setBackgroundColor(resources.getColor(R.color.error, null))
                }
                isFull -> {
                    text = getString(R.string.event_full)
                    isEnabled = false
                    setBackgroundColor(resources.getColor(R.color.text_secondary, null))
                }
                else -> {
                    text = getString(R.string.rsvp)
                    setBackgroundColor(resources.getColor(R.color.primary, null))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}