package com.osia.petsos.ui.home


import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.res.Configuration
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.osia.petsos.R
import com.osia.petsos.core.config.FirebaseConfig
import com.osia.petsos.domain.model.AdvertisementType
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.ui.report.ReportTypeBottomSheet
import com.osia.petsos.ui.theme.BackgroundLight
import com.osia.petsos.ui.theme.PetSOSTheme
import com.osia.petsos.ui.theme.PrimaryPurple
import com.osia.petsos.ui.theme.SecondaryOrange
import com.osia.petsos.ui.theme.SurfaceLight
import com.osia.petsos.ui.theme.TextPrimary
import com.osia.petsos.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToReportLost: () -> Unit = {},
    onNavigateToReportFound: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onContactOwner: (String) -> Unit = {},
    onViewDetails: (String) -> Unit = {},
    mapViewModel: com.osia.petsos.ui.map.MapViewModel = hiltViewModel()
) {
    // Obtain ViewModel if not provided (using hiltViewModel() inside body)
    val homeViewModel: HomeViewModel = viewModel

    val uiState by homeViewModel.uiState.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val selectedFilter by homeViewModel.selectedFilter.collectAsState()
    val isCardView by homeViewModel.isCardView.collectAsState()

    val currentUser by homeViewModel.currentUser.collectAsState(initial = null)
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
           homeViewModel.refresh()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    var showReportTypeSheet by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(BottomNavItem.HOME) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            // Sticky Header con blur
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f),
                color = BackgroundLight.copy(alpha = 0.8f),
                shadowElevation = 0.dp
            ) {
                Column {
                    // Top Bar
                    HomeTopBar(onProfileClick = onNavigateToProfile)

                    // Search Bar
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = homeViewModel::onSearchQueryChange,
                        isCardView = isCardView,
                        onToggleView = homeViewModel::toggleViewType,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // Filter Chips
                    FilterChips(
                        selectedFilter = selectedFilter,
                        onFilterSelected = homeViewModel::onFilterSelected,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
                    )
                }
            }

            // Content
            if (currentTab == BottomNavItem.MAP) {
                com.osia.petsos.ui.map.MapContent(
                    viewModel = mapViewModel,
                    onNavigateToDetails = onViewDetails,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp) // Space for bottom bar
                )
            } else {
                // Home Content (Pet List)
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryPurple)
                        }
                    }
                    is HomeUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Error: ${state.message}", color = Color.Red)
                        }
                    }
                    is HomeUiState.Success -> {
                        val filteredPets = state.pets.filter { pet ->
                            (selectedFilter == PetFilter.ALL ||
                                    (selectedFilter == PetFilter.LOST && pet.type == AdvertisementType.LOST) ||
                                    (selectedFilter == PetFilter.FOUND && pet.type == AdvertisementType.FOUND)) &&
                                    (searchQuery.isBlank() || pet.name?.contains(searchQuery, ignoreCase = true) == true ||
                                            pet.breed?.contains(searchQuery, ignoreCase = true) == true)
                        }

                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = { homeViewModel.refresh() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            PetList(
                                pets = filteredPets,
                                isCardView = isCardView,
                                onContactOwner = onContactOwner,
                                onViewDetails = onViewDetails,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar - Fixed at bottom con WindowInsets
        BottomNavigationBar(
            selectedTab = currentTab,
            onTabSelected = { tab ->
                when (tab) {
                    BottomNavItem.HOME -> currentTab = BottomNavItem.HOME
                    BottomNavItem.MAP -> currentTab = BottomNavItem.MAP
                    BottomNavItem.ADD -> {
                        if (currentUser != null) {
                            showReportTypeSheet = true
                        } else {
                            onNavigateToLogin()
                        }
                    }
                    BottomNavItem.MESSAGES -> onNavigateToMessages()
                    BottomNavItem.PROFILE -> onNavigateToProfile()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        )

        if (showReportTypeSheet) {
            ReportTypeBottomSheet(
                onDismissRequest = { showReportTypeSheet = false },
                onReportLostClick = {
                    showReportTypeSheet = false
                    onNavigateToReportLost()
                },
                onReportFoundClick = {
                    showReportTypeSheet = false
                    onNavigateToReportFound()
                }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PetSOS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.015).sp
        )

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(32.dp),
                tint = TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isCardView: Boolean,
    onToggleView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = {
                Text(
                    text = "Search by name, breed, location...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextSecondary
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceLight,
                focusedContainerColor = SurfaceLight,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = PrimaryPurple
            ),
            singleLine = true
        )

        // Filter Button
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            color = SurfaceLight,
            onClick = { /* TODO: Open filter dialog */ }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = TextSecondary
                )
            }
        }

        // View Toggle Button
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            color = SurfaceLight,
            onClick = onToggleView
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (isCardView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle View",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
fun FilterChips(
    selectedFilter: PetFilter,
    onFilterSelected: (PetFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PetFilter.entries.forEach { filter ->
            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(24.dp),
                color = if (selectedFilter == filter) PrimaryPurple else SurfaceLight,
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedFilter == filter) Color.White else TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun PetList(
    pets: List<PetAd>,
    isCardView: Boolean,
    onContactOwner: (String) -> Unit,
    onViewDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 0.dp,
            bottom = 16.dp // Reducido porque WindowInsets manejará el espacio del navigation bar
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(pets) { pet ->
            if (isCardView) {
                PetCard(
                    pet = pet,
                    onContactOwner = { onContactOwner(pet.id) },
                    onViewDetails = { onViewDetails(pet.id) }
                )
            } else {
                PetHorizontalCard(
                    pet = pet,
                    onContactOwner = { onContactOwner(pet.id) },
                    onViewDetails = { onViewDetails(pet.id) }
                )
            }
        }
    }
}

@Composable
fun PetCard(
    pet: PetAd,
    onContactOwner: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(12.dp),
        onClick = onViewDetails, // Make the whole card clickable
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pet Image with Status Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.LightGray)
            ) {
                // Prioritize imageHeader (thumb) if available
                val imagePath = pet.imageHeader ?: pet.images.firstOrNull()

                val imageUrl = imagePath?.let { path ->
                    FirebaseConfig.getStorageUrl(path)
                }

                if (imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Pet Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.mipmap.ic_launcher)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray)
                    }
                }

                // Status Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(6.dp),
                    color = when (pet.type) {
                        AdvertisementType.LOST -> SecondaryOrange.copy(alpha = 0.9f)
                        AdvertisementType.FOUND -> PrimaryPurple.copy(alpha = 0.9f)
                    }
                ) {
                    Text(
                        text = pet.type.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.08.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Pet Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pet.name ?: "Unknown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.015).sp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = pet.breed ?: "Unknown Breed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )

                    Text(
                        text = pet.location.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Button
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        // Bottom Navigation Background con blur effect
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp),
            color = BackgroundLight.copy(alpha = 0.8f),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                // Left side items
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BottomNavItem.entries.take(2).forEach { item ->
                        BottomNavButton(
                            item = item,
                            selected = selectedTab == item,
                            onClick = { onTabSelected(item) }
                        )
                    }
                }

                // Spacer for FAB
                Spacer(modifier = Modifier.width(64.dp))

                // Right side items
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BottomNavItem.entries.drop(3).forEach { item ->
                        BottomNavButton(
                            item = item,
                            selected = selectedTab == item,
                            onClick = { onTabSelected(item) }
                        )
                    }
                }
            }
        }

        // Floating Action Button (centered)
        FloatingActionButton(
            onClick = {
                // We reuse the onTabSelected logic which is passed down from HomeScreen
                // effectively this calls the callback we defined above
                onTabSelected(BottomNavItem.ADD)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-24).dp)
                .size(64.dp),
            containerColor = PrimaryPurple,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Pet",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun BottomNavButton(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .width(64.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) PrimaryPurple else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) PrimaryPurple else TextSecondary
        )
    }
}

