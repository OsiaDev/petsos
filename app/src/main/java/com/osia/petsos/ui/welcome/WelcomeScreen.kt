package com.osia.petsos.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.osia.petsos.R
import com.osia.petsos.ui.theme.PetSOSTheme
import kotlin.math.absoluteValue

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val features = listOf(
        Feature(
            icon = Icons.Default.Search,
            title = "Report a Lost Pet",
            description = "Quickly report a lost pet and alert nearby users."
        ),
        Feature(
            icon = Icons.Default.LocationOn,
            title = "Find a Pet",
            description = "Browse a live map of found pets in your area."
        ),
        Feature(
            icon = Icons.Default.VerifiedUser,
            title = "Safe Connections",
            description = "Safely connect with finders and owners."
        )
    )

    // Estado del LazyRow para rastrear el desplazamiento
    val listState = rememberLazyListState()

    // Calcular el índice actual basado en el desplazamiento
    val currentIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (visibleItemsInfo.isEmpty()) {
                0
            } else {
                val centerOffset = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2
                val centerItem = visibleItemsInfo.minByOrNull {
                    (it.offset + it.size / 2 - centerOffset).absoluteValue
                }
                centerItem?.index ?: 0
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Agregar padding para los insets del sistema
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 140.dp) // Espacio para los botones
        ) {
            // Hero Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_dog),
                    contentDescription = "A happy golden retriever dog looking up with a joyful expression",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "Welcome to PetSOS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Your community-powered network for lost and found pets.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Features Carousel
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(features) { feature ->
                    FeatureCard(feature = feature)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Page Indicators - Dinámicos basados en currentIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                features.forEachIndexed { index, _ ->
                    val isSelected = currentIndex == index

                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(if (isSelected) RoundedCornerShape(4.dp) else CircleShape)
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color(0xFFD1D5DB)
                            )
                    )

                    if (index < features.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        // Bottom Buttons - Fixed at bottom con padding para los insets
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Get Started Button
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Login Button
            TextButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FeatureCard(feature: Feature) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Icon Container
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    PetSOSTheme {
        WelcomeScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenDarkPreview() {
    PetSOSTheme(darkTheme = true) {
        WelcomeScreen()
    }
}