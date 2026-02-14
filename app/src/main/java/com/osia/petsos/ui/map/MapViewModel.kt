package com.osia.petsos.ui.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.core.location.LocationService
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed class MapUiState {
    data object Loading : MapUiState()
    data class Success(val pets: List<PetAd>, val userLocation: Location? = null) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _selectedPet = MutableStateFlow<PetAd?>(null)
    val selectedPet: StateFlow<PetAd?> = _selectedPet.asStateFlow()

    init {
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        viewModelScope.launch {
            val location = locationService.getCurrentLocation()
            if (location != null) {
                // Initial load with a default radius (e.g., 10km) around user
                loadPets(location.latitude, location.longitude, 10.0)
                _uiState.value = MapUiState.Success(emptyList(), location)
            } else {
                 // Fallback or just load without location? 
                 // Will wait for camera move to load if location fails, generally default to 0,0 or saved location
                 _uiState.value = MapUiState.Success(emptyList(), null)
            }
        }
    }

    fun onCameraIdle(center: LatLng, visibleRegionBounds: LatLngBounds) {
        // Calculate radius based on bounds to cover the screen
        val radiusKm = calculateRadius(center, visibleRegionBounds)
        loadPets(center.latitude, center.longitude, radiusKm)
    }

    private fun loadPets(lat: Double, lng: Double, radiusKm: Double) {
        viewModelScope.launch {
             // Keep existing location if we have one in state
            val currentLocation = (_uiState.value as? MapUiState.Success)?.userLocation
            
            petRepository.getNearbyPets(lat, lng, radiusKm).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.value = MapUiState.Success(result.data ?: emptyList(), currentLocation)
                    }
                    is Resource.Error -> {
                        // Maintain success state if we just failed to fetch updates, or show error?
                        // Showing error might block the map. Better to just log or show snackbar.
                        // For now, if we have data, keep it.
                         if (_uiState.value !is MapUiState.Success) {
                             _uiState.value = MapUiState.Error(result.message ?: "Unknown error")
                         }
                    }
                    is Resource.Loading -> {
                        // Optional: Show loading indicator overlay?
                    }
                }
            }
        }
    }

    fun selectPet(pet: PetAd?) {
        _selectedPet.value = pet
    }

    // Rough estimation of radius from center to corner of bounds
    private fun calculateRadius(center: LatLng, bounds: LatLngBounds): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            center.latitude, center.longitude,
            bounds.northeast.latitude, bounds.northeast.longitude,
            results
        )
        return results[0] / 1000.0 // Convert meters to km
    }
}
