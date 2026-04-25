package ca.sheridancollege.medreminder.ui.theme

import androidx.compose.ui.graphics.Color

// ── Medical M3 Palette ─────────────────────────────────────────────────────

// Light scheme
val PrimaryLight            = Color(0xFF006874)
val OnPrimaryLight          = Color.White
val PrimaryContainerLight   = Color(0xFF97F0FF)
val OnPrimaryContainerLight = Color(0xFF001F24)

val SecondaryLight            = Color(0xFF4A6267)
val OnSecondaryLight          = Color.White
val SecondaryContainerLight   = Color(0xFFCDE7ED)
val OnSecondaryContainerLight = Color(0xFF051F23)

val TertiaryLight            = Color(0xFF525E7D)
val OnTertiaryLight          = Color.White
val TertiaryContainerLight   = Color(0xFFDAE2FF)
val OnTertiaryContainerLight = Color(0xFF0E1B37)

val BackgroundLight    = Color(0xFFF4FAFB)
val OnBackgroundLight  = Color(0xFF191C1D)
val SurfaceLight       = Color(0xFFFFFFFF)
val OnSurfaceLight     = Color(0xFF191C1D)
val SurfaceVariantLight   = Color(0xFFDBE4E6)
val OnSurfaceVariantLight = Color(0xFF3F484A)
val OutlineLight       = Color(0xFF6F797A)
val ErrorLight         = Color(0xFFBA1A1A)
val OnErrorLight       = Color.White
val ErrorContainerLight   = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// Dark scheme
val PrimaryDark            = Color(0xFF4FD8EB)
val OnPrimaryDark          = Color(0xFF00363D)
val PrimaryContainerDark   = Color(0xFF004F58)
val OnPrimaryContainerDark = Color(0xFF97F0FF)

val SecondaryDark            = Color(0xFFB1CBD1)
val OnSecondaryDark          = Color(0xFF1C3438)
val SecondaryContainerDark   = Color(0xFF324B4F)
val OnSecondaryContainerDark = Color(0xFFCDE7ED)

val TertiaryDark            = Color(0xFFBAC6EA)
val OnTertiaryDark          = Color(0xFF24304D)
val TertiaryContainerDark   = Color(0xFF3B4764)
val OnTertiaryContainerDark = Color(0xFFDAE2FF)

val BackgroundDark    = Color(0xFF191C1D)
val OnBackgroundDark  = Color(0xFFE1E3E3)
val SurfaceDark       = Color(0xFF191C1D)
val OnSurfaceDark     = Color(0xFFE1E3E3)
val SurfaceVariantDark   = Color(0xFF3F484A)
val OnSurfaceVariantDark = Color(0xFFBFC8CA)
val OutlineDark       = Color(0xFF899294)
val ErrorDark         = Color(0xFFFFB4AB)
val OnErrorDark       = Color(0xFF690005)
val ErrorContainerDark   = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ── Semantic aliases used in screens ──────────────────────────────────────
val MedTaken   = Color(0xFF2E7D32)   // green  — dose taken
val MedMissed  = Color(0xFFBA1A1A)   // red    — dose missed / overdue
val MedPending = Color(0xFF006874)   // teal   — scheduled (= primary)
val MedWarning = Color(0xFFF57F17)   // amber  — upcoming / warning

// Keep old aliases so any leftover references compile
val RacingRed    = Color(0xFFBA1A1A)
val RacingTeal   = Color(0xFF006874)
val RacingOrange = Color(0xFFF57F17)
val RacingBlue   = Color(0xFF1B6EAC)
val F1Red        = Color(0xFFBA1A1A)
val F1Teal       = Color(0xFF006874)
val F1Orange     = Color(0xFFF57F17)
val F1Blue       = Color(0xFF1B6EAC)
val F1Purple     = Color(0xFF525E7D)
