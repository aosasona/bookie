package com.trulyao.bookie.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.controllers.AdminController
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.views.models.CurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserModal(
    context: Context = LocalContext.current,
    isOpen: Boolean,
    sheetState: SheetState,
    currentUser: CurrentUser?,
    scope: CoroutineScope,
    exitEditState: () -> Unit,
) {
    var isSavingChanges by remember { mutableStateOf(false) }

    suspend fun saveChanges() {
        try {
            isSavingChanges = true
            currentUser.let { u ->
                AdminController
                    .getInstance(context.getDatabase().userDao())
                    .updateAdminProfile(
                        uid = u?.uid?.value,
                        firstName = u?.firstName?.value,
                        lastName = u?.lastName?.value
                    )
            }

            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
            exitEditState()
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isSavingChanges = false
        }
    }

    if (isOpen && currentUser != null) {
        ModalBottomSheet(
            onDismissRequest = { exitEditState() },
            sheetState = sheetState,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = DEFAULT_VIEW_PADDING)
            ) {
                Text(
                    "Edit",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                TextInput(
                    title = "First name",
                    value = currentUser.firstName?.value ?: "",
                    onChange = { currentUser.firstName.value = it }
                )

                TextInput(
                    title = "Last name",
                    value = currentUser.lastName?.value ?: "",
                    onChange = { currentUser.lastName.value = it }
                )

                Spacer(modifier = Modifier.size(10.dp))

                LoadingButton(
                    isLoading = isSavingChanges,
                    horizontalArrangement = Arrangement.End,
                    onClick = { scope.launch { saveChanges() } }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
