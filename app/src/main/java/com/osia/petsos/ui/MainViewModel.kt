package com.osia.petsos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.data.preferences.DataStoreRepository
import com.osia.petsos.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)

    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.isFirstLaunch.collect { isFirst ->
                _startDestination.value = if (isFirst) {
                    Screen.Welcome.route
                } else {
                    Screen.Home.route // Or Login, but Home handles redirection
                }
                _isLoading.value = false
            }
        }
    }

}
