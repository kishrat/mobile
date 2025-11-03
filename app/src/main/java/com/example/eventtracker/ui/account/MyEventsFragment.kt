package com.example.eventtracker.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventtracker.databinding.FragmentMyEventsBinding
import com.example.eventtracker.util.gone
import com.example.eventtracker.util.showErrorSnackbar
import com.example.eventtracker.util.visible
import kotlinx.coroutines.launch

class MyEventsFragment : Fragment() {

    private var _binding: FragmentMyEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyEventsViewModel by viewModels()
    private lateinit var adapter: MyEventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MyEventsAdapter(
            onEventClick = { event ->
                val action = MyEventsFragmentDirections
                    .actionMyEventsFragmentToEventDetailFragment(event.eventId)
                findNavController().navigate(action)
            },
            onCancelRsvp = { event ->
                viewModel.cancelRsvp(event.eventId)
            }
        )

        binding.rvMyEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyEventsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myEventsState.collect { state ->
                when (state) {
                    is MyEventsState.Loading -> {
                        binding.progressBar.visible()
                        binding.tvEmpty.gone()
                    }
                    is MyEventsState.Success -> {
                        binding.progressBar.gone()
                        if (state.events.isEmpty()) {
                            binding.tvEmpty.visible()
                            binding.rvMyEvents.gone()
                        } else {
                            binding.tvEmpty.gone()
                            binding.rvMyEvents.visible()
                            adapter.submitList(state.events)
                        }
                    }
                    is MyEventsState.Error -> {
                        binding.progressBar.gone()
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