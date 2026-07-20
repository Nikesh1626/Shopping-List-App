package com.nikeshparihar.smartcart.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nikeshparihar.smartcart.R

// 1. Define your custom font family
val JakartaFontFamily = FontFamily(
    Font(R.font.jakartafontm, FontWeight.Normal), // Assuming jakartafont is a regular weight
    // Add other weights and styles if you have them, for example:
    // Font(R.font.jakartafont_bold, FontWeight.Bold),
    // Font(R.font.jakartafont_italic, FontWeight.Normal, FontStyle.Italic)
)

// 2. Define your Material 3 Typography
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = JakartaFontFamily, // Use your custom font family here
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Define other text styles as needed:
    titleLarge = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

// Set of Material typography styles to start with
