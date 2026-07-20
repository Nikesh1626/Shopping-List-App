package com.nikeshparihar.smartcart.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikeshparihar.smartcart.ui.theme.DarkGreen
import com.nikeshparihar.smartcart.ui.theme.JakartaFontFamily
import com.nikeshparihar.smartcart.ui.theme.LightGreen
import com.nikeshparihar.smartcart.ui.theme.MutedText
import com.nikeshparihar.smartcart.ui.theme.SoftSalmon
import com.nikeshparihar.smartcart.viewmodel.AuthViewModel

@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    val state = authViewModel.authState
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FFF8),
                        Color(0xFFEDF5ED),
                        Color(0xFFE2EDE2),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── App Branding ──
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(DarkGreen, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ShopSmart",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = JakartaFontFamily,
                color = DarkGreen,
            )
            Text(
                text = "Your smart shopping companion",
                fontSize = 14.sp,
                fontFamily = JakartaFontFamily,
                color = MutedText,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // ── Auth Card ──
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (state.isSignUp) "Create Account" else "Welcome Back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JakartaFontFamily,
                        color = DarkGreen,
                    )
                    Text(
                        text = if (state.isSignUp) "Sign up to get started" else "Sign in to continue",
                        fontSize = 13.sp,
                        fontFamily = JakartaFontFamily,
                        color = MutedText,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = authViewModel::onEmailChange,
                        label = { Text("Email", fontFamily = JakartaFontFamily) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DarkGreen) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            cursorColor = DarkGreen,
                            focusedLabelColor = DarkGreen,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = authViewModel::onPasswordChange,
                        label = { Text("Password", fontFamily = JakartaFontFamily) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DarkGreen) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = MutedText,
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            cursorColor = DarkGreen,
                            focusedLabelColor = DarkGreen,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Error message
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = SoftSalmon,
                            fontSize = 13.sp,
                            fontFamily = JakartaFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sign In / Sign Up Button
                    Button(
                        onClick = authViewModel::onEmailAuth,
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (state.isSignUp) "Create Account" else "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JakartaFontFamily,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // OR divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = LightGreen)
                        Text(
                            "OR",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontSize = 12.sp,
                            fontFamily = JakartaFontFamily,
                            color = MutedText,
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = LightGreen)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign-In
                    OutlinedButton(
                        onClick = { authViewModel.onGoogleSignIn(context as Activity) },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // G icon using text as a simple substitute
                            Text(
                                text = "G",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4),  // Google Blue
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 15.sp,
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkGreen,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle Sign Up / Sign In
                    TextButton(onClick = authViewModel::toggleSignUp) {
                        Text(
                            text = if (state.isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                            fontSize = 13.sp,
                            fontFamily = JakartaFontFamily,
                            color = DarkGreen,
                        )
                    }
                }
            }
        }
    }
}
