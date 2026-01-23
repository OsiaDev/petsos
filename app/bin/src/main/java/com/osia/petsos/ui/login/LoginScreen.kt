package com.osia.petsos.ui.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.osia.petsos.core.config.FirebaseConfig
import com.osia.petsos.ui.theme.BackgroundLight
import com.osia.petsos.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe state changes
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    // Setup Credential Manager
    val credentialManager = CredentialManager.create(context)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = 20.dp,
                            spotColor = PrimaryPurple.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(PrimaryPurple, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "PetSOS",
                    color = PrimaryPurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            // Central Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Image container with glow effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                PrimaryPurple.copy(alpha = 0.05f),
                                CircleShape
                            )
                    )

                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCkoFPagm5MZvT8rRJjhe_Irk41tm7WJHyO0GWVSMi0YGNM9mQSMdvyRV6WJZ4CL_ImJaKc7L8SvLbLa0ro7xzbv3nfWhsZmWetmw8DVTr3kXt6PMZ3jmL6vgjVcqBQZ2sIFjzlgQQ6_5QJ0KFRw4107Tv1MfJ4OBZD4nD5XUX9xfoas-wKzv5aww5JwlBLAcgIVVx7gzCfUCRUtHFjvvTQLTuEYmWxveHBAoAAQPxLRtTgmB9p_B5O9s6c_DUVx9PToomU5JVXFAw",
                        contentDescription = "Friendly pets",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Help them\nfind home.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF161019),
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Join our community to help pets find their way home.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF161019).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(240.dp)
                )
            }

            // Buttons & Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Google Sign In Button using Credential Manager
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    // Log para debugging
                                    val webClientId = FirebaseConfig.GOOGLE_WEB_CLIENT_ID
                                    Log.d("LoginScreen", "Starting Google Sign-In with Web Client ID: $webClientId")

                                    // Configure Google ID option
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(webClientId)
                                        .build()

                                    // Build credential request
                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    Log.d("LoginScreen", "Credential request built successfully")

                                    // Get credential
                                    val result = credentialManager.getCredential(
                                        request = request,
                                        context = context
                                    )

                                    Log.d("LoginScreen", "Credential result received: ${result.credential.type}")

                                    // Process credential
                                    val credential = result.credential

                                    // Check if it's a GoogleIdTokenCredential
                                    if (credential is GoogleIdTokenCredential) {
                                        val idToken = credential.idToken
                                        Log.d("LoginScreen", "GoogleIdTokenCredential received, idToken: ${idToken.take(20)}...")
                                        viewModel.onGoogleSignInResult(idToken)
                                    } else {
                                        // Try to extract GoogleIdTokenCredential from CustomCredential
                                        try {
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            val idToken = googleIdTokenCredential.idToken
                                            Log.d("LoginScreen", "GoogleIdTokenCredential extracted from CustomCredential, idToken: ${idToken.take(20)}...")
                                            viewModel.onGoogleSignInResult(idToken)
                                        } catch (e: Exception) {
                                            Log.e("LoginScreen", "Failed to extract GoogleIdTokenCredential", e)
                                            viewModel.onError("Unexpected credential type: ${credential.type}")
                                        }
                                    }
                                } catch (e: GetCredentialException) {
                                    Log.e("LoginScreen", "Credential error: ${e.message}", e)
                                    viewModel.onError("Sign in failed: ${e.message}")
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "Unexpected error: ${e.message}", e)
                                    viewModel.onError("Unexpected error: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 4.dp,
                                spotColor = PrimaryPurple.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        enabled = loginState !is LoginState.Loading
                    ) {
                        // Google Icon (Simplified)
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (loginState is LoginState.Loading) "Signing in..." else "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.Black.copy(alpha = 0.05f))
                        )
                        Text(
                            text = "SECURE AUTHENTICATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.Black.copy(alpha = 0.05f))
                        )
                    }
                }

                // Footer Terms
                val termsText = buildAnnotatedString {
                    append("By continuing, you agree to our ")
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = PrimaryPurple.copy(alpha = 0.8f)
                        )
                    ) {
                        append("Terms")
                    }
                    append(" and ")
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = PrimaryPurple.copy(alpha = 0.8f)
                        )
                    ) {
                        append("Privacy Policy")
                    }
                    append(".")
                }

                Text(
                    text = termsText,
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Error message
                if (loginState is LoginState.Error) {
                    Text(
                        text = (loginState as LoginState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

}