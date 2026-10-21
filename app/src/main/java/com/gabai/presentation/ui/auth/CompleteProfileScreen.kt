package com.gabai.presentation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.data.remote.models.UpdateLocationRequest
import com.gabai.presentation.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import androidx.compose.ui.res.painterResource
import com.gabai.R

// Google sign-in has no form step to collect province/city (unlike the
// regular registration form), so first-time Google users land here once,
// right after auth succeeds, before reaching the dashboard -- see
// PATCH /api/auth/location and NavGraph's needsLocation routing.
@Composable
fun CompleteProfileScreen(
    onLocationSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedProvinceName by remember { mutableStateOf<String?>(null) }
    var selectedCityName by remember { mutableStateOf<String?>(null) }
    var isLocationValid by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
                        painter = painterResource(R.drawable.ic_lucide_map_pin),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(Dimens.IconSizeL)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceS))

                Text(
                    text = "One more thing",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Tell us where you're based so GabAI can point you to nearby care when it matters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

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

                Button(
                    onClick = {
                        errorMessage = null
                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                RetrofitClient.getApiService(context).updateLocation(
                                    UpdateLocationRequest(province = selectedProvinceName!!, city = selectedCityName!!)
                                )
                                onLocationSaved()
                            } catch (e: HttpException) {
                                errorMessage = "Couldn't save (${e.code()}). Please try again."
                            } catch (e: Exception) {
                                errorMessage = "Couldn't reach GabAI. Check your connection and try again."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    enabled = isLocationValid && !isSubmitting,
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

                Spacer(modifier = Modifier.height(Dimens.SpaceM))
            }
        }
    }
}
