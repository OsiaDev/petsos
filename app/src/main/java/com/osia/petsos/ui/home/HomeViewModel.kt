package com.osia.petsos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.domain.model.AdvertisementType
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(PetFilter.ALL)
    val selectedFilter: StateFlow<PetFilter> = _selectedFilter.asStateFlow()

    init {
        fetchPets()
    }

    private fun fetchPets() {
        viewModelScope.launch {
            repository.getPets().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.value = HomeUiState.Success(result.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _uiState.value = HomeUiState.Error(result.message ?: "Unknown error")
                    }
                    is Resource.Loading -> {
                        _uiState.value = HomeUiState.Loading
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: PetFilter) {
        _selectedFilter.value = filter
    }
    
}

