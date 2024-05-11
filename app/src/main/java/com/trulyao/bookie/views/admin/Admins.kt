package com.trulyao.bookie.views.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.ucFirst

@Composable
fun AdminsList(
    user: User?,
    users: List<User>,
    paddingValues: PaddingValues,
    showEditModal: (User) -> Unit,
) {
    LazyColumn(contentPadding = paddingValues) {
        items(users) { admin ->
            ListItem(
                headlineContent = {
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
                },
                trailingContent = {
                    if (user?.role == Role.SuperAdmin) {
                        Row {
                            IconButton(onClick = {
                                showEditModal(admin)
                            }) {
                                Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                            }

//                                    if (user?.id == admin.id) null
//                                    else {
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = Color.Red)
                            }
//                                    }
                        }
                    }
                }
            )
        }
    }
}