enum class PetFilter(val label: String) {
    ALL("All"),
    LOST("Lost"),
    FOUND("Found")
}

enum class BottomNavItem(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    MAP("Map", Icons.Default.Map),
    ADD("Add", Icons.Default.Add),
    MESSAGES("Messages", Icons.AutoMirrored.Filled.Message),
    PROFILE("Profile", Icons.Default.Person)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PetSOSTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    PetSOSTheme(darkTheme = true) {
        HomeScreen()
    }
}

@Composable
fun PetHorizontalCard(
    pet: PetAd,
    onContactOwner: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        onClick = onViewDetails,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                // Prioritize imageHeader (thumb) if available
                val imagePath = pet.imageHeader ?: pet.images.firstOrNull()

                val imageUrl = imagePath?.let { path ->
                    FirebaseConfig.getStorageUrl(path)
                }

                if (imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Pet Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.mipmap.ic_launcher)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header: Name and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pet.name ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = when (pet.type) {
                            AdvertisementType.LOST -> SecondaryOrange.copy(alpha = 0.1f)
                            AdvertisementType.FOUND -> PrimaryPurple.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = pet.type.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = when (pet.type) {
                                AdvertisementType.LOST -> SecondaryOrange
                                AdvertisementType.FOUND -> PrimaryPurple
                            }
                        )
                    }
                }

                Text(
                    text = pet.breed ?: "Unknown Breed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextSecondary
                    )
                    Text(
                        text = pet.location.address,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                // Time (Placeholder)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextSecondary
                    )
                    Text(
                        text = "Recently", // Placeholder
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
            }
            
            // Action Icon
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                 Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = PrimaryPurple.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp),
                    onClick = onViewDetails
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message, // Using Message icon as in reference (chat_bubble) or Info
                            contentDescription = "Details",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

}