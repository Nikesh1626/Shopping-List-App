package com.nikeshparihar.smartcart.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.darkColorScheme

// Your DarkColorScheme definition (can be kept or removed if not needed elsewhere,
// as it's not currently used by ShoppingListTheme)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = TextLightGreen,
    tertiary = SoftSalmon,
    background = LightBgColour,
    surface = CardWhite,
    onPrimary = CardWhite,
    onSecondary = CardWhite,
    onTertiary = CardWhite,
    onBackground = ForestGreen,
    onSurface = ForestGreen

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ShoppingListTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
