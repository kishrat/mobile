package com.example.eventtracker.ui.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.eventtracker.R
import com.example.eventtracker.databinding.FragmentCreateEventBinding
import com.example.eventtracker.util.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateEventViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var selectedLocation: LatLng? = null
    private var selectedDateTime: Calendar = Calendar.getInstance()

    // Image picker from gallery
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.imgPreview.setImageURI(it)
            binding.imgPreview.visible()
            viewModel.uploadImage(it)
        }
    }

    // Camera launcher
    private var photoUri: Uri? = null
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let {
                selectedImageUri = it
                binding.imgPreview.setImageURI(it)
                binding.imgPreview.visible()
                viewModel.uploadImage(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAudienceDropdown()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAudienceDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            Constants.TARGET_AUDIENCES
        )
        binding.actvAudience.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnCamera.setOnClickListener {
            launchCamera()
        }

        binding.btnGallery.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.etDateTime.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnPickLocation.setOnClickListener {
            showLocationPicker()
        }

        binding.btnCreate.setOnClickListener {
            validateAndCreateEvent()
        }
    }

    private fun launchCamera() {
        // Create temporary file for photo
        val photoFile = File(
            requireContext().cacheDir,
            "event_photo_${System.currentTimeMillis()}.jpg"
        )

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        takePhoto.launch(photoUri)
    }

    private fun showDateTimePicker() {
        val now = Calendar.getInstance()

        // Date picker
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDateTime.set(year, month, day)

                // Time picker
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                        selectedDateTime.set(Calendar.MINUTE, minute)

                        // Display selected date/time
                        binding.etDateTime.setText(
                            DateTimeUtils.formatDateTime(selectedDateTime.timeInMillis)
                        )
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun showLocationPicker() {
        val dialog = LocationPickerDialog { location, venueName ->
            selectedLocation = location
            binding.tvSelectedLocation.text = venueName
            binding.tvSelectedLocation.setTextColor(
                resources.getColor(R.color.text_primary, null)
            )
        }
        dialog.show(parentFragmentManager, "LocationPicker")
    }

    private fun validateAndCreateEvent() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val maxAttendees = binding.etMaxAttendees.text.toString().trim()
        val audience = binding.actvAudience.text.toString().trim()

        // Validation
        when {
            title.isEmpty() -> {
                showErrorSnackbar("Please enter event title")
                return
            }
            description.isEmpty() -> {
                showErrorSnackbar("Please enter event description")
                return
            }
            binding.etDateTime.text.toString().isEmpty() -> {
                showErrorSnackbar("Please select date and time")
                return
            }
            maxAttendees.isEmpty() -> {
                showErrorSnackbar("Please enter max attendees")
                return
            }
            selectedLocation == null -> {
                showErrorSnackbar("Please select event location")
                return
            }
        }

        // Create event
        viewModel.createEvent(
            title = title,
            description = description,
            dateTime = selectedDateTime.timeInMillis,
            location = selectedLocation!!,
            venueName = binding.tvSelectedLocation.text.toString(),
            maxAttendees = maxAttendees.toInt(),
            targetAudience = audience.ifEmpty { null }
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createEventState.collect { state ->
                when (state) {
                    is CreateEventState.Idle -> {
                        binding.progressBar.gone()
                        binding.btnCreate.isEnabled = true
                    }
                    is CreateEventState.UploadingImage -> {
                        binding.progressBar.visible()
                        binding.btnCreate.isEnabled = false
                    }
                    is CreateEventState.CreatingEvent -> {
                        binding.progressBar.visible()
                        binding.btnCreate.isEnabled = false
                    }
                    is CreateEventState.Success -> {
                        binding.progressBar.gone()
                        showSuccessSnackbar(getString(R.string.event_created))
                        findNavController().navigateUp()
                    }
                    is CreateEventState.Error -> {
                        binding.progressBar.gone()
                        binding.btnCreate.isEnabled = true
                        showErrorSnackbar(state.message)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}