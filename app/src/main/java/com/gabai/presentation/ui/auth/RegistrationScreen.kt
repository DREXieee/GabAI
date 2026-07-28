package com.gabai.presentation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.data.remote.models.RegisterRequest
import com.gabai.presentation.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

// Province data with corresponding cities
data class ProvinceData(
    val name: String,
    val cities: List<String>
)

private val provincesData = listOf(
    ProvinceData("Metro Manila", listOf("Manila", "Quezon City", "Caloocan", "Las Piñas", "Makati", "Malabon", "Mandaluyong", "Marikina", "Muntinlupa", "Navotas", "Parañaque", "Pasay", "Pasig", "San Juan", "Taguig", "Valenzuela")),
    ProvinceData("Cebu", listOf("Cebu City", "Mandaue", "Lapu-Lapu", "Talisay", "Toledo", "Naga", "Carcar", "Danao", "Bogo")),
    ProvinceData("Davao", listOf("Davao City", "Tagum", "Panabo", "Digos", "Mati", "Samal")),
    ProvinceData("Iloilo", listOf("Iloilo City", "Passi", "Pototan", "Guimbal", "Miagao", "Oton")),
    ProvinceData("Baguio", listOf("Baguio City")),
    ProvinceData("Bulacan", listOf("Malolos", "Meycauayan", "Marilao", "Santa Maria", "Baliuag", "San Jose del Monte", "Pulilan")),
    ProvinceData("Pampanga", listOf("Angeles City", "San Fernando", "Mabalacat", "Guagua", "Lubao", "Mexico")),
    ProvinceData("Laguna", listOf("Santa Rosa", "Biñan", "San Pedro", "Calamba", "Los Baños", "Cabuyao", "San Pablo")),
    ProvinceData("Cavite", listOf("Dasmariñas", "Bacoor", "Imus", "General Trias", "Tagaytay", "Cavite City", "Trece Martires")),
    ProvinceData("Rizal", listOf("Antipolo", "Cainta", "Taytay", "Angono", "Binangonan", "San Mateo", "Morong"))
)

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
    var selectedProvinceIndex by remember { mutableStateOf(0) }
    var selectedCityIndex by remember { mutableStateOf(0) }
    var isProvinceDropdownExpanded by remember { mutableStateOf(false) }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedProvince = provincesData[selectedProvinceIndex]
    val selectedCity = selectedProvince.cities[selectedCityIndex]

    val isFormValid = nickname.isNotBlank() && email.isNotBlank() && password.length >= 8 &&
        selectedProvince.name.isNotBlank() && selectedCity.isNotBlank()
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
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Province Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedProvince.name,
                        onValueChange = {},
                        label = { Text("Province") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isProvinceDropdownExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    DropdownMenu(
                        expanded = isProvinceDropdownExpanded,
                        onDismissRequest = { isProvinceDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        provincesData.forEachIndexed { index, province ->
                            DropdownMenuItem(
                                text = { Text(province.name) },
                                onClick = {
                                    selectedProvinceIndex = index
                                    selectedCityIndex = 0 // Reset city when province changes
                                    isProvinceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // City Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = {},
                        label = { Text("City/Municipality") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCityDropdownExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    DropdownMenu(
                        expanded = isCityDropdownExpanded,
                        onDismissRequest = { isCityDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        selectedProvince.cities.forEachIndexed { index, city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCityIndex = index
                                    isCityDropdownExpanded = false
                                }
                            )
                        }
                    }
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
                                        province = selectedProvince.name,
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
