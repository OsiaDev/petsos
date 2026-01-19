package com.osia.petsos.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.domain.repository.AuthRepository
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    // UI State exposed to the View
    val uiState: StateFlow<ProfileUiState> = authRepository.currentUser
        .map { user ->
            if (user != null) ProfileUiState.Authenticated(user) else ProfileUiState.Unauthenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState.Loading
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userPets: StateFlow<Resource<List<PetAd>>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) {
                petRepository.getUserPets(user.uid)
            } else {
                kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

}