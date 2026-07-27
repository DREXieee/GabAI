package com.gabai.presentation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gabai.presentation.theme.*

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
    onRegistrationComplete: () -> Unit = {}
) {
    var nickname by remember { mutableStateOf("") }
    var selectedProvinceIndex by remember { mutableStateOf(0) }
    var selectedCityIndex by remember { mutableStateOf(0) }
    var isProvinceDropdownExpanded by remember { mutableStateOf(false) }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }
    
    val selectedProvince = provincesData[selectedProvinceIndex]
    val selectedCity = selectedProvince.cities[selectedCityIndex]
    
    val isFormValid = nickname.isNotBlank() && selectedProvince.name.isNotBlank() && selectedCity.isNotBlank()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Fill in the information below",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Continue Button
        Button(
            onClick = onRegistrationComplete,
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Privacy Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Privacy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Your information is safe and secure. GabAI complies with the Philippine Data Privacy Act and does not share your personal data with third parties.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
