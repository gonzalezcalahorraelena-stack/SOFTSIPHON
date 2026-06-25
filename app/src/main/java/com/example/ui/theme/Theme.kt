package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = SiphonCyan,
  secondary = SiphonMagenta,
  tertiary = SiphonYellow,
  background = SiphonBgDark,
  surface = SiphonSurfDark,
  surfaceVariant = SiphonSurfaceVariantDark,
  onPrimary = SiphonDark,
  onSecondary = SiphonOnBgDark,
  onBackground = SiphonOnBgDark,
  onSurface = SiphonOnBgDark,
  outline = SiphonBorderDark
)

private val LightColorScheme = lightColorScheme(
  primary = SiphonCyan,
  secondary = SiphonMagenta,
  tertiary = SiphonYellow,
  background = SiphonBgLight,
  surface = SiphonSurfLight,
  onPrimary = SiphonSurfLight,
  onSecondary = SiphonDark,
  onBackground = SiphonOnBgLight,
  onSurface = SiphonOnBgLight,
  outline = SiphonBorderLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Default to false to preserve exact SoftSiphon corporate branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
