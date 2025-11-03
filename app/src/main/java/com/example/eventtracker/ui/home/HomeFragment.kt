package com.example.eventtracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventtracker.R
import com.example.eventtracker.databinding.FragmentHomeBinding
import com.example.eventtracker.util.gone
import com.example.eventtracker.util.showErrorSnackbar
import com.example.eventtracker.util.visible
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = EventsAdapter { event ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToEventDetailFragment(event.eventId)
            findNavController().navigate(action)
        }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.searchEvents(text.toString())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventsState.collect { state ->
                when (state) {
                    is EventsState.Loading -> {
                        binding.progressBar.visible()
                        binding.tvEmpty.gone()
                    }
                    is EventsState.Success -> {
                        binding.progressBar.gone()
                        if (state.events.isEmpty()) {
                            binding.tvEmpty.visible()
                            binding.rvEvents.gone()
                        } else {
                            binding.tvEmpty.gone()
                            binding.rvEvents.visible()
                            adapter.submitList(state.events)
                        }
                    }
                    is EventsState.Error -> {
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