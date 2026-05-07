package com.reeb.controlmonotributoar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color

// 🇦🇷 Paleta oficial bandera argentina
private val DarkColorScheme = darkColorScheme(
    primary              = CelesteArgentino,
    onPrimary            = BlancoArgentino,
    primaryContainer     = CelesteMuyOscuro,
    onPrimaryContainer   = CelesteArgentino2,
    secondary            = SolAmarillo,
    onSecondary          = Color(0xFF1A1100),
    secondaryContainer   = Color(0xFF4A3400),
    onSecondaryContainer = SolAmarillo2,
    background           = Color(0xFF070F1C),
    surface              = Color(0xFF0E1E32),
    surfaceVariant       = Color(0xFF162840),
    onBackground         = BlancoArgentino,
    onSurface            = BlancoArgentino,
    onSurfaceVariant     = CelesteArgentino2,
    outline              = CelesteOscuro,
    outlineVariant       = Color(0xFF1E3558),
    error                = Color(0xFFFF6B6B),
    errorContainer       = Color(0xFF5C1A1A),
    onError              = BlancoArgentino,
    onErrorContainer     = Color(0xFFFF6B6B)
)

private val LightColorScheme = lightColorScheme(
    primary              = CelesteOscuro,        // #4A86C8 — botones, FAB, icons activos
    onPrimary            = BlancoArgentino,
    primaryContainer     = CelesteSuperficie,
    onPrimaryContainer   = CelesteMuyOscuro,
    secondary            = SolAmarillo,          // acentos dorados
    onSecondary          = SolMarron,
    secondaryContainer   = SolSuave,
    onSecondaryContainer = SolMarron,
    tertiary             = VerdeMedio,
    onTertiary           = BlancoArgentino,
    tertiaryContainer    = VerdeClaro,
    onTertiaryContainer  = VerdeExito,
    background           = BlancoSuave,          // #F0F6FF fondo global
    surface              = BlancoArgentino,      // cards, sheets
    surfaceVariant       = GrisCeleste,          // #CCDFF4 superficies secundarias
    onBackground         = TextoOscuro,
    onSurface            = TextoOscuro,
    onSurfaceVariant     = TextoMedio,
    outline              = CelesteBorde,
    outlineVariant       = GrisClaro,
    error                = RojoMedio,
    errorContainer       = RojoClaro,
    onError              = BlancoArgentino,
    onErrorContainer     = RojoError,
    inversePrimary       = CelesteArgentino2,
    inverseSurface       = CelesteMuyOscuro,
    inverseOnSurface     = BlancoArgentino
)

@Composable
fun LímiteMonotributoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isCompact = LocalConfiguration.current.screenWidthDp < 360
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = if (isCompact) CompactTypography else AppTypography,
        content     = content
    )
}