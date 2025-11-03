package com.example.eventtracker.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.eventtracker.R
import com.example.eventtracker.databinding.FragmentAccountBinding
import com.example.eventtracker.util.*
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnMyEvents.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_myEventsFragment)
        }

        binding.btnCreateEvent.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_createEventFragment)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.accountState.collect { state ->
                when (state) {
                    is AccountState.Loading -> {
                        binding.progressBar.visible()
                        binding.profileCard.gone()
                    }
                    is AccountState.Success -> {
                        binding.progressBar.gone()
                        binding.profileCard.visible()
                        displayUserProfile(state)
                    }
                    is AccountState.LoggedOut -> {
                        navigateToLogin()
                    }
                    is AccountState.Error -> {
                        binding.progressBar.gone()
                        showErrorSnackbar(state.message)
                    }
                }
            }
        }
    }

    private fun displayUserProfile(state: AccountState.Success) {
        val user = state.user

        binding.apply {
            // User info
            tvDisplayName.text = user.displayName
            tvEmail.text = user.email

            // Load avatar if available
            if (!user.avatarUrl.isNullOrEmpty()) {
                imgAvatar.loadUrl(user.avatarUrl)
            } else {
                // Set default avatar with user initials
                imgAvatar.setImageResource(R.drawable.ic_account)
            }

            // Show/hide Create Event button based on organizer status
            if (user.isOrganizer) {
                btnCreateEvent.visible()
                tvOrganizerBadge.visible()
            } else {
                btnCreateEvent.gone()
                tvOrganizerBadge.gone()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_accountFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}