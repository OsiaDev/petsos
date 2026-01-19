package com.osia.petsos.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.osia.petsos.ui.theme.*
import com.osia.petsos.utils.Resource
import com.osia.petsos.domain.model.PetAd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Handle Unauthenticated State is done in Navigation, but checking here too for safety/rendering
    val user = (uiState as? ProfileUiState.Authenticated)?.user

    if (uiState is ProfileUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryPurple)
        }
        return
    }

    // Tab State
    var selectedTab by remember { mutableStateOf(ProfileTab.REPORTED_PETS) }

    val userPetsResource by viewModel.userPets.collectAsState()
    
    val petList = remember(userPetsResource) {
        if (userPetsResource is Resource.Success) {
            (userPetsResource as Resource.Success).data ?: emptyList()
        } else {
            emptyList()
        }
    }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight, 
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // User Info Section (Always Visible)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Avatar
                    AsyncImage(
                        model = user?.photoUrl ?: "https://lh3.googleusercontent.com/aida-public/AB6AXuDwgSj-1M3pvyjfdtIWvTEz3uEGOkp6wQm7V1NWSMWfYCZR1_TSK0PhoTZE8emPz6ihAM5-V_MRmV1cqOYCqhYUIyIC47sq7YznclIWqLMVLy7KcW2dGyGnUPrAaoEYJY-7Y-ZIVd6cEJZUkFxTGAVGnnSwlbbXuAOue2GXj5jCKPbwVZXItHS1-9haT7pT_y6MmRxJ2HGPLPVXBcCu0wBY0zVqgl-ttSfCSJzFhwldZn3P3Dv-CHaFRmHITu9oEESiN66C5vELwCk",
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(Gray200)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name & Location
                    Text(
                        text = user?.displayName ?: "Alex Doe",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = TextPrimary
                    )
                    
                    Text(
                        text = "San Francisco, CA", // Placeholder
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Edit Profile Button
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurpleLight,
                            contentColor = PrimaryPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Edit Profile",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tabs Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(PrimaryPurpleLight, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabButton(
                        text = "My Reported Pets",
                        isSelected = selectedTab == ProfileTab.REPORTED_PETS,
                        onClick = { selectedTab = ProfileTab.REPORTED_PETS },
                        modifier = Modifier.weight(1f)
                    )
                    
                    TabButton(
                        text = "Settings",
                        isSelected = selectedTab == ProfileTab.SETTINGS,
                        onClick = { selectedTab = ProfileTab.SETTINGS },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                ProfileTab.REPORTED_PETS -> {
                    if (userPetsResource is com.osia.petsos.utils.Resource.Loading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryPurple)
                            }
                        }
                    } else {
                        items(petList) { pet ->
                            PetCard(pet)
                        }
                        if (petList.isEmpty()) {
                            item {
                                Text(
                                    text = "No pets reported yet.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    textAlign = TextAlign.Center,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                ProfileTab.SETTINGS -> {
                    item {
                         SettingsContent(onSignOut = { viewModel.signOut() })
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

@Composable
fun SettingsContent(onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ListItem(
            headlineContent = { Text("Notifications") },
            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
            trailingContent = { Switch(checked = true, onCheckedChange = {}) }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Sign Out") },
            leadingContent = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = ErrorLight) },
            modifier = Modifier.clickable { onSignOut() }
        )
    }
}

enum class ProfileTab {
    REPORTED_PETS,
    SETTINGS
}

@Composable
fun PetCard(pet: PetAd) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200) // Using Gray200 as border-light equivalent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = pet.type.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (pet.type == com.osia.petsos.domain.model.AdvertisementType.LOST) SecondaryOrange else PrimaryPurple
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = pet.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary
                )
                
                Text(
                    text = pet.createdAt?.toString() ?: "No date", // Basic formatting for now
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            AsyncImage(
                model = pet.photoUrls.firstOrNull()?.let { com.osia.petsos.core.config.FirebaseConfig.getStorageUrl(it) } 
                        ?: "https://lh3.googleusercontent.com/placeholder",
                contentDescription = "Pet Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(4f/3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray200)
            )
        }
    }

}

