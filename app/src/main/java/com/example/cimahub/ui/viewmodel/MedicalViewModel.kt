package com.example.cimahub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cimahub.BuildConfig
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.data.models.Study
import com.example.cimahub.data.models.VitalSigns
import com.example.cimahub.data.repository.MedicalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class UserRole {
    Student, Teacher
}

enum class VisionType(val label: String) {
    Normal("Normal"),
    Protaponia("Protaponia"),
    Deuteranopia("Deuteranopia"),
    Tritanopia("Tritanopia"),
    Acromatopia("Acromatopia"),
    Cataratas("Cataratas")
}

class MedicalViewModel : ViewModel() {
    private val _cases = MutableStateFlow<List<ClinicalCase>>(emptyList())
    val cases: StateFlow<List<ClinicalCase>> = _cases.asStateFlow()

    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedCase = MutableStateFlow<ClinicalCase?>(null)
    val selectedCase: StateFlow<ClinicalCase?> = _selectedCase.asStateFlow()

    private val _atlasZoomLevel = MutableStateFlow(1f)
    val atlasZoomLevel: StateFlow<Float> = _atlasZoomLevel.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _visionType = MutableStateFlow(VisionType.Normal)
    val visionType: StateFlow<VisionType> = _visionType.asStateFlow()

    val isOffline: StateFlow<Boolean> = MedicalRepository.isOffline
    val pendingCount: StateFlow<Int> = MedicalRepository.pendingCount

    init {
        refreshCases()
    }

    fun refreshCases() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (BuildConfig.SUPABASE_URL.isBlank()) {
                    _error.value = "Falta configurar las credenciales en 'secrets.properties' (SUPABASE_URL y SUPABASE_KEY)."
                    return@launch
                }
                MedicalRepository.fetchAllCases()
                _cases.value = MedicalRepository.getCases()
                _folders.value = MedicalRepository.getFolders()
                
                if (_cases.value.isEmpty()) {
                    _error.value = "No se encontraron casos clínicos."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (MedicalRepository.getCases().isNotEmpty()) {
                    _cases.value = MedicalRepository.getCases()
                    _folders.value = MedicalRepository.getFolders()
                } else if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_URL.contains("localhost")) {
                    _error.value = "Error de conexión: Por favor configura la URL de tu proyecto en 'secrets.properties'."
                } else {
                    _error.value = "Error de conexión / técnico: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun moveCaseToFolder(caseId: Int?, targetFolder: String) {
        MedicalRepository.updateCaseFolder(caseId, targetFolder)
        _cases.value = MedicalRepository.getCases()
        _folders.value = MedicalRepository.getFolders()
    }

    fun reorderCases(newOrder: List<ClinicalCase>) {
        MedicalRepository.reorderCases(newOrder)
        _cases.value = MedicalRepository.getCases()
    }

    fun syncPendingCases() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                MedicalRepository.syncPendingCases()
                refreshCases()
            } catch (e: Exception) {
                _error.value = "Error al sincronizar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(role: UserRole) {
        _userRole.value = role
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
        _userRole.value = null
    }

    fun selectCase(clinicalCase: ClinicalCase?) {
        _selectedCase.value = clinicalCase
    }

    fun setZoomLevel(level: Float) {
        _atlasZoomLevel.value = level.coerceIn(1f, 5f)
    }

    fun setVisionType(type: VisionType) {
        _visionType.value = type
    }

    fun addCase(clinicalCase: ClinicalCase, vitalSigns: VitalSigns, studies: List<Study> = emptyList(), onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                MedicalRepository.insertCase(clinicalCase, vitalSigns, studies)
                refreshCases()
                onResult(true)
            } catch (e: Exception) {
                _error.value = "Error al añadir caso: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCase(clinicalCase: ClinicalCase, vitalSigns: VitalSigns, studies: List<Study> = emptyList(), onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                MedicalRepository.updateCase(clinicalCase, vitalSigns, studies)
                refreshCases()
                onResult(true)
            } catch (e: Exception) {
                _error.value = "Error al actualizar caso: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addStudyToCase(caseId: Int?, study: Study) {
        MedicalRepository.addStudyToCase(caseId, study)
        _cases.value = MedicalRepository.getCases()
    }

    fun deleteCase(caseId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                MedicalRepository.deleteCase(caseId)
                refreshCases()
                onResult(true)
            } catch (e: Exception) {
                _error.value = "Error al eliminar caso: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onHotspotClicked(hotspotId: String) {
        val clinicalCase = MedicalRepository.getCases().find { it.hotspotId == hotspotId }
        if (clinicalCase != null) {
            _selectedCase.value = clinicalCase
        }
    }
}
