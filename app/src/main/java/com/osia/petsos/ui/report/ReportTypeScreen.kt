package com.osia.petsos.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.osia.petsos.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTypeBottomSheet(
    onDismissRequest: () -> Unit,
    onReportLostClick: () -> Unit,
    onReportFoundClick: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent, // Transparent because we want custom rounded corners/background
        dragHandle = null // We'll implement our own if needed, or use the design's top bar
    ) {
        ReportTypeContent(
            onDismiss = onDismissRequest,
            onReportLostClick = onReportLostClick,
            onReportFoundClick = onReportFoundClick
        )
    }
}

@Composable
fun ReportTypeContent(
    onDismiss: () -> Unit,
    onReportLostClick: () -> Unit,
    onReportFoundClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            .background(MaterialTheme.colorScheme.surface) // Use theme surface color
            .padding(24.dp)
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // iOS Grabber (Visual only)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose Report Type",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF713a8d) // Primary from design
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF7F6F7)) // Background light
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lost Pet Card
            ReportOptionCard(
                title = "I Lost a Pet",
                description = "Alert the community and get immediate help tracking down your pet.",
                buttonText = "Report Lost",
                iconResId = R.drawable.ic_launcher_foreground, // Placeholder icon, replace with resource if available
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBcL1smncXNPkhMRsrQxbwpIb3maLlktuWft-kyx9tiS5Gs4K6ueVTlhA9gA1IBwLNAcffibgK9nJv8Ig7_x-2c2E46yig6mEXV5QwzoDQIijRbGiLRS7twnfs4OOaPIPD7RFFVSVnEnH60BRcHeK_5prvlaj2WY8Lp4gk2NIAvhWxg8DbFkMTXF-sbmAQ-gg6XuX1NLVE8Ivz1zQwiZkK0Ng0sRpZk9t1q1WZZini7xy7Nd_QrWgW8ixrsnNpRzwwyuTvh53ONLf0",
                buttonColor = Color(0xFF713a8d),
                iconBgColor = Color(0xFF713a8d).copy(alpha = 0.1f),
                iconTint = Color(0xFF713a8d),
                onClick = onReportLostClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Found Pet Card
            ReportOptionCard(
                title = "I Found a Pet",
                description = "Secure the pet and help reunite them with their worried owners.",
                buttonText = "Report Found",
                iconResId = R.drawable.ic_launcher_foreground, // Placeholder
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDb5JMDATJwy17_ewybHTLT7cxEPl-RmeEZPx3s4KNo-0lIEj3A7L_Ka7Sme-Pqgr8xpZHMdfxPMMO7nlkbtwnUrjWOYqkUD-IGw4md2T_Yn3oDsIcHTQwHUkJMOrXYKV4o1gKjn2M-XbUu1BkgjAQYTumcu0W2TGCuXoMUkaCL58Dx5PPZmj_g_QpB5TqWVzU4uumefnODlLTJjguFL-IHLX7qwTWkKZ5QhSG2T6_F6pf-9EXSNk79LOtvcQ8EXGM83CgH5Z77SsE",
                buttonColor = Color(0xFFD4B883),
                iconBgColor = Color(0xFFD4B883).copy(alpha = 0.2f),
                iconTint = Color(0xFF9c8456),
                onClick = onReportFoundClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF713a8d).copy(alpha = 0.05f))
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF713a8d),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your location will be used to notify nearby users instantly. Please ensure your GPS is enabled for faster response.",
                    fontSize = 12.sp,
                    color = Color(0xFF713a8d),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ReportOptionCard(
    title: String,
    description: String,
    buttonText: String,
    iconResId: Int, // Ideally a painter or vector
    imageUrl: String,
    buttonColor: Color,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, spotColor = Color(0xFF713a8d).copy(alpha = 0.1f), shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for Material Symbol
                    // In real implementation, use actual icon resource
                    Icon(
                         // Using default icon as placeholder since we don't have the exact SVGs
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = iconTint
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF713a8d),
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(buttonColor)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(100.dp)
                .height(140.dp) // Minimum height from CSS
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray) // Placeholder while loading
        )
    }
}
