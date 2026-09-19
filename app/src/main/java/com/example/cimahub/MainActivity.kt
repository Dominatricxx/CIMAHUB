package com.example.cimahub

import android.os.Bundle
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cimahub.data.repository.MedicalRepository
import com.example.cimahub.ui.components.CaseDetailModal
import com.example.cimahub.ui.components.UabcHyperDynamicBackground
import com.example.cimahub.ui.navigation.NavGraph
import com.example.cimahub.ui.navigation.Screen
import com.example.cimahub.ui.theme.CIMAHUBTheme
import com.example.cimahub.ui.viewmodel.MedicalViewModel
import com.example.cimahub.ui.viewmodel.UserRole
import com.example.cimahub.ui.viewmodel.VisionType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CIMAHUBTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MedicalViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedCase by viewModel.selectedCase.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isAccessibilityExpanded by remember { mutableStateOf(false) }
    var isVisionExpanded by remember { mutableStateOf(false) }
    val currentVision by viewModel.visionType.collectAsState()

    // Configuración de la matriz de color según el tipo de visión
    val visionMatrix = remember(currentVision) {
        when (currentVision) {
            VisionType.Protaponia -> floatArrayOf(
                0.567f, 0.433f, 0.000f, 0.000f, 0.000f,
                0.558f, 0.442f, 0.000f, 0.000f, 0.000f,
                0.000f, 0.242f, 0.758f, 0.000f, 0.000f,
                0.000f, 0.000f, 0.000f, 1.000f, 0.000f
            )
            VisionType.Deuteranopia -> floatArrayOf(
                0.625f, 0.375f, 0.000f, 0.000f, 0.000f,
                0.700f, 0.300f, 0.000f, 0.000f, 0.000f,
                0.000f, 0.300f, 0.700f, 0.000f, 0.000f,
                0.000f, 0.000f, 0.000f, 1.000f, 0.000f
            )
            VisionType.Tritanopia -> floatArrayOf(
                0.950f, 0.050f, 0.000f, 0.000f, 0.000f,
                0.000f, 0.433f, 0.567f, 0.000f, 0.000f,
                0.000f, 0.475f, 0.525f, 0.000f, 0.000f,
                0.000f, 0.000f, 0.000f, 1.000f, 0.000f
            )
            VisionType.Acromatopia -> floatArrayOf(
                0.299f, 0.587f, 0.114f, 0.000f, 0.000f,
                0.299f, 0.587f, 0.114f, 0.000f, 0.000f,
                0.299f, 0.587f, 0.114f, 0.000f, 0.000f,
                0.000f, 0.000f, 0.000f, 1.000f, 0.000f
            )
            VisionType.Cataratas -> floatArrayOf(
                0.8f, 0.1f, 0.1f, 0.0f, 40f,
                0.1f, 0.8f, 0.1f, 0.0f, 40f,
                0.1f, 0.1f, 0.8f, 0.0f, 0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
            else -> null
        }
    }

    // Cerrar el drawer al iniciar sesión para evitar que se abra automáticamente
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            drawerState.close()
        }
    }

    CaseDetailModal(
        selectedCase = selectedCase,
        visionMatrix = visionMatrix,
        onDismiss = { viewModel.selectCase(null) },
        onStartSimulation = { caseId ->
            viewModel.selectCase(null)
            navController.navigate(Screen.Simulation.createRoute(caseId))
        }
    )

    // Aplicar el filtro a TODO el contenido, incluyendo el Drawer y el fondo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                if (visionMatrix == null) {
                    drawContent()
                } else {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            colorFilter = ColorMatrixColorFilter(visionMatrix)
                        }
                        canvas.nativeCanvas.saveLayer(null, paint)
                        drawContent()
                        canvas.nativeCanvas.restore()
                    }
                }
            }
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = isLoggedIn && currentRoute != Screen.Login.route,
            drawerContent = {
                if (isLoggedIn && currentRoute != Screen.Login.route) {
                    ModalDrawerSheet(
                        drawerContainerColor = Color.White,
                        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                        modifier = Modifier.width(300.dp).fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        // Header del Drawer
                        Column(modifier = Modifier.padding(24.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("CI", color = Color(0xFF1A8F5A), fontSize = 32.sp, fontWeight = FontWeight.Black)
                                Text("MED", color = Color(0xFFFFB300), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Simulador Médico UABC", color = Color.Gray, fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Surface(
                                color = if (userRole == UserRole.Teacher) Color(0xFF1A8F5A).copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (userRole == UserRole.Teacher) "MODO DOCENTE" else "MODO ESTUDIANTE",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userRole == UserRole.Teacher) Color(0xFF1A8F5A) else Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Inicio / Casos") },
                            selected = currentRoute == Screen.Cases.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Screen.Cases.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFF1A8F5A).copy(alpha = 0.1f),
                                selectedIconColor = Color(0xFF1A8F5A),
                                selectedTextColor = Color(0xFF1A8F5A)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // SECCIÓN DE ACCESIBILIDAD
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Accessibility, contentDescription = null) },
                            label = { Text("Accesibilidad") },
                            selected = false,
                            onClick = { isAccessibilityExpanded = !isAccessibilityExpanded },
                            badge = {
                                Icon(
                                    if (isAccessibilityExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        if (isAccessibilityExpanded) {
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                                label = { Text("Vista") },
                                selected = false,
                                onClick = { isVisionExpanded = !isVisionExpanded },
                                badge = {
                                    Icon(
                                        if (isVisionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.padding(start = 24.dp, end = 12.dp)
                            )

                            if (isVisionExpanded) {
                                val visionOptions = listOf(
                                    VisionType.Normal,
                                    VisionType.Protaponia,
                                    VisionType.Deuteranopia,
                                    VisionType.Tritanopia,
                                    VisionType.Acromatopia,
                                    VisionType.Cataratas
                                )
                                visionOptions.forEach { option ->
                                    NavigationDrawerItem(
                                        label = { Text(option.label) },
                                        selected = currentVision == option,
                                        onClick = { viewModel.setVisionType(option) },
                                        modifier = Modifier.padding(start = 48.dp, end = 12.dp),
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = Color(0xFF1A8F5A).copy(alpha = 0.1f),
                                            selectedTextColor = Color(0xFF1A8F5A)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón de Cerrar Sesión al final
                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red) },
                            label = { Text("Cerrar Sesión", color = Color.Red) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Fondo dinámico global
                UabcHyperDynamicBackground()

                Scaffold(
                    containerColor = Color.Transparent, // Permitir ver el fondo en los bordes
                    bottomBar = {
                        // Solo mostrar BottomBar si está logueado y no en la pantalla de login o simulación
                        if (isLoggedIn && currentRoute != Screen.Login.route && currentRoute?.startsWith("simulation") != true) {
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White.copy(alpha = 0.95f),
                                shadowElevation = 8.dp
                            ) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    tonalElevation = 0.dp
                                ) {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                                        label = { Text("Casos") },
                                        selected = currentRoute == Screen.Cases.route,
                                        onClick = {
                                            navController.navigate(Screen.Cases.route) {
                                                popUpTo(navController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Map, contentDescription = null) },
                                        label = { Text("Atlas") },
                                        selected = currentRoute == Screen.Atlas.route,
                                        onClick = {
                                            navController.navigate(Screen.Atlas.route) {
                                                popUpTo(navController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(if (currentRoute == Screen.Login.route) 0.dp else 12.dp) // Sin bordes en login
                            .fillMaxSize(),
                        shape = RoundedCornerShape(if (currentRoute == Screen.Login.route) 0.dp else 24.dp),
                        color = if (currentRoute == Screen.Login.route) Color.Transparent else Color.White
                    ) {
                        NavGraph(navController = navController, viewModel = viewModel)
                    }
                }

                // Botón de Menú Hamburguesa en la esquina superior izquierda
                if (isLoggedIn && currentRoute != Screen.Login.route) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .padding(top = 48.dp, start = 24.dp)
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Abrir Menú",
                            tint = Color(0xFF1A8F5A)
                        )
                    }
                }
            }
        }
    }
}
