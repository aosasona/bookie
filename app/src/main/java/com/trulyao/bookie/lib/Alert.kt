package com.trulyao.bookie.lib

import android.app.AlertDialog
import android.content.Context

public final class Alert {
    enum class AlertType {
        Success,
        Error
    }

    companion object {
        fun show(context: Context, message: String, alertType: AlertType = AlertType.Error) {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setTitle(
                when (alertType) {
                    AlertType.Success -> "Success"
                    AlertType.Error -> "Error"
                }
            )
            alertBuilder.setMessage(message)
            alertBuilder.setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }

            val alertDialog: AlertDialog = alertBuilder.create()
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
    }
}
