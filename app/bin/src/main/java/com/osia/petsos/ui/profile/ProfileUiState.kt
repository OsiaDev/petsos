package com.osia.petsos.ui.profile

import com.google.firebase.auth.FirebaseUser

sealed interface ProfileUiState {

    data object Loading : ProfileUiState

    data class Authenticated(val user: FirebaseUser) : ProfileUiState

    data object Unauthenticated : ProfileUiState

}
