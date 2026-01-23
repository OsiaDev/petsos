package com.osia.petsos.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.core.location.LocationService
import com.osia.petsos.domain.repository.AuthRepository
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.data.preferences.DataStoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.osia.petsos.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val locationService: LocationService,
    private val dataStoreRepository: DataStoreRepository
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

    val isCardView: StateFlow<Boolean> = dataStoreRepository.isCardViewHome
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        fetchPets()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: PetFilter) {
        _selectedFilter.value = filter
    }

    fun toggleViewType() {
        viewModelScope.launch {
            dataStoreRepository.saveViewPreference(!isCardView.value)
        }
    }

    private var fetchJob: Job? = null

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

            val flow = if (location != null) {
                // Fetch within 50km to have enough data for "Load More" or if user expands filter
                repository.getNearbyPets(location.latitude, location.longitude, 50.0)
            } else {
                repository.getPets()
            }

            flow.collectLatest { result ->
                // Update refreshing state only if we were refreshing
                if (!showLoading) {
                    _isRefreshing.value = false
                }

                when (result) {
                    is Resource.Success -> {
                        val pets = result.data ?: emptyList()
                        
                        val filteredPets = if (location != null) {
                            pets.filter { pet ->
                                // Client-side precise filter
                                // We fetched 50km, but maybe we only show 10km by default?
                                // User asked for "Default 10km".
                                // We can implement a "Radius" filter in the UI later. 
                                // For now, let's keep the hardcoded 10km logic or expand it.
                                // Given the "Pagination" request, likely they want to see MORE than 10km if they scroll?
                                // "se puedan ir cargando a medida que va deslizando ... los que estan mas alejados"
                                // This implies we should NOT filter strictly to 10km, but ORDER by distance.
                                // So I will REMOVE the <= 10km filter if I want to support "Show more as I scroll".
                                // But the user said "Search by default in 10km". 
                                // Conflict: "Default 10km" vs "Show farther as I scroll".
                                // Interpretation: "Show 10km" is the *Unfiltered* view? Or just the *Initial* view?
                                // Re-reading: "Busque por defecto en un rango de 10km... y a medida que desliza... los mas alejados".
                                // This means the LIST should contain MORE than 10km, but maybe visually distinct? 
                                // OR, simply valid pets are those within 10km, but "Load More" fetching expands this?
                                // EASIEST INTERPRETATION: Fetch a large batches (e.g. 50km), Sort by Distance.
                                // The "10km default" might mean "Don't show things 500km away".
                                // So I will filter to 50km (Repo limit) and Sort. 
                                // I will Remove the strict `10_000` filter so the user can see "Next Closest".
                                calculateDistance(
                                    location.latitude, location.longitude,
                                    pet.location.lat, pet.location.lng
                                ) <= 50_000 // 50 km hard limit for the "Near Me" query
                            }.sortedBy { pet ->
                                calculateDistance(
                                    location.latitude, location.longitude,
                                    pet.location.lat, pet.location.lng
                                )
                            }
                        } else {
                            pets
                        }
                        
                        _uiState.value = HomeUiState.Success(filteredPets)
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
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

}

