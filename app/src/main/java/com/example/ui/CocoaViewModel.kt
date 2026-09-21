package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CocoaDatabase
import com.example.data.CocoaRepository
import com.example.model.CocoaSurvey
import com.example.model.TreeSample
import com.example.model.TreeSampleHelper
import com.example.util.ExcelExporterImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CocoaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CocoaRepository
    val allSurveys: StateFlow<List<CocoaSurvey>>

    private val _currentSurvey = MutableStateFlow<CocoaSurvey?>(null)
    val currentSurvey: StateFlow<CocoaSurvey?> = _currentSurvey.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedVillage = MutableStateFlow<String?>(null)
    val selectedVillage: StateFlow<String?> = _selectedVillage.asStateFlow()

    private val _allVillages = MutableStateFlow<List<String>>(emptyList())
    val allVillages: StateFlow<List<String>> = _allVillages.asStateFlow()

    private val _filteredSurveys = MutableStateFlow<List<CocoaSurvey>>(emptyList())
    val filteredSurveys: StateFlow<List<CocoaSurvey>> = _filteredSurveys.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Dialog & UI Visibility states
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showTreeDetailDialog = MutableStateFlow(false)
    val showTreeDetailDialog: StateFlow<Boolean> = _showTreeDetailDialog.asStateFlow()

    private val _showDatabaseSheet = MutableStateFlow(false)
    val showDatabaseSheet: StateFlow<Boolean> = _showDatabaseSheet.asStateFlow()

    init {
        val database = CocoaDatabase.getDatabase(application, viewModelScope)
        repository = CocoaRepository(database.cocoaSurveyDao())
        
        // Ensure default 3 sample surveys are purged from database
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSampleSurveys()
        }

        allSurveys = repository.allSurveys.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Observe surveys to set initial active survey and extract villages
        viewModelScope.launch {
            allSurveys.collect { list ->
                // Update villages list
                val villages = list.map { it.village.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
                _allVillages.value = villages

                if (list.isNotEmpty()) {
                    val current = _currentSurvey.value
                    if (current == null || list.none { it.id == current.id }) {
                        _currentSurvey.value = list.first()
                    } else {
                        // Keep current survey updated with DB changes
                        val updated = list.find { it.id == current.id }
                        if (updated != null) {
                            _currentSurvey.value = updated
                        }
                    }
                } else {
                    _currentSurvey.value = null
                }
            }
        }

        // Combine search query, selectedVillage filter, and allSurveys
        viewModelScope.launch {
            combine(allSurveys, _searchQuery, _selectedVillage) { list, query, villageFilter ->
                var filtered = list
                
                // 1. Filter by Desa if selected
                if (!villageFilter.isNullOrBlank()) {
                    filtered = filtered.filter { it.village.equals(villageFilter, ignoreCase = true) }
                }

                // 2. Filter by search query (Nama Petani & ID Petani)
                if (query.isNotBlank()) {
                    val q = query.trim().lowercase(Locale.getDefault())
                    filtered = filtered.filter {
                        it.farmerName.lowercase(Locale.getDefault()).contains(q) ||
                        it.farmerId.lowercase(Locale.getDefault()).contains(q)
                    }
                }

                filtered
            }.collect { filtered ->
                _filteredSurveys.value = filtered
                val current = _currentSurvey.value
                if (filtered.isNotEmpty()) {
                    if (current == null || filtered.none { it.id == current.id }) {
                        _currentSurvey.value = filtered.first()
                    }
                }
            }
        }
    }

    fun selectSurvey(survey: CocoaSurvey) {
        _currentSurvey.value = survey
        _showDatabaseSheet.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedVillage(village: String?) {
        _selectedVillage.value = village
    }

    fun openEditDialog() {
        _showEditDialog.value = true
    }

    fun closeEditDialog() {
        _showEditDialog.value = false
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun openTreeDetailDialog() {
        _showTreeDetailDialog.value = true
    }

    fun closeTreeDetailDialog() {
        _showTreeDetailDialog.value = false
    }

    fun openDatabaseSheet() {
        _showDatabaseSheet.value = true
    }

    fun closeDatabaseSheet() {
        _showDatabaseSheet.value = false
    }

    /**
     * Update sample trees count (Tahap 1)
     */
    fun updateSampleTreesCount(count: Int) {
        val curr = _currentSurvey.value ?: return
        val validCount = count.coerceIn(1, 26)
        val newSamples = TreeSampleHelper.generateDynamicSamples(
            sampleCount = validCount,
            targetProductionKg = curr.targetProductionKg,
            totalTrees = curr.totalTrees
        )
        val updated = curr.copy(
            sampleTreesCount = validCount,
            treeSamplesJson = TreeSampleHelper.toJson(newSamples),
            updatedAt = System.currentTimeMillis()
        )
        _currentSurvey.value = updated
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(updated)
            _snackbarMessage.emit("Tahap 1 diperbarui: $validCount pohon sampel")
        }
    }

    /**
     * Regenerate dynamic sample pods based on user's Excel formula:
     * =ROUNDDOWN((jumlah produksi / (0.07 * total jumlah pohon)) * jumlah pohon sampel)
     */
    fun regenerateSamplePods() {
        val curr = _currentSurvey.value ?: return
        val newSamples = TreeSampleHelper.generateDynamicSamples(
            sampleCount = curr.sampleTreesCount,
            targetProductionKg = curr.targetProductionKg,
            totalTrees = curr.totalTrees
        )
        val updated = curr.copy(
            treeSamplesJson = TreeSampleHelper.toJson(newSamples),
            updatedAt = System.currentTimeMillis()
        )
        _currentSurvey.value = updated
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(updated)
            _snackbarMessage.emit("Variasi buah sampel (Tahap 2) berhasil digenerate")
        }
    }

    /**
     * Update single tree sample pod count (e.g. P.A = 12 pods)
     */
    fun updateSingleTreePodCount(code: String, newPods: Int) {
        val curr = _currentSurvey.value ?: return
        val samples = curr.getSampleTrees().toMutableList()
        val index = samples.indexOfFirst { it.code == code }
        if (index != -1) {
            val clampedPods = newPods.coerceIn(0, 50)
            samples[index] = TreeSample(code = code, pods = clampedPods)
            val updated = curr.copy(
                treeSamplesJson = TreeSampleHelper.toJson(samples),
                updatedAt = System.currentTimeMillis()
            )
            _currentSurvey.value = updated
            viewModelScope.launch(Dispatchers.IO) {
                repository.update(updated)
            }
        }
    }

    fun updateCurrentSurvey(survey: CocoaSurvey) {
        _currentSurvey.value = survey
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(survey)
            _snackbarMessage.emit("Data sampel buah berhasil diperbarui")
        }
    }

    /**
     * Save edited survey (Where Nama, ID, and Luas Lahan are locked/unchanged)
     */
    fun saveEditedSurvey(
        village: String,
        plantingDistance: String,
        surveyDate: String,
        surveyorName: String,
        gardenType: String,
        totalTrees: Int,
        sampleTreesCount: Int,
        targetProductionKg: Double
    ) {
        val curr = _currentSurvey.value ?: return
        
        // If sampleTreesCount changed or samples need refresh
        val currentSamples = curr.getSampleTrees()
        val updatedSamples = if (currentSamples.size != sampleTreesCount) {
            TreeSampleHelper.generateDynamicSamples(sampleTreesCount, targetProductionKg, totalTrees)
        } else {
            currentSamples
        }

        val updated = curr.copy(
            // Nama, ID, and plantedAreaHa are strictly preserved
            farmerName = curr.farmerName,
            farmerId = curr.farmerId,
            village = village.trim(),
            plantedAreaHa = curr.plantedAreaHa,
            
            // Editable fields
            plantingDistance = plantingDistance,
            surveyDate = surveyDate,
            surveyorName = surveyorName,
            gardenType = gardenType,
            totalTrees = totalTrees,
            sampleTreesCount = sampleTreesCount,
            targetProductionKg = targetProductionKg,
            treeSamplesJson = TreeSampleHelper.toJson(updatedSamples),
            updatedAt = System.currentTimeMillis()
        )

        _currentSurvey.value = updated
        _showEditDialog.value = false

        viewModelScope.launch(Dispatchers.IO) {
            repository.update(updated)
            _snackbarMessage.emit("Data survey berhasil diperbarui!")
        }
    }

    /**
     * Create new survey
     */
    fun createNewSurvey(
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
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val samples = TreeSampleHelper.generateDynamicSamples(
                sampleCount = sampleTreesCount,
                targetProductionKg = targetProductionKg,
                totalTrees = totalTrees
            )

            val newSurvey = CocoaSurvey(
                farmerName = farmerName.ifBlank { "Petani Baru" },
                farmerId = farmerId.ifBlank { "KT-${System.currentTimeMillis() % 10000}" },
                village = village.trim(),
                plantedAreaHa = plantedAreaHa.coerceAtLeast(0.1),
                plantingDistance = plantingDistance.ifBlank { "3m x 3m" },
                surveyDate = surveyDate.ifBlank { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) },
                surveyorName = surveyorName.ifBlank { "Surveyor" },
                gardenType = gardenType.ifBlank { "Kakao Sambung Pucuk" },
                totalTrees = totalTrees.coerceAtLeast(10),
                sampleTreesCount = sampleTreesCount.coerceIn(1, 26),
                targetProductionKg = targetProductionKg.coerceAtLeast(10.0),
                treeSamplesJson = TreeSampleHelper.toJson(samples)
            )

            val newId = repository.insert(newSurvey)
            val created = newSurvey.copy(id = newId)
            _currentSurvey.value = created
            _showAddDialog.value = false
            _snackbarMessage.emit("Data survey baru berhasil ditambahkan!")
        }
    }

    /**
     * Delete survey
     */
    fun deleteSurvey(survey: CocoaSurvey) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(survey)
            if (_currentSurvey.value?.id == survey.id) {
                val remaining = allSurveys.value.filter { it.id != survey.id }
                _currentSurvey.value = remaining.firstOrNull()
            }
            _snackbarMessage.emit("Data survey '${survey.farmerName}' dihapus")
        }
    }

    /**
     * Delete all surveys in database (Clear Database)
     */
    fun deleteAllSurveys() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            _currentSurvey.value = null
            _snackbarMessage.emit("Seluruh data survey di database telah berhasil dihapus")
        }
    }

    /**
     * Export all database to Excel table (CSV)
     */
    fun exportAllToExcel(context: Context) {
        val list = allSurveys.value
        if (list.isEmpty()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Database masih kosong, tidak ada data untuk diekspor")
            }
            return
        }
        ExcelExporterImporter.exportAndShareCsv(context, list, "Database_Estimasi_Kakao_Lengkap")
    }

    /**
     * Export single active survey to Excel table
     */
    fun exportCurrentSurveyToExcel(context: Context) {
        val curr = _currentSurvey.value ?: return
        val sanitized = curr.farmerName.replace(" ", "_")
        ExcelExporterImporter.exportAndShareCsv(context, listOf(curr), "Estimasi_Kakao_$sanitized")
    }

    /**
     * Import CSV from selected URI
     */
    fun importFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val imported = ExcelExporterImporter.parseCsvInputStream(inputStream)
                    if (imported.isNotEmpty()) {
                        repository.insertAll(imported)
                        _currentSurvey.value = imported.first()
                        _snackbarMessage.emit("Berhasil mengimpor ${imported.size} data survey dari Excel!")
                    } else {
                        _snackbarMessage.emit("Format file tidak sesuai atau data kosong")
                    }
                } else {
                    _snackbarMessage.emit("Gagal membuka file yang dipilih")
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("Terjadi kesalahan saat impor: ${e.localizedMessage}")
            }
        }
    }
}
