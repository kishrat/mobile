package com.example.eventtracker.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventtracker.R
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.databinding.ItemEventBinding
import com.example.eventtracker.util.DateTimeUtils
import com.example.eventtracker.util.loadUrl
import com.example.eventtracker.util.setVisibility

class EventsAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, EventsAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                // Load event image
                imgThumbnail.loadUrl(event.imageUrl)

                // Event details
                tvTitle.text = event.title
                tvDateTime.text = DateTimeUtils.formatDateTime(event.dateTime)
                tvVenue.text = event.venueName
                tvCapacity.text = root.context.getString(
                    R.string.capacity_format,
                    event.currentAttendees,
                    event.maxAttendees
                )

                // Media badges
                chipImage.setVisibility(event.hasImage)
                chipVideo.setVisibility(event.hasVideo)

                // Click listener
                root.setOnClickListener {
                    onEventClick(event)
                }
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}