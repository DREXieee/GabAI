package com.gabai.presentation.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabai.data.remote.api.PSGC_NCR_REGION_CODE
import com.gabai.data.remote.api.PsgcRetrofitClient
import com.gabai.presentation.theme.Dimens
import kotlinx.coroutines.launch

// Province option shown in the dropdown. "Metro Manila" is synthetic (NCR has
// no province level in the PSGC hierarchy -- see PsgcApiService) so its cities
// are fetched from the region endpoint instead of the province one.
private data class ProvinceOption(
    val code: String,
    val name: String,
    val isNcr: Boolean = false
)

// Shared by RegistrationScreen and CompleteProfileScreen -- both need the same
// province/city cascading dropdowns, and the caller only ever needs the
// resolved province *name* (for the API), not the PSGC code used internally
// to look up cities.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinceCityFields(
    onSelectionChanged: (province: String?, city: String?, isValid: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

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

    suspend fun loadProvinces() {
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

    LaunchedEffect(Unit) { loadProvinces() }

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

    LaunchedEffect(selectedProvince, selectedCity) {
        onSelectionChanged(selectedProvince?.name, selectedCity, selectedProvince != null && !selectedCity.isNullOrBlank())
    }

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
        TextButton(onClick = { coroutineScope.launch { loadProvinces() } }) {
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
}
