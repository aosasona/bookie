package com.trulyao.bookie.views.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.trulyao.bookie.components.LoadingButton
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.components.UserBottomNavigationBar
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.signOut
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.repositories.mockUser
import com.trulyao.bookie.views.models.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    user: User,
    viewModel: ProfileViewModel = ProfileViewModel(user),
    navigateToSignIn: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()


    suspend fun handleSignOut() {
        signOut(context)
        navigateToSignIn()
    }

    ProtectedView(user = user) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DEFAULT_VIEW_PADDING)
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                "Profile",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(30.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextInput(
                    title = "First name",
                    value = state.firstName,
                    onChange = { state.firstName = it }
                )

                TextInput(
                    title = "Last name",
                    value = state.lastName,
                    onChange = { state.lastName = it }
                )

                TextInput(
                    title = "E-mail address",
                    value = state.email,
                    onChange = { state.email = it }
                )

                DatePicker(
                    state = state.dob,
                    title = { Text("Date of birth") }
                )

                LoadingButton(
                    isLoading = state.isSaving,
                    horizontalArrangement = Arrangement.End,
                    onClick = { /*TODO*/ }) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.size(36.dp))

            HorizontalDivider()

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Change password",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Row {
                        Text("Change password")
                    }
                }
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.size(52.dp))

            Button(
                onClick = { scope.launch { handleSignOut() } },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign out", color = Color.White)
            }

            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomAppBar { UserBottomNavigationBar(navController = navController) }
        }
    ) { it ->
        Profile(user = mockUser(Role.Student), navigateToSignIn = {})
        Box(modifier = Modifier.padding(it))
    }
}