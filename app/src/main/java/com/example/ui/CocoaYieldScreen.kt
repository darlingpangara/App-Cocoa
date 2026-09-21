package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TreeSampleHelper
import com.example.ui.components.AddSurveyDialog
import com.example.ui.components.DatabaseHistorySheet
import com.example.ui.components.EditSurveyDialog
import com.example.ui.components.FarmerInfoCard
import com.example.ui.components.StagesOverviewCard
import com.example.ui.components.TreeSamplesEditorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocoaYieldScreen(
    viewModel: CocoaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSurvey by viewModel.currentSurvey.collectAsStateWithLifecycle()
    val allSurveys by viewModel.allSurveys.collectAsStateWithLifecycle()
    val filteredSurveys by viewModel.filteredSurveys.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedVillage by viewModel.selectedVillage.collectAsStateWithLifecycle()
    val allVillages by viewModel.allVillages.collectAsStateWithLifecycle()

    val showEditDialog by viewModel.showEditDialog.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val showTreeDetailDialog by viewModel.showTreeDetailDialog.collectAsStateWithLifecycle()
    val showDatabaseSheet by viewModel.showDatabaseSheet.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showTopMenu by remember { mutableStateOf(false) }
    var showDeleteDatabaseConfirmation by remember { mutableStateOf(false) }

    // File picker for Excel/CSV import
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importFromUri(context, uri)
        }
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("cocoa_yield_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forest,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Estimasi Produksi Kakao",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Yield Estimation & Analisis Kebun",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Tombol Baris Tiga pada pojok kanan atas (Menu Overflow)
                    Box {
                        IconButton(
                            onClick = { showTopMenu = !showTopMenu },
                            modifier = Modifier.testTag("topbar_overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Opsi",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Daftar Data Petani") },
                                    onClick = {
                                        showTopMenu = false
                                        viewModel.openDatabaseSheet()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.testTag("menu_view_database")
                                )
                                DropdownMenuItem(
                                    text = { Text("Ekspor Database (Excel/CSV)") },
                                    onClick = {
                                        showTopMenu = false
                                        viewModel.exportAllToExcel(context)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.testTag("menu_export_database")
                                )
                                DropdownMenuItem(
                                    text = { Text("Impor Database (Excel/CSV)") },
                                    onClick = {
                                        showTopMenu = false
                                        importFileLauncher.launch("*/*")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.testTag("menu_import_database")
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Hapus Database",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        showDeleteDatabaseConfirmation = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    modifier = Modifier.testTag("menu_delete_database")
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_survey")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Data Petani")
            }
        }
    ) { innerPadding ->
        val survey = currentSurvey

        if (survey == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forest,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Belum Ada Data Petani",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Database survey kebun kakao masih kosong.\nMulai dengan menambahkan data petani baru atau impor dari file Excel/CSV.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.openAddDialog() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                            .testTag("empty_state_add_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Data Petani", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FilledTonalButton(
                        onClick = { importFileLauncher.launch("*/*") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                            .testTag("empty_state_import_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Impor File Excel / CSV", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            val currentIndex = allSurveys.indexOfFirst { it.id == survey.id }
            val totalCount = allSurveys.size

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ================= PENCARIAN & FILTER DESA =================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_and_filter_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Kolom Pencarian
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    "Cari nama atau ID petani...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.setSearchQuery("") },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("clear_search_query_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Hapus Teks",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("main_search_text_field")
                        )

                        // 2. Filter Berdasarkan Desa
                        if (allVillages.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Filter Desa",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (selectedVillage != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• $selectedVillage",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // "Semua Desa" Chip
                                    item {
                                        val isAllSelected = selectedVillage == null
                                        FilterChip(
                                            selected = isAllSelected,
                                            onClick = { viewModel.setSelectedVillage(null) },
                                            label = {
                                                Text(
                                                    text = "Semua Desa (${allSurveys.size})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            leadingIcon = if (isAllSelected) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.testTag("filter_chip_all_villages")
                                        )
                                    }

                                    // Village chips
                                    items(allVillages) { villageName ->
                                        val isSelected = selectedVillage.equals(villageName, ignoreCase = true)
                                        val countInVillage = allSurveys.count { it.village.equals(villageName, ignoreCase = true) }
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                if (isSelected) {
                                                    viewModel.setSelectedVillage(null)
                                                } else {
                                                    viewModel.setSelectedVillage(villageName)
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = "$villageName ($countInVillage)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            leadingIcon = if (isSelected) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.testTag("filter_chip_village_$villageName")
                                        )
                                    }
                                }
                            }
                        }

                        // 3. List Data Petani yang Aktif Berdasarkan Filter & Pencarian
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pilih Data Petani (${filteredSurveys.size} Ditemukan)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (searchQuery.isNotEmpty() || selectedVillage != null) {
                                    TextButton(
                                        onClick = {
                                            viewModel.setSearchQuery("")
                                            viewModel.setSelectedVillage(null)
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(
                                            "Reset Filter",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            if (filteredSurveys.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Tidak ada data petani yang cocok dengan filter atau pencarian saat ini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(filteredSurveys) { item ->
                                        val isSelected = item.id == survey.id
                                        Surface(
                                            modifier = Modifier
                                                .clickable { viewModel.selectSurvey(item) }
                                                .testTag("farmer_chip_item_${item.id}"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isSelected) 1.5.dp else 1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Column {
                                                    Text(
                                                        text = item.farmerName,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = item.farmerId,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 10.sp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        if (item.village.isNotBlank()) {
                                                            Text(
                                                                text = " • ${item.village}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ================= TOP SECTION: 8 PETANI INFORMATIONS (4 KIRI, 4 KANAN) =================
                FarmerInfoCard(survey = survey)

                // Main Quick Controls (Edit Button & Jumlah Produksi GMR)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // EDIT BUTTON (Required by user prompt: "buatkan tombol edit untuk semua data kecuali nama, ID, dan luas lahan")
                    Button(
                        onClick = { viewModel.openEditDialog() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_data_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Data Kebun", style = MaterialTheme.typography.labelMedium)
                    }

                    // JUMLAH PRODUKSI GMR (Satuan Kg) - Acuan Jumlah Buah Tahap 2
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { viewModel.openEditDialog() }
                            .testTag("gmr_production_info_card"),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PRODUKSI GMR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%,.0f", survey.targetProductionKg)} Kg",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // ================= 5 STAGES YIELD ESTIMATION SECTION (TAHAP 1 - 5) =================
                StagesOverviewCard(
                    survey = survey,
                    onEditStage1 = { newCount -> viewModel.updateSampleTreesCount(newCount) },
                    onRegenerateStage2 = { viewModel.regenerateSamplePods() },
                    onOpenTreeDetail = { viewModel.openTreeDetailDialog() },
                    onQuickAdjustTreePod = { code, newPods ->
                        viewModel.updateSingleTreePodCount(code, newPods)
                    }
                )

                Spacer(modifier = Modifier.height(60.dp)) // Extra space for FAB
            }
        }

        // Dialogs
        if (showEditDialog && survey != null) {
            EditSurveyDialog(
                survey = survey,
                onDismiss = { viewModel.closeEditDialog() },
                onSave = { village, pDist, sDate, sName, gType, totalTrees, sampleTrees, targetProd ->
                    viewModel.saveEditedSurvey(
                        village = village,
                        plantingDistance = pDist,
                        surveyDate = sDate,
                        surveyorName = sName,
                        gardenType = gType,
                        totalTrees = totalTrees,
                        sampleTreesCount = sampleTrees,
                        targetProductionKg = targetProd
                    )
                }
            )
        }

        if (showAddDialog) {
            AddSurveyDialog(
                onDismiss = { viewModel.closeAddDialog() },
                onConfirm = { name, id, village, area, dist, date, surveyor, type, totalTrees, sampleTrees, targetProd ->
                    viewModel.createNewSurvey(
                        farmerName = name,
                        farmerId = id,
                        village = village,
                        plantedAreaHa = area,
                        plantingDistance = dist,
                        surveyDate = date,
                        surveyorName = surveyor,
                        gardenType = type,
                        totalTrees = totalTrees,
                        sampleTreesCount = sampleTrees,
                        targetProductionKg = targetProd
                    )
                }
            )
        }

        if (showTreeDetailDialog && survey != null) {
            TreeSamplesEditorDialog(
                survey = survey,
                onDismiss = { viewModel.closeTreeDetailDialog() },
                onSaveSamples = { updatedSamples ->
                    val updated = survey.copy(
                        treeSamplesJson = TreeSampleHelper.toJson(updatedSamples),
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateCurrentSurvey(updated)
                    viewModel.closeTreeDetailDialog()
                },
                onRegenerate = {
                    viewModel.regenerateSamplePods()
                }
            )
        }

        if (showDatabaseSheet) {
            DatabaseHistorySheet(
                surveys = filteredSurveys,
                currentSurveyId = currentSurvey?.id,
                searchQuery = searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedVillage = selectedVillage,
                allVillages = allVillages,
                onSelectVillage = { viewModel.setSelectedVillage(it) },
                onSelectSurvey = { viewModel.selectSurvey(it) },
                onAddNewSurvey = {
                    viewModel.closeDatabaseSheet()
                    viewModel.openAddDialog()
                },
                onDeleteSurvey = { viewModel.deleteSurvey(it) },
                onDismiss = { viewModel.closeDatabaseSheet() }
            )
        }

        // Dialog Konfirmasi Hapus Seluruh Database
        if (showDeleteDatabaseConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteDatabaseConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Konfirmasi Hapus Database",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus seluruh data survey kebun kakao dari database? Tindakan ini akan mengosongkan database dan tidak dapat dibatalkan.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAllSurveys()
                            showDeleteDatabaseConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.testTag("confirm_delete_database_button")
                    ) {
                        Text("Hapus Semua Data")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDatabaseConfirmation = false },
                        modifier = Modifier.testTag("cancel_delete_database_button")
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
