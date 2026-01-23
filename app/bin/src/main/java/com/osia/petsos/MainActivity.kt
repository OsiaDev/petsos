package com.osia.petsos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.osia.petsos.ui.MainViewModel
import com.osia.petsos.ui.navigation.AppNavigation
import com.osia.petsos.ui.theme.PetSOSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilitar edge-to-edge con configuración correcta
        enableEdgeToEdge()

        // Configurar para que el contenido se dibuje detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val startDestination by viewModel.startDestination.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()

            if (!isLoading && startDestination != null) {
                PetSOSTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(startDestination = startDestination!!)
                    }
                }
            } else {
                // Show Splash/Loading
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Blank content or Logo
                }
            }
        }
    }
}