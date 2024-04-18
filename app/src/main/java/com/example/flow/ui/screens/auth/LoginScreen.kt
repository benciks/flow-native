package com.example.flow.ui.screens.auth

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import com.example.flow.data.model.AuthResult
import com.example.flow.ui.screens.NavGraphs
import com.example.flow.ui.screens.destinations.LoginScreenDestination
import com.example.flow.ui.screens.destinations.RegisterScreenDestination
import com.example.flow.ui.screens.destinations.TimeScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RootNavGraph(start = true)
@Destination
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(viewModel, context) {
        viewModel.authResults.collect { result ->
            when (result) {
                is AuthResult.Authorized -> {
                    navController.navigate(TimeScreenDestination.route) {
                        popUpTo(LoginScreenDestination.route) {
                            inclusive = true
                        }
                    }
                }

                is AuthResult.Unauthorized -> {
                    Toast.makeText(context, "Unauthorized", Toast.LENGTH_SHORT).show()
                }

                is AuthResult.UnknownError -> {
                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Username") }
                )
                TextField(
                    placeholder = { Text("Password") },
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = PasswordVisualTransformation()
                )
                Button(onClick = { viewModel.login(email, password) }) {
                    Text("Login")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account?")
                    TextButton(onClick = {
                        navController.navigate(RegisterScreenDestination)
                    }) {
                        Text("Sign up")
                    }
                }
            }
        }
    }
}