package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddSurveyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        farmerName: String,
        farmerId: String,
        village: String,
        plantedAreaHa: Double,
        plantingDistance: String,
        surveyDate: String,
        surveyorName: String,
        gardenType: String,
        totalTrees: Int,
        sampleTreesCount: Int,
        targetProductionKg: Double
    ) -> Unit
) {
    val currentDateStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    val autoId = remember { "KT-${SimpleDateFormat("yyMM", Locale.getDefault()).format(Date())}-${(100..999).random()}" }

    var farmerName by remember { mutableStateOf("") }
    var farmerId by remember { mutableStateOf(autoId) }
    var village by remember { mutableStateOf("") }
    var plantedAreaText by remember { mutableStateOf("1.0") }
    var plantingDistance by remember { mutableStateOf("3m x 3m") }
    var surveyDate by remember { mutableStateOf(currentDateStr) }
    var surveyorName by remember { mutableStateOf("") }
    var gardenType by remember { mutableStateOf("Kakao Sambung Pucuk") }
    var totalTreesText by remember { mutableStateOf("1000") }
    var sampleTreesText by remember { mutableStateOf("8") }
    var targetProductionText by remember { mutableStateOf("600.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_survey_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Tambah Survey Petani Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Input data petani dan kebun kakao",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "IDENTITAS PETANI & LAHAN:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text("Nama Petani *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = farmerId,
                    onValueChange = { farmerId = it },
                    label = { Text("ID Petani / Kebun") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text("Desa / Lokasi") },
                    placeholder = { Text("Contoh: Desa Sukamaju") },
                    leadingIcon = { Icon(Icons.Default.Forest, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = plantedAreaText,
                    onValueChange = { plantedAreaText = it },
                    label = { Text("Luas Tanam (Hektar / Ha) *") },
                    leadingIcon = { Icon(Icons.Default.GridView, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = plantingDistance,
                    onValueChange = { plantingDistance = it },
                    label = { Text("Jarak Tanam") },
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "SURVEY & TEKNIS KEBUN:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = surveyDate,
                    onValueChange = { surveyDate = it },
                    label = { Text("Tanggal Survey") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = surveyorName,
                    onValueChange = { surveyorName = it },
                    label = { Text("Nama Surveyor") },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = gardenType,
                    onValueChange = { gardenType = it },
                    label = { Text("Type Kebun / Klon") },
                    leadingIcon = { Icon(Icons.Default.Forest, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalTreesText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) totalTreesText = it },
                    label = { Text("Estimasi Total Pohon di Kebun") },
                    leadingIcon = { Icon(Icons.Default.Nature, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sampleTreesText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) sampleTreesText = it },
                    label = { Text("Tahap 1: Pohon Sampel 10m x 10m (1-26)") },
                    leadingIcon = { Icon(Icons.Default.Nature, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetProductionText,
                    onValueChange = { targetProductionText = it },
                    label = { Text("Jumlah Produksi GMR (Kg)") },
                    supportingText = { Text("Acuan pada jumlah buah Tahap 2") },
                    leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val area = plantedAreaText.replace(",", ".").toDoubleOrNull() ?: 1.0
                    val totalTrees = totalTreesText.toIntOrNull() ?: 1000
                    val sampleTrees = (sampleTreesText.toIntOrNull() ?: 8).coerceIn(1, 26)
                    val targetProd = targetProductionText.replace(",", ".").toDoubleOrNull() ?: 500.0

                    onConfirm(
                        farmerName.ifBlank { "Petani Baru" },
                        farmerId.ifBlank { autoId },
                        village.trim(),
                        area,
                        plantingDistance.ifBlank { "3m x 3m" },
                        surveyDate.ifBlank { currentDateStr },
                        surveyorName.ifBlank { "Surveyor" },
                        gardenType.ifBlank { "Kakao Sambung Pucuk" },
                        totalTrees,
                        sampleTrees,
                        targetProd
                    )
                },
                modifier = Modifier.testTag("submit_add_survey_button")
            ) {
                Text("Tambah & Hitung")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
