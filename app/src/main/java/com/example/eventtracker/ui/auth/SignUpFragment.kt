package com.example.eventtracker.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.eventtracker.R
import com.example.eventtracker.databinding.FragmentSignupBinding
import com.example.eventtracker.util.*
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val displayName = binding.etDisplayName.text.toString().trim()
            val isOrganizer = binding.cbOrganizer.isChecked

            if (validateInput(email, password, displayName)) {
                viewModel.signUp(email, password, displayName, isOrganizer)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validateInput(email: String, password: String, displayName: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            showErrorSnackbar(getString(R.string.error_empty_fields))
            return false
        }

        if (!email.isValidEmail()) {
            showErrorSnackbar(getString(R.string.error_invalid_email))
            return false
        }

        if (!password.isValidPassword()) {
            showErrorSnackbar(getString(R.string.error_weak_password))
            return false
        }

        return true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signUpState.collect { state ->
                when (state) {
                    is SignUpState.Idle -> {
                        binding.progressBar.gone()
                        binding.btnSignup.isEnabled = true
                    }
                    is SignUpState.Loading -> {
                        binding.progressBar.visible()
                        binding.btnSignup.isEnabled = false
                    }
                    is SignUpState.Success -> {
                        binding.progressBar.gone()
                        showSuccessSnackbar("Account created successfully!")
                        findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
                    }
                    is SignUpState.Error -> {
                        binding.progressBar.gone()
                        binding.btnSignup.isEnabled = true
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