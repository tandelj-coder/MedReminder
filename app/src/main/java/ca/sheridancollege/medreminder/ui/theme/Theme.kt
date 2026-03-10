package ca.sheridancollege.medreminder.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = F1Red,
    secondary = F1Teal,
    tertiary = F1Orange,
    background = DeepPaddock,
    surface = CardDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = TextGray
)

private val LightColorScheme = lightColorScheme(
    primary = F1Red,
    secondary = F1Teal,
    background = CardLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextBlack,
    onSurface = TextBlack,
    outline = TextGray
)

@Composable
fun MedReminderTheme(
    darkTheme: Boolean = true, // Default to dark for the F1 aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MedReminderTypography,
        content = content
    )
}
