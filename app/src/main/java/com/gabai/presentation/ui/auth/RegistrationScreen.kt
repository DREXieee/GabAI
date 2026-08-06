package com.gabai.presentation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import com.gabai.data.remote.models.RegisterRequest
import com.gabai.presentation.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun RegistrationScreen(
    onRegistrationComplete: (needsLocation: Boolean) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nickname by remember { mutableStateOf("") }
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
                        onRegistrationComplete(needsLocation)
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

    var selectedProvinceName by remember { mutableStateOf<String?>(null) }
    var selectedCityName by remember { mutableStateOf<String?>(null) }
    var isLocationValid by remember { mutableStateOf(false) }

    val isFormValid = nickname.isNotBlank() && email.isNotBlank() && password.length >= 8 && isLocationValid
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
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(Dimens.IconSizeL)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceS))

                // Title
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Support for everyday stress and disaster recovery, including typhoons — " +
                        "fill in the information below to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                // Nickname Input
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Name or Nickname") },
                    placeholder = { Text("Enter your name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Email Input
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

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("At least 8 characters") },
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

                ProvinceCityFields { province, city, isValid ->
                    selectedProvinceName = province
                    selectedCityName = city
                    isLocationValid = isValid
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                // Continue Button
                Button(
                    onClick = {
                        errorMessage = null
                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.getApiService(context).register(
                                    RegisterRequest(
                                        email = email.trim(),
                                        password = password,
                                        displayName = nickname.trim(),
                                        province = selectedProvinceName,
                                        city = selectedCityName
                                    )
                                )
                                AuthSessionStore(context).saveSession(response)
                                onRegistrationComplete(false)
                            } catch (e: HttpException) {
                                errorMessage = if (e.code() == 400)
                                    "That email may already be registered, or the password is too short."
                                else
                                    "Registration failed (${e.code()}). Please try again."
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
                            text = "Continue",
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

                TextButton(onClick = onNavigateToLogin) {
                    Text("Already have an account? Log in")
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                // Privacy Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.SpaceM),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
                    ) {
                        Text(
                            text = "Privacy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            // NOTE: don't restore a bare "GabAI complies with the Philippine Data
                            // Privacy Act" claim here without an actual legal review backing it
                            // (registered privacy policy, DPO, data-subject-rights process, etc.) --
                            // asserting formal compliance we haven't substantiated is a real legal
                            // risk for an app handling sensitive mental-health data, arguably worse
                            // than not claiming it. This wording is deliberately more conservative.
                            text = "Your information is kept confidential and is not shared with third parties. " +
                                "A full privacy policy is in progress — for now, only verified GabAI " +
                                "staff and doctor accounts can access patient conversations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))
            }
        }
    }
}
