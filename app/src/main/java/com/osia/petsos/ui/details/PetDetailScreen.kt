package com.osia.petsos.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.osia.petsos.core.config.FirebaseConfig
import com.osia.petsos.domain.model.AdvertisementType
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.ui.theme.BackgroundLight
import com.osia.petsos.ui.theme.PrimaryPurple
import com.osia.petsos.ui.theme.PrimaryPurpleLight
import com.osia.petsos.ui.theme.SecondaryOrange
import com.osia.petsos.ui.theme.SurfaceLight
import com.osia.petsos.ui.theme.TextPrimary
import com.osia.petsos.ui.theme.TextSecondary
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import android.content.Intent
import android.net.Uri
import com.google.maps.android.compose.MapUiSettings
import androidx.core.net.toUri

@Composable
fun PetDetailScreen(
    petId: String,
    viewModel: PetDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onContactOwner: (String) -> Unit
) {
    LaunchedEffect(petId) {
        viewModel.getPetDetails(petId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        when (val state = uiState) {
            is PetDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryPurple
                )
            }
            is PetDetailUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
            is PetDetailUiState.Success -> {
                PetDetailContent(
                    pet = state.pet,
                    onNavigateBack = onNavigateBack,
                    onContactOwner = onContactOwner
                )
            }
        }
    }
}

@Composable
fun PetDetailContent(
    pet: PetAd,
    onNavigateBack: () -> Unit,
    onContactOwner: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // Space for bottom bar
        ) {
            // Hero Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                val images = pet.images.ifEmpty { listOf("") }
                val pagerState = rememberPagerState(pageCount = { images.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val imageUrl = if (images[page].isNotEmpty()) {
                        FirebaseConfig.getStorageUrl(images[page])
                    } else null

                    if (imageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Pet Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image Available")
                        }
                    }
                }

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                )

                // Top Bar (Back Button & Title)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp), // Adjust for system bars if needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = pet.name ?: "Pet Details",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                     Spacer(modifier = Modifier.weight(1f))
                     Spacer(modifier = Modifier.size(40.dp)) // Balance layout
                }

                // Status Badge
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart),
                    shape = RoundedCornerShape(24.dp),
                    color = when (pet.type) {
                        AdvertisementType.LOST -> SecondaryOrange
                        AdvertisementType.FOUND -> PrimaryPurple
                    }
                ) {
                    Text(
                        text = pet.type.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Pager Indicators
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(images.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp)
                            )
                        }
                    }
                }
            }

            // Content Body
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title and Location
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = pet.name ?: "Unknown",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.015).sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = "Location",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = pet.location.address.ifEmpty { "Location not specified" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Details Grid
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if(pet.type == AdvertisementType.FOUND) SurfaceLight else PrimaryPurpleLight.copy(alpha = 0.3f), // Adjust bg based on type or keep standard? HTML used PrimaryLight
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(label = "Species", value = pet.category.name, modifier = Modifier.weight(1f))
                            DetailItem(label = "Breed", value = pet.breed ?: "Unknown", modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Age is not in PetAd? Assuming it might be added later or we use Description
                            // Actually HTML showed Age. PetAd has no Age field shown in view_file.
                            // I will skip Age or put "N/A" if not available.
                            // DetailItem(label = "Age", value = "3 years", modifier = Modifier.weight(1f))

                            // Gender also not in PetAd visible fields?
                            // Let's check the fields again.
                            // PetAd fields: category, breed, name, description, hasReward, etc. NO AGE, NO GENDER.
                            // I will omit them for now.
                        }

                         // Posted By
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Posted by",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            // We don't have user name in PetAd, only userId.
                            // In a real app we would fetch user profile.
                            // For now, static text or "User"
                            Text(
                                text = "User (ID: ${pet.userId.take(5)}...)", // Placeholder
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Description
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Description",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = pet.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )
                }

                // Last Seen At (Map)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last Seen At",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        TextButton(onClick = {
                            val gmmIntentUri =
                                "geo:${pet.location.lat},${pet.location.lng}?q=${pet.location.lat},${pet.location.lng}(${pet.name})".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            // Check if Maps is installed, otherwise let system handle it
                            // Since we are in Composable scope and getting context is easy
                            // We will use LocalContext.current inside the onClick
                        }) {
                           // Text moved inside the Clickable area logic below
                        }
                    }

                    val context = LocalContext.current

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = SurfaceLight
                    ) {
                        if (pet.location.lat != 0.0 && pet.location.lng != 0.0) {
                            val petLocation = LatLng(pet.location.lat, pet.location.lng)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(petLocation, 15f)
                            }

                            Box {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    uiSettings = MapUiSettings(
                                        zoomControlsEnabled = false,
                                        scrollGesturesEnabled = true,
                                        zoomGesturesEnabled = true,
                                        rotationGesturesEnabled = false,
                                        tiltGesturesEnabled = false
                                    )
                                ) {
                                    Marker(
                                        state = rememberUpdatedMarkerState(position = petLocation),
                                        title = pet.name,
                                        snippet = pet.location.address
                                    )
                                }
                                
                                // Overlay button to open in maps
                                IconButton(
                                    onClick = {
                                        val gmmIntentUri =
                                            "geo:${pet.location.lat},${pet.location.lng}?q=${pet.location.lat},${pet.location.lng}(${
                                                Uri.encode(pet.name)
                                            })".toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (_: Exception) {
                                            // Fallback to browser
                                            val browserIntent = Intent(Intent.ACTION_VIEW,
                                                "https://www.google.com/maps/search/?api=1&query=${pet.location.lat},${pet.location.lng}".toUri())
                                            context.startActivity(browserIntent)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Open in Maps",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Location coordinates not available", color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White, // Or background with blur
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onContactOwner(pet.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    )
                ) {
                    Text(
                        text = "Contact Owner",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { /* Share Logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurpleLight, // Adjust for dark mode if needed
                        contentColor = PrimaryPurple
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Pet Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
