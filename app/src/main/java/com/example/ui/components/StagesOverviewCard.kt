package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CocoaSurvey
import com.example.model.TreeSample
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StagesOverviewCard(
    survey: CocoaSurvey,
    onEditStage1: (Int) -> Unit,
    onRegenerateStage2: () -> Unit,
    onOpenTreeDetail: () -> Unit,
    onQuickAdjustTreePod: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleTrees = survey.getSampleTrees()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stages_overview_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TAHAPAN ESTIMASI PRODUKSI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // ================= TAHAP 1 =================
        StageCard(
            stageNumber = 1,
            stageTitle = "Jumlah Pohon Produktif Area Sampel",
            stageSubtitle = "Petak ukur 10 meter × 10 meter (Luas 100 m²)",
            accentColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Nature,
            resultValue = "${survey.sampleTreesCount}",
            resultUnit = "Pohon",
            testTag = "stage_1_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Jumlah pohon:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick +/- Adjusters for Tahap 1
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        IconButton(
                            onClick = { onEditStage1(survey.sampleTreesCount - 1) },
                            modifier = Modifier.size(32.dp),
                            enabled = survey.sampleTreesCount > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang Pohon Sampel", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${survey.sampleTreesCount} Pohon",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { onEditStage1(survey.sampleTreesCount + 1) },
                            modifier = Modifier.size(32.dp),
                            enabled = survey.sampleTreesCount < 26
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Pohon Sampel", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // ================= TAHAP 2 =================
        StageCard(
            stageNumber = 2,
            stageTitle = "Jumlah Buah Sampel Pohon Produktif",
            stageSubtitle = "Buah yang sudah besar dan masak pada pohon sampel",
            accentColor = MaterialTheme.colorScheme.secondary,
            icon = Icons.Default.AutoAwesome,
            resultValue = "${survey.totalSamplePods}",
            resultUnit = "Buah",
            testTag = "stage_2_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // List of Tree Sample Chips (P.A, P.B, P.C...)
                Text(
                    text = "Rincian Buah per Pohon Sampel:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Rincian Buah per Pohon Sampel diurut kebawah (1 kolom vertikal)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleTrees.forEachIndexed { idx, sample ->
                        TreePodChip(
                            sample = sample,
                            treeNumber = idx + 1
                        )
                    }
                }

                // Tombol Generate Tahap 2
                Button(
                    onClick = onRegenerateStage2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stage_2_generate_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // ================= TAHAP 3 =================
        StageCard(
            stageNumber = 3,
            stageTitle = "Jumlah Rata-rata Buah Setiap Sampel",
            stageSubtitle = "Jumlah buah sampel dibagi jumlah pohon sampel yang dihitung buahnya",
            accentColor = MaterialTheme.colorScheme.tertiary,
            icon = Icons.Default.Functions,
            resultValue = survey.formatAvgPods(),
            resultUnit = "Buah",
            testTag = "stage_3_card"
        ) {
            // Visual Math Formula Box
            MathEquationBox(
                leftValue = "${survey.totalSamplePods} Buah",
                leftLabel = "Total Buah Sampel",
                operator = "÷",
                rightValue = "${survey.sampleTreesCount} Pohon",
                rightLabel = "Jumlah Sampel Pohon",
                equalValue = "${survey.formatAvgPods()} Buah"
            )
        }

        // ================= TAHAP 4 =================
        StageCard(
            stageNumber = 4,
            stageTitle = "Jumlah Rata-rata Produksi per Pohon",
            stageSubtitle = "Rata-rata buah disetiap sampel pohon produktif dikali faktor 0.07 Kg/buah (konversi standar kakao)",
            accentColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Scale,
            resultValue = survey.formatAvgProductionPerTree(),
            resultUnit = "Kg",
            testTag = "stage_4_card"
        ) {
            MathEquationBox(
                leftValue = "${survey.formatAvgPods()} Buah",
                leftLabel = "Rata-rata Buah Sampel",
                operator = "×",
                rightValue = "0.07",
                rightLabel = "Konversi Standar Kakao",
                equalValue = "${survey.formatAvgProductionPerTree()} Kg"
            )
        }

        // ================= TAHAP 5 =================
        StageCard(
            stageNumber = 5,
            stageTitle = "Estimasi Total Produksi Kebun",
            stageSubtitle = "Rata-rata produksi per pohon dikali jumlah pohon dari data petani",
            accentColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.TrendingUp,
            resultValue = survey.formatTotalEstimatedPlotYield(),
            resultUnit = "Kg",
            testTag = "stage_5_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MathEquationBox(
                    leftValue = "${survey.formatAvgProductionPerTree()} Kg",
                    leftLabel = "Produksi per Pohon",
                    operator = "×",
                    rightValue = "${survey.totalTrees} Pohon",
                    rightLabel = "Jumlah Tegakan Pohon",
                    equalValue = "${survey.formatTotalEstimatedPlotYield()} Kg"
                )

                // Total Summary & Per Hektar Conversion
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PRODUKSI PER HEKTAR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Luas Lahan: ${survey.plantedAreaHa} Hektar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = "${survey.formatProductionPerHa()} Kg / Ha",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageCard(
    stageNumber: Int,
    stageTitle: String,
    stageSubtitle: String,
    accentColor: Color,
    icon: ImageVector,
    resultValue: String,
    resultUnit: String,
    testTag: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T.$stageNumber",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "TAHAP $stageNumber",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = stageTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Output Result Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = resultValue,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = resultUnit,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stageSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Body Content
            content()
        }
    }
}

@Composable
private fun TreePodChip(
    sample: TreeSample,
    treeNumber: Int
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pohon $treeNumber (${sample.code}):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${sample.pods} Buah",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun MathEquationBox(
    leftValue: String,
    leftLabel: String,
    operator: String,
    rightValue: String,
    rightLabel: String,
    equalValue: String,
    equalLabel: String = "Hasil Perhitungan"
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Kolom Kiri
                Column(
                    modifier = Modifier.weight(1.05f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = leftValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = leftLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }

                // Operator Hitungan (Rata atas sejajar dengan teks angka)
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = operator,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Kolom Kanan
                Column(
                    modifier = Modifier.weight(1.05f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = rightValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rightLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }

                // Simbol Sama Dengan (Rata atas sejajar dengan teks angka)
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "=",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Kolom Hasil (Rata Kiri)
                Column(
                    modifier = Modifier.weight(1.15f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = equalValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = equalLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
