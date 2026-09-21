package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "cocoa_surveys")
data class CocoaSurvey(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 8 Data Petani (4 Kiri & 4 Kanan)
    // 4 Kiri:
    val farmerName: String,         // 1. Nama Petani (Read-Only on Edit)
    val farmerId: String,           // 2. ID Petani (Read-Only on Edit)
    val village: String = "",       // Data Desa (Data bantu filter)
    val plantedAreaHa: Double,      // 3. Luas Tanam dlm Ha (Read-Only on Edit)
    val plantingDistance: String,   // 4. Jarak Tanam (misal: "3m x 3m")
    
    // 4 Kanan:
    val surveyDate: String,         // 5. Tanggal Survey (misal: "16/08/2026")
    val surveyorName: String,       // 6. Nama Surveyor
    val gardenType: String,         // 7. Type Kebun (misal: "Kakao Sambung Pucuk", "Kakao Monokultur", "Kakao Agroforestri")
    val totalTrees: Int,            // 8. Jumlah Total Pohon di Kebun
    
    // Nilai rujukan produksi historis/target untuk kalkulasi otomatis (Kg)
    val targetProductionKg: Double = 500.0,
    
    // Tahap 1: Jumlah pohon produktif 10m x 10m (100 m2)
    val sampleTreesCount: Int = 8,
    
    // Tahap 2: JSON list of TreeSample (P.A, P.B, P.C...) with pods count (2-15)
    val treeSamplesJson: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Parse sample trees
    fun getSampleTrees(): List<TreeSample> {
        val parsed = TreeSampleHelper.parseJson(treeSamplesJson)
        if (parsed.isNotEmpty()) return parsed
        return TreeSampleHelper.generateDynamicSamples(sampleTreesCount, targetProductionKg, totalTrees)
    }

    // Tahap 2: Total Buah Sampel
    val totalSamplePods: Int
        get() = getSampleTrees().sumOf { it.pods }

    // Tahap 3: Jumlah rata-rata buah disetiap sampel pohon produktif (Buah) - Hasil sebenarnya (2 desimal)
    val avgPodsPerSampleTree: Double
        get() {
            val count = sampleTreesCount.coerceAtLeast(1)
            return totalSamplePods.toDouble() / count
        }

    val avgPodsPerSampleTreeInt: Int
        get() {
            val count = sampleTreesCount.coerceAtLeast(1)
            return Math.round(totalSamplePods.toDouble() / count).toInt()
        }

    // Tahap 4: Jumlah Rata-rata produksi per pohon (Kg)
    // Formula: Rata-rata buah (desimal) * 0.07
    val avgProductionPerTreeKg: Double
        get() = avgPodsPerSampleTree * 0.07

    // Estimasi Pohon Produktif per Ha (berdasarkan plot 10m x 10m = 100m2 -> dikali 100)
    val productiveTreesPerHa: Double
        get() = (sampleTreesCount * 100).toDouble()

    // Tahap 5: Pengali menggunakan Jumlah Pohon dari Data Petani (totalTrees)
    // Total Estimasi Produksi Keseluruhan Kebun (Kg) = Rata-rata produksi per pohon (Kg) * Jumlah Pohon (Data Petani)
    val totalEstimatedPlotYieldKg: Double
        get() = avgProductionPerTreeKg * totalTrees

    // Estimasi Produksi per Hektar (Kg/Ha) = Total Produksi / Luas Lahan (Ha)
    val productionPerHaKg: Double
        get() = if (plantedAreaHa > 0) totalEstimatedPlotYieldKg / plantedAreaHa else totalEstimatedPlotYieldKg

    // Formatted strings for display
    fun formatTotalEstimatedPlotYield(): String {
        return String.format(Locale.US, "%,.2f", totalEstimatedPlotYieldKg)
    }

    fun formatProductionPerHa(): String {
        return String.format(Locale.US, "%,.2f", productionPerHaKg)
    }

    fun formatAvgProductionPerTree(): String {
        return String.format(Locale.US, "%.3f", avgProductionPerTreeKg)
    }

    // Format hasil sebenarnya 2 angka di belakang koma untuk Tahap 3
    fun formatAvgPods(): String {
        return String.format(Locale.US, "%.2f", avgPodsPerSampleTree)
    }
}
