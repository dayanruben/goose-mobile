package xyz.block.gosling.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val primaryColor = Color(220, 160, 107)
val secondaryColor = Color(150, 100, 70)
val whiteColor = Color.White
val blackColor = Color.Black

// Extended color palette for the app
class GoslingColors(
    val primaryBackground: Color,
    val secondaryButton: Color,
    val inputBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val borderColor: Color
)

val LocalGoslingColors = staticCompositionLocalOf {
    GoslingColors(
        primaryBackground = primaryColor,
        secondaryButton = secondaryColor,
        inputBackground = whiteColor,
        primaryText = whiteColor,
        secondaryText = blackColor,
        borderColor = secondaryColor
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = primaryColor,
    secondary = secondaryColor,
    tertiary = whiteColor,
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = whiteColor,
    onSecondary = whiteColor,
    onTertiary = blackColor,
    onSurface = whiteColor,
    onBackground = whiteColor
)

private val LightColorScheme = lightColorScheme(
    primary = primaryColor,
    secondary = secondaryColor,
    tertiary = whiteColor,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = blackColor,
    onSecondary = whiteColor,
    onTertiary = blackColor,
    onSurface = blackColor,
    onBackground = blackColor
)

private val Typography = Typography(
    bodyLarge = TextStyle(fontSize = 16.sp),
    titleLarge = TextStyle(fontSize = 20.sp)
)

@Composable
fun GoslingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val goslingColors = GoslingColors(
        primaryBackground = primaryColor,
        secondaryButton = secondaryColor,
        inputBackground = whiteColor,
        primaryText = if (darkTheme) whiteColor else blackColor,
        secondaryText = if (darkTheme) blackColor else whiteColor,
        borderColor = secondaryColor
    )

    CompositionLocalProvider(LocalGoslingColors provides goslingColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}