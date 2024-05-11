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
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.ucFirst


@Composable
fun StudentHeadline(user: User?, student: User) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "${student.firstName.ucFirst()} ${student.lastName.ucFirst()}" + (if (student.id == user?.id) " (You)" else ""),
        )

        Text(
            text = student.email,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun StudentTrailingContent(
    user: User?,
    student: User,
    showEditModal: (User) -> Unit,
    showDeleteDialog: (User) -> Unit,
) {
    Row {
        IconButton(onClick = {
            showEditModal(student)
        }) {
            Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
        }

        IconButton(onClick = { showDeleteDialog(student) }) {
            Icon(Icons.Filled.Delete, "Delete", tint = Color.Red)
        }
    }
}
