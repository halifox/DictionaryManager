package com.github.dictionary

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun devDialog(context: Context) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.dialog_dev_titile)
        .setMessage(R.string.dialog_dev_message)
        .setPositiveButton(R.string.dialog_yes) { _, _ -> }
        .show()
}