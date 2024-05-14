package com.example.flow.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flow.R
import com.example.flow.data.model.AuthResult
import com.example.flow.ui.screens.destinations.LoginScreenDestination
import com.example.flow.ui.screens.destinations.TimeScreenDestination
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun RegisterScreen(
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
                Image(
                    painter = painterResource(R.drawable.logo), contentDescription = "logo",
                    modifier = Modifier.size(100.dp)
                )
                Text(
                    text = "Sign up",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Username") },
                    singleLine = true
                )
                TextField(
                    placeholder = { Text("Password") },
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true
                )
                Button(onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    // Username cannot have spaces
                    if (email.contains(" ")) {
                        Toast.makeText(
                            context,
                            "Username cannot have spaces",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (password.length < 8) {
                        Toast.makeText(
                            context,
                            "Password must be at least 8 characters",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    viewModel.signUp(email, password)
                }) {
                    Text("Sign up")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account?")
                    TextButton(onClick = {
                        navController.navigate(LoginScreenDestination.route)
                    }) {
                        Text("Log in")
                    }
                }
            }
        }
    }

}