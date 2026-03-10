package iut.but2.Ko_List.ui.theme

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

// ==================
// RANK 1: NOVICE
// ==================
private val Rank1DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark, secondary = SecondaryCyanDark, tertiary = AccentCoralDark,
    background = BgModernDark, surface = SurfaceModernDark,
    onPrimary = Color.White, onSecondary = Color.Black, onTertiary = Color.White,
    onBackground = Color(0xFFF1F5F9), onSurface = Color(0xFFF1F5F9)
)
private val Rank1LightColorScheme = lightColorScheme(
    primary = PrimaryBlue, secondary = SecondaryCyan, tertiary = AccentCoral,
    background = BgModernLight, surface = SurfaceModernLight,
    onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), onSurface = Color(0xFF0F172A)
)

// ==================
// RANK 2: APPRENTI
// ==================
private val Rank2DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenDark, secondary = SecondaryLimeDark, tertiary = AccentTealDark,
    background = BgModernDark, surface = SurfaceModernDark,
    onPrimary = Color.White, onSecondary = Color.Black, onTertiary = Color.White,
    onBackground = Color(0xFFF1F5F9), onSurface = Color(0xFFF1F5F9)
)
private val Rank2LightColorScheme = lightColorScheme(
    primary = PrimaryGreen, secondary = SecondaryLime, tertiary = AccentTeal,
    background = BgModernLight, surface = SurfaceModernLight,
    onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), onSurface = Color(0xFF0F172A)
)

// ==================
// RANK 3: EXPERT
// ==================
private val Rank3DarkColorScheme = darkColorScheme(
    primary = PrimaryOrangeDark, secondary = SecondaryAmberDark, tertiary = AccentRoseDark,
    background = BgModernDark, surface = SurfaceModernDark,
    onPrimary = Color.White, onSecondary = Color.Black, onTertiary = Color.White,
    onBackground = Color(0xFFF1F5F9), onSurface = Color(0xFFF1F5F9)
)
private val Rank3LightColorScheme = lightColorScheme(
    primary = PrimaryOrange, secondary = SecondaryAmber, tertiary = AccentRose,
    background = BgModernLight, surface = SurfaceModernLight,
    onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), onSurface = Color(0xFF0F172A)
)

// ==================
// RANK 4: MAÎTRE
// ==================
private val Rank4DarkColorScheme = darkColorScheme(
    primary = PrimaryMagentaDark, secondary = SecondaryCyanCyberDark, tertiary = AccentPurpleDark,
    background = BgModernDark, surface = SurfaceModernDark,
    onPrimary = Color.White, onSecondary = Color.Black, onTertiary = Color.White,
    onBackground = Color(0xFFF1F5F9), onSurface = Color(0xFFF1F5F9)
)
private val Rank4LightColorScheme = lightColorScheme(
    primary = PrimaryMagenta, secondary = SecondaryCyanCyber, tertiary = AccentPurple,
    background = BgModernLight, surface = SurfaceModernLight,
    onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), onSurface = Color(0xFF0F172A)
)

// ==================
// RANK 5: LÉGENDE
// ==================
private val Rank5DarkColorScheme = darkColorScheme(
    primary = PrimaryGoldDark, secondary = SecondarySlateDark, tertiary = AccentYellowDark,
    background = BgModernDark, surface = SurfaceModernDark,
    onPrimary = Color.White, onSecondary = Color.Black, onTertiary = Color.Black,
    onBackground = Color(0xFFF1F5F9), onSurface = Color(0xFFF1F5F9)
)
private val Rank5LightColorScheme = lightColorScheme(
    primary = PrimaryGold, secondary = SecondarySlate, tertiary = AccentYellow,
    background = BgModernLight, surface = SurfaceModernLight,
    onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    rankTitle: String = "Novice de l'Organisation",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            when (rankTitle) {
                "Apprenti Planificateur" -> if (darkTheme) Rank2DarkColorScheme else Rank2LightColorScheme
                "Expert en Productivité" -> if (darkTheme) Rank3DarkColorScheme else Rank3LightColorScheme
                "Maître des Tâches" -> if (darkTheme) Rank4DarkColorScheme else Rank4LightColorScheme
                "Légende du Temps" -> if (darkTheme) Rank5DarkColorScheme else Rank5LightColorScheme
                else -> if (darkTheme) Rank1DarkColorScheme else Rank1LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
