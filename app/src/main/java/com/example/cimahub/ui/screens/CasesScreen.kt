package com.example.cimahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.ui.viewmodel.MedicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasesScreen(
    viewModel: MedicalViewModel
) {
    val cases by viewModel.cases.collectAsState()
    val folders by viewModel.folders.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CI", color = Color(0xFF1A8F5A), fontWeight = FontWeight.Bold)
                        Text("MED", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            folders.forEach { folderName ->
                item {
                    FolderHeader(folderName, cases.count { it.folder == folderName })
                }
                items(cases.filter { it.folder == folderName }) { clinicalCase ->
                    CaseCard(clinicalCase) {
                        viewModel.selectCase(clinicalCase)
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
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
fun CaseCard(clinicalCase: ClinicalCase, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                    fontWeight = FontWeight.Bold
                )
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
