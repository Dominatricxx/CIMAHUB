package com.example.cimahub.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.ui.viewmodel.MedicalViewModel
import com.example.cimahub.ui.viewmodel.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasesScreen(
    viewModel: MedicalViewModel,
    onMenuClick: () -> Unit = {},
    onAddCase: () -> Unit,
    onEditCase: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cases by viewModel.cases.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var focusedCase by remember { mutableStateOf<ClinicalCase?>(null) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }

    // Estado para búsqueda por entrada de texto
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filtrar casos por término de búsqueda si está activo
    val displayedCases = remember(cases, searchQuery) {
        if (searchQuery.isBlank()) {
            cases
        } else {
            cases.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.summary.contains(searchQuery, ignoreCase = true) ||
                it.folder.contains(searchQuery, ignoreCase = true) ||
                it.anamnesis.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Estado para el arrastre 3D y superposición de tarjetas
    var draggingCaseId by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(density) { 72.dp.toPx() }
    val swapThresholdPx = itemHeightPx * 0.55f

    // Diálogo para mover caso a una carpeta específica
    if (showFolderDialog && focusedCase != null) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Mover de Ubicación / Carpeta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecciona una carpeta existente o crea una nueva para '${focusedCase?.title}':")
                    
                    folders.filter { it.isNotBlank() }.forEach { folderName ->
                        OutlinedButton(
                            onClick = {
                                viewModel.moveCaseToFolder(focusedCase?.id, folderName)
                                Toast.makeText(context, "Caso movido a '$folderName'", Toast.LENGTH_SHORT).show()
                                showFolderDialog = false
                                focusedCase = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (focusedCase?.folder == folderName) Color(0xFF1A8F5A).copy(alpha = 0.1f) else Color.Transparent
                            )
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF1A8F5A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(folderName, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFolderNameInput,
                        onValueChange = { newFolderNameInput = it },
                        label = { Text("Nueva Carpeta...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderNameInput.isNotBlank()) {
                            viewModel.moveCaseToFolder(focusedCase?.id, newFolderNameInput.trim())
                            Toast.makeText(context, "Caso movido a '${newFolderNameInput.trim()}'", Toast.LENGTH_SHORT).show()
                            newFolderNameInput = ""
                            showFolderDialog = false
                            focusedCase = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A8F5A))
                ) {
                    Text("Mover")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            // CABECERA PERSISTENTE UNIVERSAL
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir menú lateral izquierdo",
                            tint = Color(0xFF1A8F5A)
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CI", color = Color(0xFF1A8F5A), fontWeight = FontWeight.Black, fontSize = 22.sp)
                            Text("MED", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar casos por texto",
                            tint = Color(0xFF1A8F5A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            if (userRole == UserRole.Teacher && focusedCase == null && draggingCaseId == null) {
                FloatingActionButton(
                    onClick = { onAddCase() },
                    containerColor = Color(0xFF1A8F5A),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Caso")
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // LISTA PRINCIPAL DE CASOS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (focusedCase != null && draggingCaseId == null) Modifier.blur(16.dp) else Modifier)
            ) {
                // Barra desplegable de entrada de texto para búsqueda
                AnimatedVisibility(visible = isSearchActive) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar caso por nombre, resumen o carpeta...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF1A8F5A))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar texto", tint = Color.Gray)
                                    }
                                } else {
                                    IconButton(onClick = { isSearchActive = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A8F5A),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }

                if (pendingCount > 0) {
                    Surface(
                        color = Color(0xFFD1ECF1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.syncPendingCases() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFF0C5460),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tienes $pendingCount caso(s) en espera de subir. Toca para sincronizar.",
                                color = Color(0xFF0C5460),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isOffline) {
                    Surface(
                        color = Color(0xFFFFF3CD),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Modo Sin Conexión - Usando datos guardados localmente",
                                color = Color(0xFF856404),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF1A8F5A)
                        )
                    } else if (error != null && displayedCases.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(error!!, color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.refreshCases() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A8F5A))
                            ) {
                                Text("Reintentar")
                            }
                        }
                    } else if (displayedCases.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No se encontraron casos para '$searchQuery'" else "No se encontraron casos",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { searchQuery = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A8F5A))
                                ) {
                                    Text("Limpiar búsqueda")
                                }
                            }
                        }
                    } else {
                        val activeFolders = displayedCases.map { it.folder }.distinct().sortedBy { if (it == "Sin carpeta") 1 else 0 }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            activeFolders.forEach { folderName ->
                                item {
                                    FolderHeader(folderName, displayedCases.count { it.folder == folderName })
                                }
                                items(displayedCases.filter { it.folder == folderName }, key = { it.id ?: it.title }) { clinicalCase ->
                                    val isBeingDragged = draggingCaseId == clinicalCase.id

                                    CaseCard(
                                        clinicalCase = clinicalCase,
                                        isBeingDragged = isBeingDragged,
                                        dragOffsetY = if (isBeingDragged) dragOffsetY else 0f,
                                        onTap = { viewModel.selectCase(clinicalCase) },
                                        onLongPressMenu = {
                                            if (draggingCaseId == null) {
                                                focusedCase = clinicalCase
                                            }
                                        },
                                        onDragStarted = {
                                            focusedCase = null
                                            draggingCaseId = clinicalCase.id
                                            dragOffsetY = 0f
                                        },
                                        onDragging = { deltaY ->
                                            if (focusedCase != null) {
                                                focusedCase = null
                                            }
                                            draggingCaseId = clinicalCase.id
                                            dragOffsetY += deltaY

                                            val currentList = cases.toMutableList()
                                            val index = currentList.indexOfFirst { it.id == clinicalCase.id }

                                            if (dragOffsetY > swapThresholdPx && index < currentList.size - 1) {
                                                val temp = currentList[index]
                                                currentList[index] = currentList[index + 1]
                                                currentList[index + 1] = temp
                                                viewModel.reorderCases(currentList)
                                                dragOffsetY -= itemHeightPx
                                            } else if (dragOffsetY < -swapThresholdPx && index > 0) {
                                                val temp = currentList[index]
                                                currentList[index] = currentList[index - 1]
                                                currentList[index - 1] = temp
                                                viewModel.reorderCases(currentList)
                                                dragOffsetY += itemHeightPx
                                            }
                                        },
                                        onDragEnded = {
                                            draggingCaseId = null
                                            dragOffsetY = 0f
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(20.dp)) }
                            }
                        }
                    }
                }
            }

            // --- SUBMENÚ DE ACCIONES AL MANTENER PRESIONADO (SIN MOVER EL DEDO) ---
            if (focusedCase != null && draggingCaseId == null) {
                val targetCase = focusedCase!!
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable { focusedCase = null }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 12.dp,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Archivar
                                IconButton(onClick = {
                                    viewModel.moveCaseToFolder(targetCase.id, "Archivados")
                                    Toast.makeText(context, "Caso archivado en 'Archivados'", Toast.LENGTH_SHORT).show()
                                    focusedCase = null
                                }) {
                                    Icon(Icons.Default.Archive, contentDescription = "Archivar", tint = Color(0xFF1A8F5A))
                                }

                                // 2. Añadir a Favoritos
                                val isFavorite = targetCase.folder == "Favoritos"
                                IconButton(onClick = {
                                    val newFolder = if (isFavorite) "Sin carpeta" else "Favoritos"
                                    viewModel.moveCaseToFolder(targetCase.id, newFolder)
                                    Toast.makeText(context, if (!isFavorite) "Añadido a Favoritos" else "Removido de Favoritos", Toast.LENGTH_SHORT).show()
                                    focusedCase = null
                                }) {
                                    Icon(
                                        if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favoritos",
                                        tint = if (isFavorite) Color(0xFFFFB300) else Color.DarkGray
                                    )
                                }

                                // 3. Mover a Carpeta
                                IconButton(onClick = {
                                    showFolderDialog = true
                                }) {
                                    Icon(Icons.Default.DriveFileMove, contentDescription = "Mover a carpeta", tint = Color(0xFF2F50FF))
                                }

                                // 4. Modificar / Editar Caso (Rol Docente)
                                if (userRole == UserRole.Teacher) {
                                    IconButton(onClick = {
                                        val targetId = targetCase.id
                                        focusedCase = null
                                        if (targetId != null) {
                                            onEditCase(targetId)
                                        }
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modificar caso", tint = Color(0xFF1A8F5A))
                                    }
                                }

                                // Botón para cerrar
                                IconButton(onClick = { focusedCase = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, Color(0xFF1A8F5A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = targetCase.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A8F5A)
                                )
                                Text(
                                    text = "Ubicación actual: ${targetCase.folder}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )

                                if (targetCase.summary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = targetCase.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Arrastra la tarjeta para reordenarla o toca fuera para cerrar",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FolderHeader(name: String, count: Int) {
    val gradient = if (name == "ECOE") {
        Brush.horizontalGradient(listOf(Color(0xFFE8F4EF), Color(0xFFDCE8FF)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFF4F4F4), Color(0xFFEAEAEA)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(gradient, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (name == "ECOE") Icons.Default.Folder else Icons.Outlined.FolderOpen, 
                contentDescription = null, 
                tint = if (name == "ECOE") Color(0xFF2F50FF) else Color(0xFFAAAAAA),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF222222),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count caso(s)", 
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun CaseCard(
    clinicalCase: ClinicalCase,
    isBeingDragged: Boolean = false,
    dragOffsetY: Float = 0f,
    onTap: () -> Unit,
    onLongPressMenu: () -> Unit,
    onDragStarted: () -> Unit,
    onDragging: (Float) -> Unit,
    onDragEnded: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .zIndex(if (isBeingDragged) 10f else 1f)
            .graphicsLayer {
                translationY = if (isBeingDragged) dragOffsetY else 0f
                scaleX = if (isBeingDragged) 1.04f else 1.0f
                scaleY = if (isBeingDragged) 1.04f else 1.0f
            }
            .pointerInput(clinicalCase) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPressMenu() }
                )
            }
            .pointerInput(clinicalCase) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStarted() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragging(dragAmount.y)
                    },
                    onDragEnd = { onDragEnded() },
                    onDragCancel = { onDragEnded() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        border = if (isBeingDragged) BorderStroke(2.dp, Color(0xFF1A8F5A)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBeingDragged) 16.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clinicalCase.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = if (isBeingDragged) Color(0xFF1A8F5A) else Color.Unspecified
                )
                if (clinicalCase.summary.isNotBlank()) {
                    Text(
                        text = clinicalCase.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Icon(
                Icons.Default.PlayCircle, 
                contentDescription = "Ver detalles",
                tint = Color(0xFF1A8F5A),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
