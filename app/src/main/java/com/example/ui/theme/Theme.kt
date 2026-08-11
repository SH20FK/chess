package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ExpressiveLightColorScheme = lightColorScheme(
  primary = Color(0xFF5C6BC0),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE8EAF6),
  onPrimaryContainer = Color(0xFF1A237E),
  secondary = Color(0xFF78909C),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFECEFF1),
  onSecondaryContainer = Color(0xFF263238),
  tertiary = Color(0xFF81C784),
  onTertiary = Color.White,
  background = PastelCream,
  onBackground = PastelOnSurface,
  surface = PastelSurface,
  onSurface = PastelOnSurface,
  surfaceVariant = PastelSurfaceContainer,
  onSurfaceVariant = PastelOnSurfaceVariant,
  outline = Color(0xFFB0BEC5)
)

private val ExpressiveDarkColorScheme = darkColorScheme(
  primary = Color(0xFF9FA8DA),
  onPrimary = Color(0xFF1A237E),
  primaryContainer = Color(0xFF3949AB),
  onPrimaryContainer = Color(0xFFE8EAF6),
  secondary = Color(0xFFB0BEC5),
  onSecondary = Color(0xFF263238),
  background = Color(0xFF191C1E),
  onBackground = Color(0xFFE1E2E5),
  surface = Color(0xFF191C1E),
  onSurface = Color(0xFFE1E2E5),
  surfaceVariant = Color(0xFF2D3133),
  onSurfaceVariant = Color(0xFFC1C7CE)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to clean, modern pastel light theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) ExpressiveDarkColorScheme else ExpressiveLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
