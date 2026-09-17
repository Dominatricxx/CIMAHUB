package com.example.cimahub

import android.os.Bundle
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cimahub.ui.components.CaseDetailModal
import com.example.cimahub.ui.components.UabcHyperDynamicBackground
import com.example.cimahub.ui.navigation.NavGraph
import com.example.cimahub.ui.navigation.Screen
import com.example.cimahub.ui.theme.CIMAHUBTheme
import com.example.cimahub.ui.viewmodel.MedicalViewModel
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CaseDetailModal(
        selectedCase = selectedCase,
        onDismiss = { viewModel.selectCase(null) },
        onStartSimulation = { caseId ->
            viewModel.selectCase(null)
            navController.navigate(Screen.Simulation.createRoute(caseId))
        }
    )

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
