package com.trulyao.bookie.views.models

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import com.trulyao.bookie.entities.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
data class ProfileViewState constructor(
    var firstName: String,
    var lastName: String,
    var email: String,
    var dob: DatePickerState,

    var isSaving: Boolean = false,
)

class ProfileViewModel(val user: User) : ViewModel() {
    @OptIn(ExperimentalMaterial3Api::class)
    private val _uiState = MutableStateFlow(
        ProfileViewState(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            dob = DatePickerState(
                locale = CalendarLocale.ROOT,
                initialSelectedDateMillis = user.dateOfBirth.time.toLong()
            )
        )
    )
    val state: StateFlow<ProfileViewState> = _uiState.asStateFlow()

    suspend fun saveChanges() {}
}