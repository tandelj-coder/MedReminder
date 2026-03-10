package ca.sheridancollege.medreminder.ui.theme

import androidx.compose.ui.graphics.Color

// --- Master Racing Expressive Palette ---

val PaddockBlack = Color(0xFF000000)
val PaddockDarkGray = Color(0xFF121212)
val PaddockLightGray = Color(0xFF1E1E1E)

val F1Red = Color(0xFFE10600)
val F1Teal = Color(0xFF00D2BE)
val F1Orange = Color(0xFFFF8700)
val F1Yellow = Color(0xFFFFF500)
val F1Blue = Color(0xFF0042BB)
val F1Purple = Color(0xFF7C3AED)

val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF949498)

// Semantic Overlays
val GlassWhite = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)

// Material 3 Expressive Mappings
val PrimaryDark = F1Red
val OnPrimaryDark = Color.White
val PrimaryContainerDark = Color(0xFF410002)
val OnPrimaryContainerDark = Color(0xFFFFDAD6)

val SecondaryDark = F1Teal
val OnSecondaryDark = Color.Black
val SecondaryContainerDark = Color(0xFF005048)
val OnSecondaryContainerDark = Color(0xFF70F7E7)

val TertiaryDark = F1Orange
val OnTertiaryDark = Color.Black
val TertiaryContainerDark = Color(0xFF633B00)
val OnTertiaryContainerDark = Color(0xFFFFDDB3)

val SurfaceDark = PaddockDarkGray
val OnSurfaceDark = Color.White
val BackgroundDark = PaddockBlack
val OnBackgroundDark = Color.White

// Compatibility Aliases (to prevent unresolved references)
val DeepPaddock = PaddockBlack
val CardDark = PaddockDarkGray
val RacingRed = F1Red
val RacingTeal = F1Teal
val RacingOrange = F1Orange
val RacingBlue = F1Blue
val SoftLavender = Color(0xFFC4B5FD)
val CyberTeal = F1Teal
val MidnightBlue = PaddockBlack
val ErrorRose = F1Red
val PrimaryGradient = listOf(F1Red, F1Purple)
val SurfaceGradient = listOf(PaddockBlack, PaddockDarkGray)
