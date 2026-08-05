package com.gabai.presentation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gabai.data.auth.GoogleAuthHelper
import com.gabai.data.auth.GoogleSignInResult
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.data.remote.models.GoogleSignInRequest
import com.gabai.data.remote.models.LoginRequest
import com.gabai.presentation.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun LoginScreen(
    onLoginComplete: (needsLocation: Boolean) -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var googleErrorMessage by remember { mutableStateOf<String?>(null) }

    fun handleGoogleSignIn() {
        googleErrorMessage = null
        isGoogleSigningIn = true
        coroutineScope.launch {
            when (val result = GoogleAuthHelper.signIn(context)) {
                is GoogleSignInResult.Success -> {
                    try {
                        val response = RetrofitClient.getApiService(context)
                            .googleSignIn(GoogleSignInRequest(result.idToken))
                        AuthSessionStore(context).saveSession(response)
                        val needsLocation = response.user.province.isNullOrBlank() || response.user.city.isNullOrBlank()
                        onLoginComplete(needsLocation)
                    } catch (e: Exception) {
                        googleErrorMessage = "Couldn't sign in with Google. Please try again."
                    }
                }
                is GoogleSignInResult.Cancelled -> Unit
                is GoogleSignInResult.NotConfigured ->
                    googleErrorMessage = "Google sign-in isn't set up for this build yet."
                is GoogleSignInResult.Failure -> googleErrorMessage = result.message
            }
            isGoogleSigningIn = false
        }
    }

    val isFormValid = email.isNotBlank() && password.isNotBlank()
    val windowWidthSize = rememberWindowWidthSize()

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = if (windowWidthSize == WindowWidthSize.COMPACT) Dp.Unspecified else Dimens.ReadingMaxWidth)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
            ) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXL))

                Box(
                    modifier = Modifier
                        .size(Dimens.AuthIconBadgeSize)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SelfImprovement,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(Dimens.IconSizeL)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceS))

                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Log in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("you@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                TextButton(
                    onClick = onNavigateToForgotPassword,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot password?")
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                Button(
                    onClick = {
                        errorMessage = null
                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.getApiService(context).login(
                                    LoginRequest(email = email.trim(), password = password)
                                )
                                AuthSessionStore(context).saveSession(response)
                                val needsLocation = response.user.province.isNullOrBlank() || response.user.city.isNullOrBlank()
                                onLoginComplete(needsLocation)
                            } catch (e: HttpException) {
                                errorMessage = if (e.code() == 401)
                                    "Incorrect email or password."
                                else
                                    "Login failed (${e.code()}). Please try again."
                            } catch (e: Exception) {
                                errorMessage = "Couldn't reach GabAI. Check your connection and try again."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    enabled = isFormValid && !isSubmitting,
                    shape = PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Ink,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.SpaceM),
                            color = Ink,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Log In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Dimens.SpaceS)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedButton(
                    onClick = { handleGoogleSignIn() },
                    enabled = !isGoogleSigningIn,
                    shape = PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight)
                ) {
                    if (isGoogleSigningIn) {
                        CircularProgressIndicator(modifier = Modifier.size(Dimens.SpaceM), strokeWidth = 2.dp)
                    } else {
                        Text("Continue with Google")
                    }
                }

                if (googleErrorMessage != null) {
                    Text(
                        text = googleErrorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextButton(onClick = onNavigateToRegister) {
                    Text("Don't have an account? Register")
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))
            }
        }
    }
}
