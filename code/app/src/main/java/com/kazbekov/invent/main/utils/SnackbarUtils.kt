package com.kazbekov.invent.main.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showMessage(view: View, message: String, isLong: Boolean = false) {
    Snackbar.make(
        view,
        message,
        if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
    ).show()
}