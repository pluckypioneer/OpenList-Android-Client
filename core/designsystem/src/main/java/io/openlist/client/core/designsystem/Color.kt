package io.openlist.client.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Palette tokens transcribed from DESIGN.md (colors.*). Only tokens are reused;
// the source document is a marketing-site analysis, its components are not.
object OpenListPalette {
    val Primary = Color(0xFF5645D4)
    val PrimaryPressed = Color(0xFF4534B3)
    val PrimaryDeep = Color(0xFF3A2A99)
    val OnPrimary = Color(0xFFFFFFFF)

    val Canvas = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF6F5F4)
    val SurfaceSoft = Color(0xFFFAFAF9)
    val Hairline = Color(0xFFE5E3DF)
    val HairlineSoft = Color(0xFFEDE9E4)
    val HairlineStrong = Color(0xFFC8C4BE)

    val InkDeep = Color(0xFF000000)
    val Ink = Color(0xFF1A1A1A)
    val Charcoal = Color(0xFF37352F)
    val Slate = Color(0xFF5D5B54)
    val Steel = Color(0xFF787671)
    val Stone = Color(0xFFA4A097)
    val Muted = Color(0xFFBBB8B1)

    val Success = Color(0xFF1AAE39)
    val Warning = Color(0xFFDD5B00)
    val Error = Color(0xFFE03131)

    // colors.card-tint-* — pastel plate/chip backgrounds. Paired with the deep
    // "badge-tag" partner inks below (DESIGN.md badge-tag-purple/-orange/-green
    // pattern, extended to the remaining tints with same-hue deep partners).
    val TintPeach = Color(0xFFFFE8D4)
    val TintRose = Color(0xFFFDE0EC)
    val TintMint = Color(0xFFD9F3E1)
    val TintLavender = Color(0xFFE6E0F5)
    val TintSky = Color(0xFFDCECFA)
    val TintYellow = Color(0xFFFEF7D6)
    val TintCream = Color(0xFFF8F5E8)
    val TintGray = Color(0xFFF0EEEC)

    val BrandOrangeDeep = Color(0xFF793400)
    val BrandPinkDeep = Color(0xFFA02E6D)
    val BrandPurple800 = Color(0xFF391C57)
    val BrandGreen = Color(0xFF1AAE39)
    val BrandBrown = Color(0xFF523410)
    val LinkBlue = Color(0xFF0075DE)
    val LinkBlueDeep = Color(0xFF005BAB)

    // colors.brand-navy / on-dark* — hero band family.
    val BrandNavy = Color(0xFF0A1530)
    val BrandNavyMid = Color(0xFF1A2A52)
    val OnDark = Color(0xFFFFFFFF)
    val OnDarkMuted = Color(0xFFA4A097)

    // Brand color spectrum, used only as the hero band's sticky-note dot
    // decoration (DESIGN.md hero-band-dark), never as text or surfaces.
    val BrandPink = Color(0xFFFF64C8)
    val BrandOrange = Color(0xFFDD5B00)
    val BrandPurple = Color(0xFF7B3FF2)
    val BrandTeal = Color(0xFF2A9D99)
    val BrandYellow = Color(0xFFF5D75E)

    // Dark-theme surface ramp: DESIGN.md has no dark tokens (documented Known Gap).
    // Derived as a WARM charcoal family (matching Notion's product dark mode and
    // the warm light-theme neutrals above) — deliberately hue-free, no blue cast
    // (产品级中性化决策, 2026-07-11).
    val DarkCanvas = Color(0xFF191919)
    val DarkCanvasDim = Color(0xFF141414)
    val DarkSurface = Color(0xFF1F1F1F)
    val DarkSurfaceSoft = Color(0xFF262626)
    val DarkContainer = Color(0xFF232323)
    val DarkContainerHigh = Color(0xFF2A2A2A)
    val DarkContainerHighest = Color(0xFF303030)
    val DarkBright = Color(0xFF3B3B3B)
    val DarkHairline = Color(0xFF373737)
    val DarkHairlineStrong = Color(0xFF4D4D4D)
    val DarkInk = Color(0xFFF3F2F0)
    val DarkSlate = Color(0xFFB9B6AE)
    // Muted lavender rather than the old periwinkle #9B8CFF: keeps the brand
    // hue readable on dark without tipping the whole theme blue-purple.
    val DarkPrimary = Color(0xFFB3A6E3)
    val DarkErrorContainer = Color(0xFF4A2B2B)
    val DarkOnErrorContainer = Color(0xFFF2C7C4)
}

// Material 3 fallback palette for Android 10/11; Android 12+ uses Monet.
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5B4BBA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6DEFF),
    onPrimaryContainer = Color(0xFF20104F),
    secondary = Color(0xFF605B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6DFF5),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF286B63),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFACEFE3),
    onTertiaryContainer = Color(0xFF00201B),
    background = Color(0xFFFCF8FF),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFCF8FF),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceTint = Color(0xFF5B4BBA),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F2FA),
    surfaceContainer = Color(0xFFF0ECF4),
    surfaceContainerHigh = Color(0xFFEAE6EE),
    surfaceContainerHighest = Color(0xFFE4E1E9),
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCABDFF),
    onPrimary = Color(0xFF302178),
    primaryContainer = Color(0xFF47339F),
    onPrimaryContainer = Color(0xFFE6DEFF),
    secondary = Color(0xFFCAC3DC),
    onSecondary = Color(0xFF322D41),
    secondaryContainer = Color(0xFF494358),
    onSecondaryContainer = Color(0xFFE6DFF5),
    tertiary = Color(0xFF91D3C7),
    onTertiary = Color(0xFF00382F),
    tertiaryContainer = Color(0xFF075047),
    onTertiaryContainer = Color(0xFFACEFE3),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE7E0E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE7E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceTint = Color(0xFFCABDFF),
    outline = Color(0xFF948F99),
    outlineVariant = Color(0xFF49454F),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
)
/** Semantic colors outside Material3's default ColorScheme slots (success/warning). */
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
)

val LightExtendedColors = ExtendedColors(
    success = Color(0xFF226C37),
    onSuccess = OpenListPalette.OnPrimary,
    warning = Color(0xFF8A4F00),
    onWarning = OpenListPalette.OnPrimary,
)

val DarkExtendedColors = ExtendedColors(
    success = Color(0xFF8DD99C),
    onSuccess = OpenListPalette.InkDeep,
    warning = Color(0xFFFFB870),
    onWarning = OpenListPalette.InkDeep,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
