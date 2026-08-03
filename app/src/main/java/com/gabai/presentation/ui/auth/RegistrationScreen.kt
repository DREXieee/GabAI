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
import com.gabai.data.remote.api.PSGC_NCR_REGION_CODE
import com.gabai.data.remote.api.PsgcRetrofitClient
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.data.remote.models.GoogleSignInRequest
import com.gabai.data.remote.models.RegisterRequest
import com.gabai.presentation.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

// Province option shown in the dropdown. "Metro Manila" is synthetic (NCR has
// no province level in the PSGC hierarchy -- see PsgcApiService) so its cities
// are fetched from the region endpoint instead of the province one.
private data class ProvinceOption(
    val code: String,
    val name: String,
    val isNcr: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegistrationComplete: () -> Unit = {},
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
                        onRegistrationComplete()
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

    var provinces by remember { mutableStateOf<List<ProvinceOption>>(emptyList()) }
    var isLoadingProvinces by remember { mutableStateOf(true) }
    var provincesLoadFailed by remember { mutableStateOf(false) }
    var selectedProvince by remember { mutableStateOf<ProvinceOption?>(null) }
    var isProvinceDropdownExpanded by remember { mutableStateOf(false) }

    var cities by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingCities by remember { mutableStateOf(false) }
    var citiesLoadFailed by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val fetched = PsgcRetrofitClient.service.getProvinces()
                .map { ProvinceOption(code = it.code, name = it.name) }
                .sortedBy { it.name }
            provinces = listOf(ProvinceOption(code = PSGC_NCR_REGION_CODE, name = "Metro Manila", isNcr = true)) + fetched
            provincesLoadFailed = false
        } catch (e: Exception) {
            provincesLoadFailed = true
        } finally {
            isLoadingProvinces = false
        }
    }

    LaunchedEffect(selectedProvince) {
        val province = selectedProvince
        selectedCity = null
        cities = emptyList()
        if (province == null) return@LaunchedEffect
        isLoadingCities = true
        citiesLoadFailed = false
        try {
            val fetched = if (province.isNcr) {
                PsgcRetrofitClient.service.getCitiesForRegion(province.code)
            } else {
                PsgcRetrofitClient.service.getCitiesForProvince(province.code)
            }
            cities = fetched.map { it.name }.sorted()
        } catch (e: Exception) {
            citiesLoadFailed = true
        } finally {
            isLoadingCities = false
        }
    }

    val isFormValid = nickname.isNotBlank() && email.isNotBlank() && password.length >= 8 &&
        selectedProvince != null && !selectedCity.isNullOrBlank()
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
                    text = "Fill in the information below",
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

                // Province Dropdown
                ExposedDropdownMenuBox(
                    expanded = isProvinceDropdownExpanded,
                    onExpandedChange = { isProvinceDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedProvince?.name ?: if (isLoadingProvinces) "Loading provinces..." else "",
                        onValueChange = {},
                        label = { Text("Province") },
                        placeholder = { if (provincesLoadFailed) Text("Couldn't load — tap to retry") },
                        readOnly = true,
                        enabled = !isLoadingProvinces,
                        trailingIcon = {
                            if (isLoadingProvinces) {
                                CircularProgressIndicator(modifier = Modifier.size(Dimens.SpaceM), strokeWidth = 2.dp)
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProvinceDropdownExpanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isProvinceDropdownExpanded,
                        onDismissRequest = { isProvinceDropdownExpanded = false }
                    ) {
                        provinces.forEach { province ->
                            DropdownMenuItem(
                                text = { Text(province.name) },
                                onClick = {
                                    selectedProvince = province
                                    isProvinceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (provincesLoadFailed) {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            isLoadingProvinces = true
                            try {
                                val fetched = PsgcRetrofitClient.service.getProvinces()
                                    .map { ProvinceOption(code = it.code, name = it.name) }
                                    .sortedBy { it.name }
                                provinces = listOf(ProvinceOption(code = PSGC_NCR_REGION_CODE, name = "Metro Manila", isNcr = true)) + fetched
                                provincesLoadFailed = false
                            } catch (e: Exception) {
                                provincesLoadFailed = true
                            } finally {
                                isLoadingProvinces = false
                            }
                        }
                    }) {
                        Text("Couldn't load provinces. Tap to retry.")
                    }
                }

                // City Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCityDropdownExpanded && cities.isNotEmpty(),
                    onExpandedChange = { if (cities.isNotEmpty()) isCityDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCity ?: if (isLoadingCities) "Loading cities..." else "",
                        onValueChange = {},
                        label = { Text("City/Municipality") },
                        readOnly = true,
                        enabled = selectedProvince != null && !isLoadingCities,
                        trailingIcon = {
                            if (isLoadingCities) {
                                CircularProgressIndicator(modifier = Modifier.size(Dimens.SpaceM), strokeWidth = 2.dp)
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCityDropdownExpanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isCityDropdownExpanded && cities.isNotEmpty(),
                        onDismissRequest = { isCityDropdownExpanded = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCity = city
                                    isCityDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (citiesLoadFailed) {
                    Text(
                        text = "Couldn't load cities for this province. Try selecting it again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
                                        province = selectedProvince?.name,
                                        city = selectedCity
                                    )
                                )
                                AuthSessionStore(context).saveSession(response)
                                onRegistrationComplete()
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
