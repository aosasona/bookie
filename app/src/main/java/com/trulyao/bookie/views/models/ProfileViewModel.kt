package com.trulyao.bookie.views.models

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import com.trulyao.bookie.controllers.EditableUserData
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.AppDatabase
import com.trulyao.bookie.lib.handleException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
data class ProfileViewState(
    var firstName: String,
    var lastName: String,
    var email: String,
    var isSaving: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
class ProfileViewModel(val user: User) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfileViewState(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
        )
    )

    val state: StateFlow<ProfileViewState> = _uiState.asStateFlow()

    fun setFirstName(value: String) {
        this._uiState.update {
            it.copy(
                firstName = value,
                lastName = it.lastName,
                email = it.email,
            )
        }
    }

    fun setLastName(value: String) {
        this._uiState.update {
            it.copy(
                firstName = it.firstName,
                lastName = value,
                email = it.email,
            )
        }
    }

    fun setEmail(value: String) {
        this._uiState.update {
            it.copy(
                firstName = it.firstName,
                lastName = it.lastName,
                email = value,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    suspend fun saveChanges(context: Context, dob: DatePickerState) {
        try {
            this._uiState.value.isSaving = true
            val data = EditableUserData(
                firstName = this.state.value.firstName,
                lastName = this.state.value.lastName,
                email = this.state.value.email,
                dateOfBirth = Date(dob.selectedDateMillis ?: 0),
            )

            UserController
                .getInstance(AppDatabase.getInstance(context).userDao())
                .updateProfile(user.id ?: 0, data)

            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            this._uiState.value.isSaving = false
        }
    }
}