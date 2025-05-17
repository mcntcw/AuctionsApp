package com.example.auctionsapp.core.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.auctionsapp.R

val ClashDisplayFontFamily = FontFamily(
    Font(R.font.clash_display_regular, FontWeight.Normal),
    Font(R.font.clash_display_bold, FontWeight.Bold),
    Font(R.font.clash_display_light, FontWeight.Light)
)

val CustomTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ClashDisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ClashDisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE5E5E5),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF494949),
    surface = Color(0xFF2D2D2D),
    onPrimary = Color(0xFF2D2D2D),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFFDCDCDC),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF545454),
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFC0C0C0),
    surface = Color(0xFF282828),
    onPrimary = Color(0xFF595959),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),

)

@Composable
fun AuctionsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CustomTypography,
        content = content
    )
}