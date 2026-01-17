package com.osia.petsos.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.osia.petsos.R
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
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToAddPet: () -> Unit = {},
    onContactOwner: (String) -> Unit = {},
    onViewDetails: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(PetFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // Filter Chips
                    FilterChips(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
                    )
                }
            }

            // Pet List con padding bottom para el bottom bar
            PetList(
                pets = getSamplePets().filter { pet ->
                    when (selectedFilter) {
                        PetFilter.ALL -> true
                        PetFilter.LOST -> pet.status == PetStatus.LOST
                        PetFilter.FOUND -> pet.status == PetStatus.FOUND
                    }
                },
                onContactOwner = onContactOwner,
                onViewDetails = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Bottom Navigation Bar - Fixed at bottom
        BottomNavigationBar(
            selectedTab = BottomNavItem.HOME,
            onTabSelected = { tab ->
                when (tab) {
                    BottomNavItem.HOME -> { /* Already here */ }
                    BottomNavItem.ALERTS -> onNavigateToAlerts()
                    BottomNavItem.ADD -> onNavigateToAddPet()
                    BottomNavItem.MESSAGES -> onNavigateToMessages()
                    BottomNavItem.PROFILE -> onNavigateToProfile()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
            modifier = Modifier
                .size(48.dp),
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
    pets: List<Pet>,
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
            bottom = 96.dp // Space for bottom navigation
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(pets) { pet ->
            PetCard(
                pet = pet,
                onContactOwner = { onContactOwner(pet.id) },
                onViewDetails = { onViewDetails(pet.id) }
            )
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
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
                    .aspectRatio(16f / 9f) // aspect-video del HTML
            ) {
                Image(
                    painter = painterResource(id = pet.imageRes),
                    contentDescription = pet.imageAlt,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Status Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(6.dp),
                    color = when (pet.status) {
                        PetStatus.LOST -> SecondaryOrange.copy(alpha = 0.9f)
                        PetStatus.FOUND -> PrimaryPurple.copy(alpha = 0.9f)
                    }
                ) {
                    Text(
                        text = pet.status.label,
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
                    text = pet.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.015).sp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = pet.breed,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )

                    Text(
                        text = pet.locationTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Button
                Button(
                    onClick = if (pet.status == PetStatus.LOST) onContactOwner else onViewDetails,
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
                        text = if (pet.status == PetStatus.LOST) "Contact Owner" else "View Details",
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
            onClick = { onTabSelected(BottomNavItem.ADD) },
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

// Data Classes and Enums
enum class PetFilter(val label: String) {
    ALL("All"),
    LOST("Lost"),
    FOUND("Found")
}

enum class PetStatus(val label: String) {
    LOST("LOST"),
    FOUND("FOUND")
}

data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val locationTime: String,
    val status: PetStatus,
    val imageRes: Int,
    val imageAlt: String
)

enum class BottomNavItem(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    ALERTS("Alerts", Icons.Default.Notifications),
    ADD("Add", Icons.Default.Add),
    MESSAGES("Messages", Icons.AutoMirrored.Filled.Message),
    PROFILE("Profile", Icons.Default.Person)
}

// Sample Data
fun getSamplePets(): List<Pet> = listOf(
    Pet(
        id = "1",
        name = "Buddy",
        breed = "Golden Retriever",
        locationTime = "Last seen: Central Park • 2 hours ago",
        status = PetStatus.LOST,
        imageRes = R.drawable.hero_dog,
        imageAlt = "Golden retriever puppy sitting in a grassy field"
    ),
    Pet(
        id = "2",
        name = "Whiskers",
        breed = "Domestic Shorthair",
        locationTime = "Found near: Sunset Blvd • 1 day ago",
        status = PetStatus.FOUND,
        imageRes = R.drawable.hero_dog,
        imageAlt = "A curious gray tabby cat with green eyes"
    ),
    Pet(
        id = "3",
        name = "Pip",
        breed = "Jack Russell Terrier",
        locationTime = "Last seen: Ocean Beach • 5 hours ago",
        status = PetStatus.LOST,
        imageRes = R.drawable.hero_dog,
        imageAlt = "A small brown and white dog running on a beach"
    )
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PetSOSTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    PetSOSTheme(darkTheme = true) {
        HomeScreen()
    }
}