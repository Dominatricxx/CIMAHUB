package com.example.cimahub.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.data.repository.MedicalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MedicalViewModel : ViewModel() {
    private val _cases = MutableStateFlow(MedicalRepository.getCases())
    val cases: StateFlow<List<ClinicalCase>> = _cases.asStateFlow()

    private val _folders = MutableStateFlow(MedicalRepository.getFolders())
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    private val _selectedCase = MutableStateFlow<ClinicalCase?>(null)
    val selectedCase: StateFlow<ClinicalCase?> = _selectedCase.asStateFlow()

    private val _atlasZoomLevel = MutableStateFlow(1f)
    val atlasZoomLevel: StateFlow<Float> = _atlasZoomLevel.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login() {
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun selectCase(clinicalCase: ClinicalCase?) {
        _selectedCase.value = clinicalCase
    }

    fun setZoomLevel(level: Float) {
        _atlasZoomLevel.value = level.coerceIn(1f, 5f)
    }

    fun onHotspotClicked(hotspotId: String) {
        val clinicalCase = MedicalRepository.getCases().find { it.hotspotId == hotspotId }
        if (clinicalCase != null) {
            _selectedCase.value = clinicalCase
        }
    }
}
