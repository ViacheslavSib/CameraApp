package com.example.dev.cameraapp.customCamera.dialogs
import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

/**
 * @author v.sibichenko on 06/16/2019.
 */

/**
 * Shows an error message dialog.
 */
class ErrorDialog : DialogFragment() {
    private val ARG_MESSAGE = "message"

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
        AlertDialog.Builder(activity)
            .setMessage(arguments?.getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.ok)  { _, _ -> activity?.finish() }
            .create()

    companion object {
        fun newInstance(message: String) = ErrorDialog().apply {
            arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
        }
    }

}