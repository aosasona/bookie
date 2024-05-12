package com.trulyao.bookie.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.controllers.EditableUserData
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.views.models.CurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserModal(
    user: User?,
    context: Context = LocalContext.current,
    isOpen: Boolean,
    sheetState: SheetState,
    currentUser: CurrentUser?,
    scope: CoroutineScope,
    exitEditState: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val dob = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
    var password by remember { mutableStateOf("") }
    var isSavingChanges by remember { mutableStateOf(false) }

    suspend fun saveChanges() {
        try {
            isSavingChanges = true

            val controller = UserController.getInstance(context.getDatabase().userDao())
            var toastText = "Profile"

            currentUser.let { u ->
                controller.updateProfile(
                    u?.uid?.value ?: 0,
                    EditableUserData(
                        firstName = u?.firstName?.value ?: "",
                        lastName = u?.lastName?.value ?: "",
                        email = u?.email?.value ?: "",
                        dateOfBirth = Date(dob.selectedDateMillis ?: currentUser!!.dateOfBirth.time),
                    )
                )

                if ((user?.role == Role.SuperAdmin || u?.role?.value == Role.Student) && password.isNotEmpty()) {
                    toastText += " and password"
                    controller.adminUpdatePassword(u?.uid?.value ?: 0, password)
                }
            }

            Toast.makeText(context, "$toastText updated!", Toast.LENGTH_SHORT).show()
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
                    .verticalScroll(scrollState)
            ) {
                Text(
                    "Edit",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                TextInput(
                    title = "First name",
                    value = currentUser.firstName.value,
                    onChange = { currentUser.firstName.value = it }
                )

                TextInput(
                    title = "Last name",
                    value = currentUser.lastName.value,
                    onChange = { currentUser.lastName.value = it }
                )

                // We only want to allow edits to these fields if the admin is a super admin or the target user is a student; which is accessible to everyone
                if (user?.role == Role.SuperAdmin || currentUser.role.value == Role.Student) {
                    TextInput(title = "E-mail address", value = currentUser.email.value, onChange = { currentUser.email.value = it })

                    PasswordInput(title = "Password", value = password, onChange = { password = it })

                    DatePicker(state = dob, title = { Text("Date of birth") })
                }

                Spacer(modifier = Modifier.size(10.dp))

                LoadingButton(
                    isLoading = isSavingChanges,
                    horizontalArrangement = Arrangement.End,
                    onClick = { scope.launch { saveChanges() } }
                ) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.size(30.dp))
            }
        }
    }
}
