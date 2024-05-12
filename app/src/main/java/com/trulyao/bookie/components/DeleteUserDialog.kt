package com.trulyao.bookie.components

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.lib.ucFirst
import com.trulyao.bookie.views.models.CurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DeleteUserDialog(context: Context, scope: CoroutineScope, target: CurrentUser?, reload: () -> Unit, onDismiss: () -> Unit) {
    var isDeleting by remember { mutableStateOf(false) }

    suspend fun onConfirm() {
        try {
            isDeleting = true

            val userId = target?.uid?.value ?: 0
            if (userId == 0) throw Exception("User ID is not valid")

            UserController.getInstance(context.getDatabase().userDao()).deleteUser(userId)
            reload()

            Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isDeleting = false
            onDismiss()
        }
    }


    AlertDialog(
        icon = { Icon(Icons.Default.Warning, "Warning") },
        title = { Text("Confirm action") },
        text = {
            Text(
                text = "Are you sure you want to delete this user (${target?.firstName?.value?.ucFirst()} ${target?.lastName?.value?.ucFirst()})?",
                textAlign = TextAlign.Center
            )
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            LoadingButton(isLoading = isDeleting, onClick = { scope.launch { onConfirm() } }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
