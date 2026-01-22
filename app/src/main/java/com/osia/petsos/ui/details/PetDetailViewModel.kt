package com.osia.petsos.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PetDetailUiState {
    data object Loading : PetDetailUiState()
    data class Success(val pet: PetAd) : PetDetailUiState()
    data class Error(val message: String) : PetDetailUiState()
}

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetDetailUiState>(PetDetailUiState.Loading)

    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    fun getPetDetails(petId: String) {
        viewModelScope.launch {
            petRepository.getPet(petId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = PetDetailUiState.Loading
                    }
                    is Resource.Success -> {
                        result.data?.let {
                            _uiState.value = PetDetailUiState.Success(it)
                        } ?: run {
                            _uiState.value = PetDetailUiState.Error("Pet not found")
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = PetDetailUiState.Error(result.message ?: "Unknown error")
                    }
                }
            }
        }
    }
}
