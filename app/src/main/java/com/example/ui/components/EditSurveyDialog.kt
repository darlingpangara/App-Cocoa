package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CocoaSurvey

@Composable
fun EditSurveyDialog(
    survey: CocoaSurvey,
    onDismiss: () -> Unit,
    onSave: (
        village: String,
        plantingDistance: String,
        surveyDate: String,
        surveyorName: String,
        gardenType: String,
        totalTrees: Int,
        sampleTreesCount: Int,
        targetProductionKg: Double
    ) -> Unit
) {
    // Editable state variables
    var village by remember { mutableStateOf(survey.village) }
    var plantingDistance by remember { mutableStateOf(survey.plantingDistance) }
    var surveyDate by remember { mutableStateOf(survey.surveyDate) }
    var surveyorName by remember { mutableStateOf(survey.surveyorName) }
    var gardenType by remember { mutableStateOf(survey.gardenType) }
    var totalTreesText by remember { mutableStateOf(survey.totalTrees.toString()) }
    var sampleTreesCountText by remember { mutableStateOf(survey.sampleTreesCount.toString()) }
    var targetProductionText by remember { mutableStateOf(survey.targetProductionKg.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_survey_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Edit Parameter Survey Kebun",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Data identitas (Nama, ID, Luas) terkunci",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info banner about locked fields
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sesuai aturan, Nama Petani, ID Kebun, dan Luas Lahan (Ha) dikunci dan tidak dapat diubah.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 3 LOCKED FIELDS (Nama, ID, Luas Lahan)
                Text(
                    text = "DATA UTAMA (TERKUNCI):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LockedField(
                    label = "Nama Petani",
                    value = survey.farmerName,
                    icon = Icons.Default.Person
                )

                LockedField(
                    label = "ID Petani / Kebun",
                    value = survey.farmerId,
                    icon = Icons.Default.Badge
                )

                LockedField(
                    label = "Luas Tanam (Ha)",
                    value = "${survey.plantedAreaHa} Hektar",
                    icon = Icons.Default.GridView
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // EDITABLE FIELDS
                Text(
                    text = "PARAMETER DAPAT DIUBAH:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text("Desa / Lokasi") },
                    leadingIcon = { Icon(Icons.Default.Forest, contentDescription = null) },
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
                    label = { Text("Jumlah Total Pohon di Kebun") },
                    leadingIcon = { Icon(Icons.Default.Nature, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sampleTreesCountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) sampleTreesCountText = it },
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
                    val totalTrees = totalTreesText.toIntOrNull() ?: survey.totalTrees
                    val sampleTrees = (sampleTreesCountText.toIntOrNull() ?: survey.sampleTreesCount).coerceIn(1, 26)
                    val targetProd = targetProductionText.replace(",", ".").toDoubleOrNull() ?: survey.targetProductionKg
                    onSave(
                        village.trim(),
                        plantingDistance,
                        surveyDate,
                        surveyorName,
                        gardenType,
                        totalTrees,
                        sampleTrees,
                        targetProd
                    )
                },
                modifier = Modifier.testTag("save_edit_survey_button")
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun LockedField(
    label: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Terkunci",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Terkunci",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
