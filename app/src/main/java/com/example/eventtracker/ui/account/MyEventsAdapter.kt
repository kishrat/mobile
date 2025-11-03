package com.example.eventtracker.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventtracker.R
import com.example.eventtracker.data.model.Event
import com.example.eventtracker.databinding.ItemRsvpEventBinding
import com.example.eventtracker.util.DateTimeUtils
import com.example.eventtracker.util.loadUrl

class MyEventsAdapter(
    private val onEventClick: (Event) -> Unit,
    private val onCancelRsvp: (Event) -> Unit
) : ListAdapter<Event, MyEventsAdapter.MyEventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventViewHolder {
        val binding = ItemRsvpEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyEventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyEventViewHolder(
        private val binding: ItemRsvpEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                // Load event image
                imgThumbnail.loadUrl(event.imageUrl)

                // Event details
                tvTitle.text = event.title
                tvDateTime.text = DateTimeUtils.formatDateTime(event.dateTime)
                tvVenue.text = event.venueName

                // Click listeners
                root.setOnClickListener {
                    onEventClick(event)
                }

                btnCancelRsvp.setOnClickListener {
                    onCancelRsvp(event)
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