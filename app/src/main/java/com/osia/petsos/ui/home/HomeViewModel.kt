package com.osia.petsos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.domain.repository.AuthRepository
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
    private val repository: PetRepository,
    authRepository: AuthRepository,
    private val locationService: com.osia.petsos.core.location.LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUser = authRepository.currentUser

    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(PetFilter.ALL)

    val selectedFilter: StateFlow<PetFilter> = _selectedFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchPets()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: PetFilter) {
        _selectedFilter.value = filter
    }

    private var fetchJob: kotlinx.coroutines.Job? = null

    fun refresh() {
        // Don't show full screen loading when refreshing via swipe
        fetchPets(showLoading = false)
    }

    private fun fetchPets(showLoading: Boolean = true) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (showLoading) {
                _uiState.value = HomeUiState.Loading
            } else {
                _isRefreshing.value = true
            }

            // Fetch pets and location in parallel or sequentially (location first for sorting)
            val location = try {
                locationService.getCurrentLocation()
            } catch (_: SecurityException) {
                null
            }

            repository.getPets().collectLatest { result ->
                // Update refreshing state only if we were refreshing
                if (!showLoading) {
                    _isRefreshing.value = false
                }

                when (result) {
                    is Resource.Success -> {
                        val pets = result.data ?: emptyList()
                        val sortedPets = if (location != null) {
                            pets.sortedBy { pet ->
                                calculateDistance(
                                    location.latitude, location.longitude,
                                    pet.location.lat, pet.location.lng
                                )
                            }
                        } else {
                            pets
                        }
                        _uiState.value = HomeUiState.Success(sortedPets)
                    }

                    is Resource.Error -> {
                        _uiState.value = HomeUiState.Error(result.message ?: "Unknown error")
                    }

                    is Resource.Loading -> {
                        if (showLoading) {
                            _uiState.value = HomeUiState.Loading
                        }
                    }
                }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

}

