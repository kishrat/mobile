package com.example.eventtracker.ui.create

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.eventtracker.R
import com.example.eventtracker.databinding.DialogLocationPickerBinding
import com.example.eventtracker.util.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationPickerDialog(
    private val onLocationSelected: (LatLng, String) -> Unit
) : DialogFragment(), OnMapReadyCallback {

    private var _binding: DialogLocationPickerBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var selectedLocation: LatLng? = null
    private var marker: com.google.android.gms.maps.model.Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLocationPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupClickListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapPicker) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set initial location to Ontario Tech campus
        val campusLocation = LatLng(
            Constants.CAMPUS_LATITUDE,
            Constants.CAMPUS_LONGITUDE
        )

        map.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(campusLocation, 16f))

            uiSettings.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
            }

            // Set map click listener
            setOnMapClickListener { location ->
                placeMarker(location)
            }
        }

        // Place initial marker at campus
        placeMarker(campusLocation)
    }

    private fun placeMarker(location: LatLng) {
        // Remove existing marker
        marker?.remove()

        // Add new marker
        marker = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title("Event Location")
                .draggable(true)
        )

        selectedLocation = location

        // Update venue name input
        binding.etVenueName.setText(
            "Location: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}"
        )

        // Move camera to marker
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(location))
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val location = selectedLocation
            val venueName = binding.etVenueName.text.toString().trim()

            if (location != null && venueName.isNotEmpty()) {
                onLocationSelected(location, venueName)
                dismiss()
            } else {
                binding.etVenueName.error = "Please enter venue name"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}