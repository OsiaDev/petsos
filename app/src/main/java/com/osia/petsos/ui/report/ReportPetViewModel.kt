package com.osia.petsos.ui.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.domain.model.AdvertisementType
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.domain.model.PetCategory
import com.osia.petsos.domain.model.PetLocation
import com.osia.petsos.domain.repository.AuthRepository
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportPetUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    
    val reportType: AdvertisementType = AdvertisementType.LOST, // Default
    val images: List<Uri> = emptyList(),
    val petCategory: PetCategory = PetCategory.DOG,
    val name: String = "",
    val breed: String = "",
    val description: String = "",
    val hasReward: Boolean = false,
    val rewardAmount: String = "",
    val location: PetLocation? = null,
    
    // Validation errors
    val nameError: String? = null,
    val breedError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null,
    val imagesError: String? = null,
    val contactNameError: String? = null,
    val contactPhoneError: String? = null
)

@HiltViewModel
class ReportPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportPetUiState())
    val uiState: StateFlow<ReportPetUiState> = _uiState.asStateFlow()

    private var initialUserLoaded = false

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            if (!initialUserLoaded) {
                 val user = authRepository.currentUser.first()
                 user?.let {
                     _uiState.update { state -> 
                         state.copy(
                             contactName = it.displayName ?: "",
                             contactPhone = it.phoneNumber ?: "" // If available
                         ) 
                     }
                 }
                 initialUserLoaded = true
            }
        }
    }

    data class ReportPetUiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null,
        
        val reportType: AdvertisementType = AdvertisementType.LOST, // Default
        val images: List<Uri> = emptyList(),
        val petCategory: PetCategory = PetCategory.DOG,
        val name: String = "",
        val breed: String = "",
        val description: String = "",
        val hasReward: Boolean = false,
        val rewardAmount: String = "",
        val location: PetLocation? = null,
        
        val contactName: String = "",
        val contactPhone: String = "",
        
        // Validation errors
        val nameError: String? = null,
        val breedError: String? = null,
        val descriptionError: String? = null,
        val locationError: String? = null,
        val imagesError: String? = null,
        val contactNameError: String? = null,
        val contactPhoneError: String? = null
    )

    fun setReportType(type: AdvertisementType) {
        _uiState.update { it.copy(reportType = type) }
    }

    fun onNameChange(newValue: String) {
        _uiState.update { it.copy(name = newValue, nameError = null) }
    }

    fun onCategoryChange(newValue: PetCategory) {
        _uiState.update { it.copy(petCategory = newValue) }
    }

    fun onBreedChange(newValue: String) {
        _uiState.update { it.copy(breed = newValue, breedError = null) }
    }

    fun onDescriptionChange(newValue: String) {
        _uiState.update { it.copy(description = newValue, descriptionError = null) }
    }

    fun onContactNameChange(newValue: String) {
        _uiState.update { it.copy(contactName = newValue, contactNameError = null) }
    }

    fun onContactPhoneChange(newValue: String) {
        _uiState.update { it.copy(contactPhone = newValue, contactPhoneError = null) }
    }

    fun onImagesSelected(uris: List<Uri>) {
        _uiState.update { 
            val current = it.images.toMutableList()
            current.addAll(uris)
            it.copy(images = current.take(5), imagesError = null) 
        }
    }

    fun onImageRemoved(uri: Uri) {
         _uiState.update { 
            val current = it.images.toMutableList()
            current.remove(uri)
            it.copy(images = current) 
        }
    }

    fun onLocationSelected(location: PetLocation) {
        _uiState.update { it.copy(location = location, locationError = null) }
    }

    fun onRewardChanged(hasReward: Boolean) {
        _uiState.update { it.copy(hasReward = hasReward) }
    }
    
    fun onRewardAmountChanged(amount: String) {
        _uiState.update { it.copy(rewardAmount = amount) }
    }

    fun submitReport() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentUser = authRepository.currentUser.first()
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                return@launch
            }

            val state = _uiState.value
            val petAd = PetAd(
                type = state.reportType,
                category = state.petCategory,
                name = if (state.name.isBlank()) null else state.name.trim(),
                breed = if (state.petCategory == PetCategory.OTHER) null else state.breed.trim(),
                description = state.description.trim(),
                hasReward = state.hasReward,
                rewardAmount = if (state.hasReward) state.rewardAmount.toIntOrNull() else null,
                location = state.location ?: PetLocation(),
                userId = currentUser.uid,
                status = com.osia.petsos.domain.model.PetAdStatus.ACTIVE,
                phones = if (state.contactPhone.isNotBlank()) listOf(state.contactPhone.trim()) else emptyList(),
                userName = state.contactName,
                userEmail = currentUser.email ?: ""
            )

            when (val result = petRepository.savePet(petAd, state.images)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {
                     _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        if (state.images.isEmpty()) {
            _uiState.update { it.copy(imagesError = "Add at least 1 photo") }
            isValid = false
        }

        if (state.reportType == AdvertisementType.LOST && state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            isValid = false
        }

        if (state.petCategory != PetCategory.OTHER && state.breed.isBlank()) {
            _uiState.update { it.copy(breedError = "Breed is required") }
            isValid = false
        }

        if (state.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description is required") }
            isValid = false
        }
        
        if (state.location == null) {
             _uiState.update { it.copy(locationError = "Location is required") }
             isValid = false
        }

        if (state.contactName.isBlank()) {
             _uiState.update { it.copy(contactNameError = "Contact Name is required") }
             isValid = false
        }

        if (state.contactPhone.isBlank()) {
             _uiState.update { it.copy(contactPhoneError = "Phone is required") }
             isValid = false
        }

        return isValid
    }
    
    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }
}
