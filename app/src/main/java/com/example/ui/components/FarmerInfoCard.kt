package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CocoaSurvey

@Composable
fun FarmerInfoCard(
    survey: CocoaSurvey,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("farmer_info_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar inside card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nature,
                            contentDescription = "Profil Petani & Kebun",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "INFORMASI DATA PETANI & KEBUN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Identitas & Parameter Lahan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(14.dp))

            // 2 Columns: 4 Left & 4 Right (Exact User Request)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 4 Bagian Kiri: (1. Nama, 2. ID, 3. Luas, 4. Jarak Tanam)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoItem(
                        icon = Icons.Default.Person,
                        label = "Nama Petani",
                        value = survey.farmerName,
                        highlight = true
                    )
                    InfoItem(
                        icon = Icons.Default.Badge,
                        label = "ID Petani / Kebun",
                        value = survey.farmerId
                    )
                    InfoItem(
                        icon = Icons.Default.GridView,
                        label = "Luas Tanam",
                        value = "${survey.plantedAreaHa} Ha",
                        highlight = true
                    )
                    InfoItem(
                        icon = Icons.Default.Straighten,
                        label = "Jarak Tanam",
                        value = survey.plantingDistance
                    )
                }

                // Vertical Divider between left & right
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // 4 Bagian Kanan: (5. Tanggal, 6. Surveyor, 7. Type Kebun, 8. Jumlah Pohon)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoItem(
                        icon = Icons.Default.CalendarMonth,
                        label = "Tanggal Survey",
                        value = survey.surveyDate
                    )
                    InfoItem(
                        icon = Icons.Default.PersonOutline,
                        label = "Surveyor",
                        value = survey.surveyorName
                    )
                    InfoItem(
                        icon = Icons.Default.Forest,
                        label = "Type Kebun",
                        value = survey.gardenType
                    )
                    InfoItem(
                        icon = Icons.Default.Nature,
                        label = "Jumlah Pohon",
                        value = "${survey.totalTrees} Pohon",
                        highlight = true
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
                color = if (highlight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
