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
import com.example.eventtracker.databinding.FragmentLoginBinding
import com.example.eventtracker.util.*
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            showErrorSnackbar(getString(R.string.error_empty_fields))
            return false
        }

        if (!email.isValidEmail()) {
            showErrorSnackbar(getString(R.string.error_invalid_email))
            return false
        }

        return true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Idle -> {
                        binding.progressBar.gone()
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginState.Loading -> {
                        binding.progressBar.visible()
                        binding.btnLogin.isEnabled = false
                    }
                    is LoginState.Success -> {
                        binding.progressBar.gone()
                        showSuccessSnackbar("Welcome back!")
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    is LoginState.Error -> {
                        binding.progressBar.gone()
                        binding.btnLogin.isEnabled = true
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