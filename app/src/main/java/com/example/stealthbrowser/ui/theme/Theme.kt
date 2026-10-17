package com.example.stealthbrowser.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = CyanAccent,
  onPrimary = DarkBg,
  primaryContainer = DarkSurfaceHighlight,
  onPrimaryContainer = CyanAccent,
  secondary = ElectricBlue,
  onSecondary = TextPrimary,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = CyanAccentMuted,
  tertiary = PurpleStealth,
  onTertiary = TextPrimary,
  background = DarkBg,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorder.copy(alpha = 0.6f)
)

private val LightColorScheme = lightColorScheme(
  primary = CyanAccentMuted,
  onPrimary = TextPrimary,
  primaryContainer = LightSurfaceElevated,
  onPrimaryContainer = LightTextPrimary,
  secondary = ElectricBlue,
  onSecondary = TextPrimary,
  background = LightBg,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightSurfaceElevated,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  outlineVariant = LightBorder.copy(alpha = 0.6f)
)

val SmoothShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun StealthBrowserTheme(
  darkTheme: Boolean = true, // Default to sleek dark cyber stealth
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = SmoothShapes,
    content = content
  )
}
