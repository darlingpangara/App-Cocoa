package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.CocoaSurvey
import com.example.model.TreeSample
import com.example.model.TreeSampleHelper
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporterImporter {

    private val CSV_HEADER = listOf(
        "Nama Petani",
        "ID Petani",
        "Desa",
        "Luas Tanam (Ha)",
        "Jarak Tanam",
        "Tanggal Survey",
        "Surveyor",
        "Type Kebun",
        "Jumlah Pohon",
        "Jumlah Produksi GMR (Kg)",
        "Tahap 1: Pohon Sampel 10x10m (Pohon)",
        "Tahap 2: Total Buah Sampel (Buah)",
        "Rincian Buah Sampel",
        "Tahap 3: Rata2 Buah (Buah)",
        "Tahap 4: Produksi/Pohon (Kg)",
        "Tahap 5: Estimasi Total Produksi Kebun (Kg)",
        "Produksi per Hektar (Kg/Ha)"
    )

    fun generateCsvContent(surveys: List<CocoaSurvey>): String {
        val sb = StringBuilder()
        // Header
        sb.append(CSV_HEADER.joinToString(",") { escapeCsv(it) }).append("\r\n")

        // Rows
        for (survey in surveys) {
            val sampleTrees = survey.getSampleTrees()
            val sampleDetails = sampleTrees.joinToString("; ") { "${it.code}:${it.pods} Buah" }
            val row = listOf(
                survey.farmerName,
                survey.farmerId,
                survey.village,
                String.format(Locale.US, "%.2f", survey.plantedAreaHa),
                survey.plantingDistance,
                survey.surveyDate,
                survey.surveyorName,
                survey.gardenType,
                survey.totalTrees.toString(),
                String.format(Locale.US, "%.1f", survey.targetProductionKg),
                survey.sampleTreesCount.toString(),
                survey.totalSamplePods.toString(),
                sampleDetails,
                String.format(Locale.US, "%.2f", survey.avgPodsPerSampleTree),
                String.format(Locale.US, "%.3f", survey.avgProductionPerTreeKg),
                String.format(Locale.US, "%.2f", survey.totalEstimatedPlotYieldKg),
                String.format(Locale.US, "%.2f", survey.productionPerHaKg)
            )
            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\r\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var escaped = value.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains(";") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }

    fun exportAndShareCsv(context: Context, surveys: List<CocoaSurvey>, fileNamePrefix: String = "Estimasi_Kakao"): File? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${fileNamePrefix}_$timeStamp.csv"
            val file = File(context.cacheDir, fileName)

            val csvData = generateCsvContent(surveys)
            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Excel opens with proper encoding
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                fos.write(csvData.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Tabel Estimasi Produksi Kebun Kakao ($fileName)")
                putExtra(Intent.EXTRA_TEXT, "Berikut terlampir data tabel Excel estimasi produksi kebun kakao.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Bagikan / Ekspor Tabel Excel")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private data class ColumnIndices(
        val idxFarmerName: Int = -1,
        val idxFarmerId: Int = -1,
        val idxVillage: Int = -1,
        val idxPlantedArea: Int = -1,
        val idxPlantingDistance: Int = -1,
        val idxSurveyDate: Int = -1,
        val idxSurveyor: Int = -1,
        val idxGardenType: Int = -1,
        val idxTotalTrees: Int = -1,
        val idxTargetProd: Int = -1,
        val idxSampleTrees: Int = -1,
        val idxSampleDetails: Int = -1
    )

    fun parseCsvInputStream(inputStream: InputStream): List<CocoaSurvey> {
        val result = mutableListOf<CocoaSurvey>()
        val allLines = mutableListOf<String>()

        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.isNotEmpty()) {
                allLines.add(trimmed)
            }
        }

        if (allLines.isEmpty()) return emptyList()

        // Clean UTF-8 BOM if present on first line
        if (allLines[0].startsWith("\uFEFF")) {
            allLines[0] = allLines[0].substring(1).trim()
        }

        // Detect overall file delimiter by inspecting non-quoted separators in the first few lines
        val delimiter = detectDelimiter(allLines.take(5))

        var headerIndices: ColumnIndices? = null
        var startIndex = 0

        // Check if first line is a header
        val firstLineTokens = parseCsvLineWithDelimiter(allLines[0], delimiter)
        val detected = detectHeaderIndices(firstLineTokens)
        if (detected != null) {
            headerIndices = detected
            startIndex = 1
        }

        for (i in startIndex until allLines.size) {
            val currentLine = allLines[i]
            if (currentLine.isBlank()) continue

            val parts = parseCsvLineWithDelimiter(currentLine, delimiter)
            if (parts.size < 3) continue

            try {
                val farmerName: String
                val farmerId: String
                val village: String
                val plantedAreaRaw: String?
                val plantingDist: String
                val date: String
                val surveyor: String
                val gardenType: String
                val totalTreesRaw: String?
                val targetProdRaw: String?
                val sampleTreesRaw: String?
                val sampleDetailsRaw: String?

                if (headerIndices != null) {
                    farmerName = getByCol(parts, headerIndices.idxFarmerName) ?: "Petani"
                    farmerId = getByCol(parts, headerIndices.idxFarmerId) ?: "ID-${System.currentTimeMillis() % 10000}"
                    village = getByCol(parts, headerIndices.idxVillage) ?: ""
                    plantedAreaRaw = getByCol(parts, headerIndices.idxPlantedArea)
                    plantingDist = getByCol(parts, headerIndices.idxPlantingDistance) ?: "3m x 3m"
                    date = getByCol(parts, headerIndices.idxSurveyDate) ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    surveyor = getByCol(parts, headerIndices.idxSurveyor) ?: "Surveyor"
                    gardenType = getByCol(parts, headerIndices.idxGardenType) ?: "Kakao Sambung Pucuk"
                    totalTreesRaw = getByCol(parts, headerIndices.idxTotalTrees)
                    targetProdRaw = getByCol(parts, headerIndices.idxTargetProd)
                    sampleTreesRaw = getByCol(parts, headerIndices.idxSampleTrees)
                    sampleDetailsRaw = getByCol(parts, headerIndices.idxSampleDetails)
                } else {
                    // Positional fallback:
                    // Check if format is new (Col 0 = Nama, Col 1 = ID, Col 2 = Desa, Col 3 = Luas Ha, ... Col 8 = Jumlah Pohon)
                    val isCol3Area = parts.size >= 9 && parts.getOrNull(3)?.replace("Ha", "", ignoreCase = true)?.replace(",", ".")?.trim()?.toDoubleOrNull() != null

                    if (isCol3Area) {
                        farmerName = parts.getOrNull(0)?.trim() ?: "Petani"
                        farmerId = parts.getOrNull(1)?.trim() ?: "ID-${System.currentTimeMillis() % 10000}"
                        village = parts.getOrNull(2)?.trim() ?: ""
                        plantedAreaRaw = parts.getOrNull(3)?.trim()
                        plantingDist = parts.getOrNull(4)?.trim() ?: "3m x 3m"
                        date = parts.getOrNull(5)?.trim() ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        surveyor = parts.getOrNull(6)?.trim() ?: "Surveyor"
                        gardenType = parts.getOrNull(7)?.trim() ?: "Kakao Sambung Pucuk"
                        totalTreesRaw = parts.getOrNull(8)?.trim()
                        targetProdRaw = parts.getOrNull(9)?.trim()
                        sampleTreesRaw = parts.getOrNull(10)?.trim()
                        sampleDetailsRaw = parts.getOrNull(12)?.trim()
                    } else {
                        // Older 8-10 column format (Col 0 = ID, Col 1 = Nama, Col 2 = Luas, Col 3 = Jarak, Col 4 = Tgl, Col 5 = Surveyor, Col 6 = Type, Col 7 = Total Pohon, Col 8 = Target Prod, Col 9 = Pohon Sampel)
                        farmerId = parts.getOrNull(0)?.trim() ?: "ID-${System.currentTimeMillis() % 10000}"
                        farmerName = parts.getOrNull(1)?.trim() ?: "Petani"
                        village = ""
                        plantedAreaRaw = parts.getOrNull(2)?.trim()
                        plantingDist = parts.getOrNull(3)?.trim() ?: "3m x 3m"
                        date = parts.getOrNull(4)?.trim() ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        surveyor = parts.getOrNull(5)?.trim() ?: "Surveyor"
                        gardenType = parts.getOrNull(6)?.trim() ?: "Kakao Sambung Pucuk"
                        totalTreesRaw = parts.getOrNull(7)?.trim()
                        targetProdRaw = parts.getOrNull(8)?.trim()
                        sampleTreesRaw = parts.getOrNull(9)?.trim()
                        sampleDetailsRaw = parts.getOrNull(11)?.trim()
                    }
                }

                val plantedArea = parseRobustDouble(plantedAreaRaw, 1.0)
                val totalTrees = parseRobustTotalTrees(totalTreesRaw, 1000)
                val targetProd = parseRobustDouble(targetProdRaw, 500.0)
                val sampleTrees = parseRobustInt(sampleTreesRaw, 8).coerceIn(1, 26)

                val sampleDetails = sampleDetailsRaw ?: ""
                val parsedSamples = if (sampleDetails.isNotEmpty() && sampleDetails.contains(":")) {
                    sampleDetails.split(";", ",").mapNotNull { token ->
                        val sub = token.trim().split(":")
                        if (sub.size == 2) {
                            val code = sub[0].trim()
                            val pods = parseRobustInt(sub[1], 5)
                            TreeSample(code = code, pods = pods)
                        } else null
                    }
                } else {
                    TreeSampleHelper.generateDynamicSamples(sampleTrees, targetProd, totalTrees)
                }

                result.add(
                    CocoaSurvey(
                        farmerId = farmerId.ifBlank { "ID-${System.currentTimeMillis() % 10000}" },
                        farmerName = farmerName.ifBlank { "Petani" },
                        village = village,
                        plantedAreaHa = plantedArea,
                        plantingDistance = plantingDist.ifBlank { "3m x 3m" },
                        surveyDate = date,
                        surveyorName = surveyor.ifBlank { "Surveyor" },
                        gardenType = gardenType.ifBlank { "Kakao Sambung Pucuk" },
                        totalTrees = totalTrees,
                        targetProductionKg = targetProd,
                        sampleTreesCount = sampleTrees,
                        treeSamplesJson = TreeSampleHelper.toJson(parsedSamples)
                    )
                )
            } catch (e: Exception) {
                // Skip malformed row
            }
        }
        return result
    }

    private fun getByCol(parts: List<String>, index: Int): String? {
        if (index in parts.indices) {
            val v = parts[index].trim()
            return if (v.isNotEmpty()) v else null
        }
        return null
    }

    private fun detectHeaderIndices(headerTokens: List<String>): ColumnIndices? {
        var idxFarmerName = -1
        var idxFarmerId = -1
        var idxVillage = -1
        var idxPlantedArea = -1
        var idxPlantingDistance = -1
        var idxSurveyDate = -1
        var idxSurveyor = -1
        var idxGardenType = -1
        var idxTotalTrees = -1
        var idxTargetProd = -1
        var idxSampleTrees = -1
        var idxSampleDetails = -1

        var matchedCount = 0

        for (i in headerTokens.indices) {
            val rawH = headerTokens[i].lowercase(Locale.ROOT).trim()

            when {
                (rawH.contains("rincian") || rawH.contains("detail") || rawH.contains("buah sampel")) && idxSampleDetails == -1 -> {
                    idxSampleDetails = i
                    matchedCount++
                }
                (rawH.contains("tahap 1") || (rawH.contains("sampel") && (rawH.contains("pohon") || rawH.contains("10x10") || rawH.contains("10m")))) && idxSampleTrees == -1 -> {
                    idxSampleTrees = i
                    matchedCount++
                }
                (rawH.contains("total pohon") || rawH.contains("jumlah pohon") || rawH.contains("populasi") || (rawH.contains("pohon") && !rawH.contains("sampel") && !rawH.contains("sample") && !rawH.contains("tahap"))) && idxTotalTrees == -1 -> {
                    idxTotalTrees = i
                    matchedCount++
                }
                (rawH.contains("target") || rawH.contains("gmr") || rawH.contains("produksi gmr") || rawH.contains("jumlah produksi") || rawH.contains("produksi (kg)")) && idxTargetProd == -1 -> {
                    idxTargetProd = i
                    matchedCount++
                }
                (rawH.contains("luas") || rawH.contains("lahan") || rawH.contains("(ha)") || rawH.contains("hektar")) && idxPlantedArea == -1 -> {
                    idxPlantedArea = i
                    matchedCount++
                }
                (rawH.contains("jarak") || rawH.contains("spasi")) && idxPlantingDistance == -1 -> {
                    idxPlantingDistance = i
                    matchedCount++
                }
                (rawH.contains("tanggal") || rawH.contains("tgl") || rawH.contains("date")) && idxSurveyDate == -1 -> {
                    idxSurveyDate = i
                    matchedCount++
                }
                (rawH.contains("surveyor") || rawH.contains("petugas") || rawH.contains("pencatat")) && idxSurveyor == -1 -> {
                    idxSurveyor = i
                    matchedCount++
                }
                (rawH.contains("type") || rawH.contains("tipe") || rawH.contains("klon") || rawH.contains("kebun")) && !rawH.contains("produksi") && idxGardenType == -1 -> {
                    idxGardenType = i
                    matchedCount++
                }
                (rawH.contains("id") || rawH.contains("kode") || rawH.contains("nik") || rawH.contains("no")) && !rawH.contains("pohon") && idxFarmerId == -1 -> {
                    idxFarmerId = i
                    matchedCount++
                }
                (rawH.contains("desa") || rawH.contains("lokasi") || rawH.contains("alamat") || rawH.contains("kelurahan")) && idxVillage == -1 -> {
                    idxVillage = i
                    matchedCount++
                }
                rawH.contains("nama") && !rawH.contains("surveyor") && idxFarmerName == -1 -> {
                    idxFarmerName = i
                    matchedCount++
                }
            }
        }

        if (matchedCount >= 3) {
            return ColumnIndices(
                idxFarmerName = idxFarmerName,
                idxFarmerId = idxFarmerId,
                idxVillage = idxVillage,
                idxPlantedArea = idxPlantedArea,
                idxPlantingDistance = idxPlantingDistance,
                idxSurveyDate = idxSurveyDate,
                idxSurveyor = idxSurveyor,
                idxGardenType = idxGardenType,
                idxTotalTrees = idxTotalTrees,
                idxTargetProd = idxTargetProd,
                idxSampleTrees = idxSampleTrees,
                idxSampleDetails = idxSampleDetails
            )
        }
        return null
    }

    private fun detectDelimiter(lines: List<String>): Char {
        var commaCount = 0
        var semicolonCount = 0
        var tabCount = 0

        for (line in lines) {
            var inQuotes = false
            for (c in line) {
                if (c == '\"') {
                    inQuotes = !inQuotes
                } else if (!inQuotes) {
                    when (c) {
                        ',' -> commaCount++
                        ';' -> semicolonCount++
                        '\t' -> tabCount++
                    }
                }
            }
        }

        return when {
            semicolonCount > commaCount && semicolonCount >= tabCount -> ';'
            tabCount > commaCount && tabCount > semicolonCount -> '\t'
            else -> ','
        }
    }

    private fun parseCsvLineWithDelimiter(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Robust integer parsing for Total Trees (supports 1100, 1.200, 1,350, 1500.0, "1600 pohon", etc.)
     */
    private fun parseRobustTotalTrees(raw: String?, defaultVal: Int = 1000): Int {
        if (raw.isNullOrBlank()) return defaultVal
        val cleaned = raw
            .replace("Pohon", "", ignoreCase = true)
            .replace("pohon", "", ignoreCase = true)
            .replace("\u00A0", "")
            .replace("\"", "")
            .replace("'", "")
            .trim()

        // 1. Direct Int
        cleaned.toIntOrNull()?.let { if (it > 0) return it }

        // 2. Thousand separators like "1.100", "1,200", "1.350", "2.000", "10.000"
        if (cleaned.matches(Regex("""^\d{1,4}[.,]\d{3}$"""))) {
            val noSep = cleaned.replace(".", "").replace(",", "")
            noSep.toIntOrNull()?.let { if (it > 0) return it }
        }

        // 3. Decimal float representation from Excel like "1100.0" or "1250,00"
        val asDouble = cleaned.replace(",", ".").toDoubleOrNull()
        if (asDouble != null && asDouble > 0) {
            return Math.round(asDouble).toInt()
        }

        // 4. Only digits fallback
        val digitsOnly = cleaned.filter { it.isDigit() }
        if (digitsOnly.isNotEmpty()) {
            digitsOnly.toIntOrNull()?.let { if (it > 0) return it }
        }

        return defaultVal
    }

    private fun parseRobustInt(raw: String?, defaultVal: Int = 0): Int {
        if (raw.isNullOrBlank()) return defaultVal
        val cleaned = raw
            .replace("Pohon", "", ignoreCase = true)
            .replace("Buah", "", ignoreCase = true)
            .replace("\u00A0", "")
            .replace("\"", "")
            .trim()

        cleaned.toIntOrNull()?.let { return it }

        if (cleaned.matches(Regex("""^\d{1,4}[.,]\d{3}$"""))) {
            val noSep = cleaned.replace(".", "").replace(",", "")
            noSep.toIntOrNull()?.let { return it }
        }

        val asDouble = cleaned.replace(",", ".").toDoubleOrNull()
        if (asDouble != null) {
            return Math.round(asDouble).toInt()
        }

        val digitsOnly = cleaned.filter { it.isDigit() }
        if (digitsOnly.isNotEmpty()) {
            digitsOnly.toIntOrNull()?.let { return it }
        }

        return defaultVal
    }

    private fun parseRobustDouble(raw: String?, defaultVal: Double = 0.0): Double {
        if (raw.isNullOrBlank()) return defaultVal
        val cleaned = raw
            .replace("Ha", "", ignoreCase = true)
            .replace("Kg", "", ignoreCase = true)
            .replace("Pohon", "", ignoreCase = true)
            .replace("\u00A0", "")
            .replace("\"", "")
            .trim()

        // Format "1.250,50" -> Indonesian thousand dot + comma decimal
        if (cleaned.contains(".") && cleaned.contains(",")) {
            if (cleaned.lastIndexOf(",") > cleaned.lastIndexOf(".")) {
                val norm = cleaned.replace(".", "").replace(",", ".")
                norm.toDoubleOrNull()?.let { if (it > 0) return it }
            } else {
                val norm = cleaned.replace(",", "")
                norm.toDoubleOrNull()?.let { if (it > 0) return it }
            }
        }

        // Format "1.250" or "1,250"
        if (cleaned.matches(Regex("""^\d{1,4}[.,]\d{3}$"""))) {
            val noSep = cleaned.replace(".", "").replace(",", "")
            noSep.toDoubleOrNull()?.let { if (it > 0) return it }
        }

        val normalized = cleaned.replace(",", ".")
        normalized.toDoubleOrNull()?.let { if (it > 0) return it }

        return defaultVal
    }
}

