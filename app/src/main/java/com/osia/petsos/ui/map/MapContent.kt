package com.osia.petsos.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering
import com.osia.petsos.core.config.FirebaseConfig
import com.osia.petsos.domain.model.AdvertisementType
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.ui.theme.PrimaryPurple
import com.osia.petsos.ui.theme.SecondaryOrange

@Composable
fun MapContent(
    viewModel: MapViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    var isMapLoaded by remember { mutableStateOf(false) }

    var isMapReady by remember { mutableStateOf(false) }

    // Initial camera move successfully located user
    LaunchedEffect(uiState, isMapReady) {
        if (isMapReady && !isMapLoaded && uiState is MapUiState.Success) {
            val location = (uiState as MapUiState.Success).userLocation
            if (location != null) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        15f
                    )
                )
                isMapLoaded = true
            }
        }
    }
    
    // Monitor camera changes to load data
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            // Camera Idle
            val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
            val target = cameraPositionState.position.target
            if (bounds != null) {
                viewModel.onCameraIdle(target, bounds)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { isMapReady = true },
            properties = MapProperties(
                isMyLocationEnabled = true, // We have permission from HomeScreen
                minZoomPreference = 10f,
                maxZoomPreference = 20f
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true
            )
        ) {
            if (uiState is MapUiState.Success) {
                val pets = (uiState as MapUiState.Success).pets
                
                // Map PetAd to something ClusterItem can use?
                // Clustering composable handles list of items. 
                // We need a wrapper class implementing ClusterItem if PetAd doesn't directly,
                // or just pass PetAd if we extend it (not possible easily with data class).
                // Actually, Clustering takes items. We can create a wrapper.
                
                val clusterItems = remember(pets) {
                    pets.map { PetClusterItem(it) }
                }

                Clustering(
                    items = clusterItems,
                    onClusterClick = { cluster ->
                        // Zoom in on cluster
                        false 
                    },
                    onClusterItemClick = { item ->
                        viewModel.selectPet(item.pet)
                        true // Consume event to prevent default info window if we want custom
                    },
                    clusterContent = { cluster ->
                         // Custom cluster rendering if needed:
                         // Default is usually fine, shows number. 
                         // To customize: 
                         Surface(
                             shape = CircleShape,
                             color = PrimaryPurple,
                             contentColor = Color.White,
                             modifier = Modifier.size(40.dp),
                             border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                         ) {
                             Box(contentAlignment = Alignment.Center) {
                                 Text(
                                     text = cluster.size.toString(),
                                     fontWeight = FontWeight.Bold,
                                     textAlign = TextAlign.Center
                                 )
                             }
                         }
                    },
                    clusterItemContent = { item ->
                        // Custom marker content (small icon)
                         PetMarker(item.pet)
                    }
                )
            }
        }

        // Custom Info Window / Bottom Sheet for selected pet
        if (selectedPet != null) {
            PetInfoWindow(
                pet = selectedPet!!,
                onDismiss = { viewModel.selectPet(null) },
                onDetailsClick = { onNavigateToDetails(selectedPet!!.id) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun PetMarker(pet: PetAd) {
    val color = if (pet.type == AdvertisementType.LOST) SecondaryOrange else PrimaryPurple
    
    Surface(
        shape = CircleShape,
        color = color,
        modifier = Modifier.size(36.dp).border(2.dp, Color.White, CircleShape),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(2.dp)) {
           // We could load the image here, but implementation inside a marker is tricky with async images due to rendering bitmap constraints on Maps. 
           // Standard approach is just an icon or synchronously loaded primitive.
           // For stability, let's use a simple icon for the marker, and the detail view for the image.
           // Or attempt AsyncImage but it might not render immediately on map initialization without custom generator.
           // Let's stick to a colored dot with initial or icon.
           // To be "wow", maybe try to load image? Native Clustering supports it via iconGenerator. 
           // Compose Clustering `clusterItemContent` is a Composable! So AsyncImage MIGHT work if the library supports rendering composable to bitmap. 
           // Maps Compose supports Composables as markers properly now.
            val imagePath = pet.imageHeader ?: pet.images.firstOrNull()
            if (imagePath != null) {
                AsyncImage(
                     model = ImageRequest.Builder(LocalContext.current)
                         .data(FirebaseConfig.getStorageUrl(imagePath))
                         .crossfade(true)
                         .allowHardware(false) // Required for map rendering
                         .build(),
                     contentDescription = null,
                     modifier = Modifier.fillMaxSize().clip(CircleShape),
                     contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun PetInfoWindow(
    pet: PetAd,
    onDismiss: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp), // Compact height
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = onDetailsClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
             // Image
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                val imagePath = pet.imageHeader ?: pet.images.firstOrNull()
                if (imagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(FirebaseConfig.getStorageUrl(imagePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = pet.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                 Text(
                    text = pet.breed ?: "Unknown Breed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (pet.type == AdvertisementType.LOST) SecondaryOrange else PrimaryPurple,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = pet.type.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            
            // Close Button
            // We can rely on clicking outside to dismiss (selectPet(null)) but an explicit close is nice.
            // Or just clicking the card goes to details.
        }
    }
}

data class PetClusterItem(
    val pet: PetAd
) : com.google.maps.android.clustering.ClusterItem {
    override fun getPosition(): LatLng = LatLng(pet.location.lat, pet.location.lng)
    override fun getTitle(): String? = pet.name
    override fun getSnippet(): String? = pet.breed
    override fun getZIndex(): Float = 0f
}
