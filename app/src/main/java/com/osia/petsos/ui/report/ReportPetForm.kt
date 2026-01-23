package com.osia.petsos.ui.report

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.osia.petsos.domain.model.AdvertisementType
import com.osia.petsos.domain.model.PetCategory
import com.osia.petsos.domain.model.PetLocation
import com.google.android.gms.maps.model.LatLng
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.location.LocationServices
import com.osia.petsos.ui.theme.BrandPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPetForm(
    uiState: ReportPetViewModel.ReportPetUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBreedChange: (String) -> Unit,
    onCategoryChange: (PetCategory) -> Unit,
    onImagesSelected: (List<Uri>) -> Unit,
    onImageRemoved: (Uri) -> Unit,
    onLocationSelected: (PetLocation) -> Unit, 
    onLocationAddressChange: (String) -> Unit,
    onRewardChanged: (Boolean) -> Unit,
    onRewardAmountChanged: (String) -> Unit,
    onContactNameChange: (String) -> Unit,
    onContactPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    // Camera & Gallery Logic
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        onImagesSelected(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            onImagesSelected(listOf(tempPhotoUri!!))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempPhotoUri = createTempPictureUri(context)
            cameraLauncher.launch(tempPhotoUri!!)
        }
    }

    // Location Logic
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrElse(Manifest.permission.ACCESS_FINE_LOCATION) { false } ||
                      permissions.getOrElse(Manifest.permission.ACCESS_COARSE_LOCATION) { false }
        if (granted) {
            isFetchingLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    isFetchingLocation = false
                    if (location != null) {
                        onLocationSelected(PetLocation(location.latitude, location.longitude, ""))
                    }
                }.addOnFailureListener {
                    isFetchingLocation = false
                }
            } catch (_: SecurityException) {
                isFetchingLocation = false // Should not happen if permission granted
            }
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = onErrorDismiss,
            confirmButton = { TextButton(onClick = onErrorDismiss) { Text("OK") } },
            title = { Text("Error") },
            text = { Text(uiState.error) }
        )
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("Choose where to get the image from.") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                         tempPhotoUri = createTempPictureUri(context)
                         cameraLauncher.launch(tempPhotoUri!!)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("Camera")
                }
            }
        )
    }

    if (showLocationPicker) {
        LocationSelectionMap(
            initialLocation = if (uiState.location != null && (uiState.location.lat != 0.0 || uiState.location.lng != 0.0)) {
                LatLng(uiState.location.lat, uiState.location.lng)
            } else null,
            onLocationSelected = { latLng ->
                onLocationSelected(PetLocation(latLng.latitude, latLng.longitude, uiState.location?.address ?: ""))
            },
            onDismiss = { showLocationPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.reportType == AdvertisementType.LOST) "Report Lost Pet" else "Report Found Pet",
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Button(
                onClick = onSubmit,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPurple
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Publish Announcement") 
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background // #F7F5FC light
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "About Your Pet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Photos
            item {
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Add Photos*", 
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.images.size} / 5",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "The more photos, the better. The first one will be the main photo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Add Button
                        if (uiState.images.size < 5) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        showImageSourceDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto, 
                                        contentDescription = "Add",
                                        tint = BrandPurple
                                    )
                                    Text("Add", fontSize = 10.sp, color = BrandPurple, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Images List
                        uiState.images.forEach { uri ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Delete button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clickable { onImageRemoved(uri) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (uiState.imagesError != null) {
                        Text(text = uiState.imagesError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Name
            item {
                val nameLabel = if (uiState.reportType == AdvertisementType.LOST) "Pet's Name*" else "Pet's Name"
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text(nameLabel) },
                    placeholder = { Text("e.g. Buddy") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.nameError != null,
                    supportingText = { uiState.nameError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Type
            item {
                Text(text = "Pet Type*", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val types = listOf(PetCategory.DOG, PetCategory.CAT, PetCategory.OTHER)
                    types.forEach { type ->
                        val isSelected = uiState.petCategory == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(type) },
                            label = { 
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                    textAlign = TextAlign.Center
                                ) 
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Breed
            if (uiState.petCategory != PetCategory.OTHER) {
                item {
                    OutlinedTextField(
                        value = uiState.breed,
                        onValueChange = onBreedChange,
                        label = { Text("Breed*") },
                        placeholder = { Text("e.g. Golden Retriever") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = uiState.breedError != null,
                        supportingText = { uiState.breedError?.let { Text(it) } }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Description
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description*") },
                    placeholder = { Text("Color, size, temperament, wearing a collar?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.descriptionError != null,
                    supportingText = { uiState.descriptionError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reward (Only for Lost)
            if (uiState.reportType == AdvertisementType.LOST) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Offer Reward?", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(
                            checked = uiState.hasReward,
                            onCheckedChange = onRewardChanged,
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandPurple, checkedTrackColor = BrandPurple.copy(alpha = 0.5f))
                        )
                    }
                    
                    if (uiState.hasReward) {
                         OutlinedTextField(
                            value = uiState.rewardAmount,
                            onValueChange = { if (it.all { char -> char.isDigit() }) onRewardAmountChanged(it) },
                            label = { Text("Reward Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Last Seen Location",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Location
            item {
                // Address Search UI (Visual only for now, would need Places API)
                val locationText = if (uiState.location?.lat != 0.0)
                    "${uiState.location?.lat}, ${uiState.location?.lng}" 
                    else ""

                OutlinedTextField(
                    value = uiState.location?.address ?: "", 
                    onValueChange = onLocationAddressChange,
                    label = { Text("Address or Reference*") },
                    placeholder = { Text("e.g. Near Central Park") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = false, 
                    isError = uiState.locationError != null,
                    supportingText = { uiState.locationError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Use My Location Button
                OutlinedButton(
                    onClick = { 
                         locationPermissionLauncher.launch(
                             arrayOf(
                                 Manifest.permission.ACCESS_FINE_LOCATION,
                                 Manifest.permission.ACCESS_COARSE_LOCATION
                             )
                         )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
                ) {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetching Location...")
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use My Current Location")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Static Map Image (Placeholder as per design)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showLocationPicker = true }
                ) {
                    // Placeholder Map Image


                    // Since we can't easily access BuildConfig.MAPS_API_KEY without verifying it exists, 
                    // and hardcoding it is bad practice (though already in manifest),
                    // I will stick to the placeholder for now but add an overlay text saying "Tap to select on map"
                    // Or actually, I can just use the placeholder for now to be safe and consistent.
                    
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDjwiCDrW-ipeCCgtd9787cllN_OMqzBoTstPPUJq2xKCTq-aQWE5Mf-14AkjVgG13P5qV46PHDxDKBfm8OwJ7igblRopjqFo8317G3MA8Oox021KkQthdFkfc16LXSI-euFmcWX7V64kwD9z_rMb9pSuhmEo_tjAap-nKyOGiSKtS16uXTEB2abjPx9PDkBMM9xmuyWfTXy-YhsOP1Y6zeVIi8x8KlMfXeUy9PGJStMBuIVDRVsqFbDZOWNsPfNqI78E9ytVjyeHw",
                        contentDescription = "Map Location",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f/3f)
                    )
                    
                    // Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .aspectRatio(4f/3f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color.White)
                            Text(
                                text = "Tap to set location on map", 
                                color = Color.White, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Date & Time
            item {
                 Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = "2023-10-27", // Placeholder
                        onValueChange = {},
                        label = { Text("Date*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null) }
                    )
                    OutlinedTextField(
                        value = "18:30", // Placeholder
                        onValueChange = {},
                        label = { Text("Time*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                         trailingIcon = { Icon(Icons.Default.Schedule, null) }
                    )
                 }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Contact Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Contact
            item {
                 OutlinedTextField(
                    value = uiState.contactName,
                    onValueChange = onContactNameChange,
                    label = { Text("Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.contactNameError != null,
                    supportingText = { uiState.contactNameError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                 OutlinedTextField(
                    value = uiState.contactPhone,
                    onValueChange = onContactPhoneChange,
                    label = { Text("Phone*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.contactPhoneError != null,
                    supportingText = { uiState.contactPhoneError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(100.dp)) // Space for bottom button
            }
        }
    }
}

fun createTempPictureUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    
    // Use cache directory or external files dir
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
    
    // Must match authorities in AndroidManifest.xml
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
}
