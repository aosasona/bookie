package com.trulyao.bookie.views.students

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.components.UserBottomNavigationBar
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.signOut
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.repositories.mockUser
import kotlinx.coroutines.launch

@Composable
fun Profile(user: User, navigateToSignIn: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

            Button(
                onClick = { scope.launch { handleSignOut() } },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign out", color = Color.White)
            }
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
    ) {
        it
        Profile(user = mockUser(Role.Student), navigateToSignIn = {})
    }
}