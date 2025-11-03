package com.example.eventtracker.util

import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.load
import com.google.android.material.snackbar.Snackbar

// Fragment Extensions
fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    view?.let {
        Snackbar.make(it, message, duration).show()
    }
}

fun Fragment.showErrorSnackbar(message: String) {
    view?.let {
        Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(com.example.eventtracker.R.color.error, null))
            .show()
    }
}

fun Fragment.showSuccessSnackbar(message: String) {
    view?.let {
        Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(com.example.eventtracker.R.color.success, null))
            .show()
    }
}

// View Extensions
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.setVisibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

// ImageView Extensions
fun ImageView.loadUrl(url: String?) {
    if (url.isNullOrEmpty()) {
        setImageResource(com.example.eventtracker.R.drawable.ic_event)
    } else {
        load(url) {
            crossfade(true)
            placeholder(com.example.eventtracker.R.drawable.ic_event)
            error(com.example.eventtracker.R.drawable.ic_event)
        }
    }
}

// String Extensions
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= 6
}