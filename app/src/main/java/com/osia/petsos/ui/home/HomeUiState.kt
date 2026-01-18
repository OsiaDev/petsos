package com.osia.petsos.ui.home

import com.osia.petsos.domain.model.PetAd

sealed class HomeUiState {

    object Loading : HomeUiState()

    data class Success(val pets: List<PetAd>) : HomeUiState()

    data class Error(val message: String) : HomeUiState()

}
