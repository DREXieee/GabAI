package com.gabai.presentation.ui.clinics

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.screenContentPadding
import androidx.compose.ui.res.painterResource
import com.gabai.R
import com.gabai.presentation.ui.common.GabAILogoButton
import com.gabai.presentation.ui.common.SkeletonList

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearestClinicsScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: NearestClinicsViewModel = viewModel(factory = NearestClinicsViewModelFactory(context))

    val clinicsWithDistance by viewModel.clinics.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var permissionsGranted by remember {
        mutableStateOf(
            LOCATION_PERMISSIONS.any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.any { it }
        if (permissionsGranted) viewModel.onLocationPermissionGranted()
    }

    LaunchedEffect(Unit) {
        if (permissionsGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearest Clinics", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { GabAILogoButton(navController) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
        ) {
            Text(
                text = "We'll refer you to a nearby participating clinic in your city or municipality for professional support.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.SpaceS)
            )

            if (!permissionsGranted) {
                Text(
                    text = "Location access is off, so clinics below aren't sorted by distance. " +
                        "You can still browse and get directions to any of them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Dimens.SpaceS)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = GabAIShapes.large
            ) {
                ClinicMap(
                    clinics = clinicsWithDistance.map { it.clinic },
                    userLocation = userLocation,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            when {
                isLoading && clinicsWithDistance.isEmpty() -> SkeletonList(
                    loadingDescription = "Loading clinics",
                    rowCount = 3,
                    showLeadingBadge = false
                )

                error != null && clinicsWithDistance.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }

                clinicsWithDistance.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No clinics available right now.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                    items(clinicsWithDistance, key = { it.clinic.id }) { entry ->
                        ClinicRow(
                            entry = entry,
                            onGetDirections = { context.startActivity(clinicDirectionsIntent(entry.clinic)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicRow(
    entry: ClinicWithDistance,
    onGetDirections: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = GabAIShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_map_pin),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.clinic.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(entry.clinic.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (entry.distanceKm != null) {
                Text(
                    text = "%.1f km away".format(entry.distanceKm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onGetDirections,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painterResource(R.drawable.ic_lucide_footprints), contentDescription = null)
                Text(" Get Directions", modifier = Modifier.padding(start = Dimens.SpaceXXS))
            }
        }
    }
}
