package com.trulyao.bookie.views.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.ucFirst


@Composable
fun AdminHeadLine(user: User?, admin: User) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "${admin.firstName.ucFirst()} ${admin.lastName.ucFirst()}" + (if (user?.id == admin.id) " (You)" else ""),
        )

        Text(
            text = admin.email,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun AdminTrailingContent(user: User?, admin: User, showEditModal: (User) -> Unit, showDeleteDialog: (User) -> Unit) {
    if (user?.role == Role.SuperAdmin) {
        Row {
            IconButton(onClick = {
                showEditModal(admin)
            }) {
                Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            if (user?.id == admin.id) Unit
            else {
                IconButton(onClick = { showDeleteDialog(admin) }) {
                    Icon(Icons.Filled.Delete, "Delete", tint = Color.Red)
                }
            }
        }
    } else Unit
}