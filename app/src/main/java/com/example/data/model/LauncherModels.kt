package com.example.data.model

enum class ThemeType(val displayName: String, val description: String) {
    CYBER_MATRIX("Cyber Neon Matrix", "Grade digital futurista com feixes de dados e pulsos cibernéticos interativos."),
    COSMIC_NEBULA("Cosmos & Estrelas", "Nebulosa cósmica profunda com poeira estelar e estrelas cadentes ao toque."),
    ZEN_AURORA("Aurora Zen Líquida", "Ondas bioluminescentes iridescentes e orbes fluidos calmantes."),
    RETRO_ARCADE("Retro 80s Arcade", "Grade synthwave clássica, sol neon retrô e efeitos de sintetizador."),
    BIO_FOREST("Floresta Bio-Luminosa", "Noite encantada com vaga-lumes mágicos interativos que reagem ao toque.")
}

enum class ClockType(val displayName: String) {
    MINIMAL_DIGITAL("Digital Minimalista"),
    CYBER_HUD("HUD Cyberpunk"),
    ANALOG_DIAL("Analógico Elegante"),
    RETRO_FLIP("Flip Clock Retrô"),
    TYPOGRAPHY("Texto Tipográfico"),
    GLASS_PILL("Cápsula Glassmorphic")
}

enum class FontType(val displayName: String) {
    SANS("Moderna Sans"),
    MONOSPACE("Cyber Monospace"),
    SERIF("Editorial Serif"),
    CURSIVE("Script Caligráfica"),
    ROUNDED("Arredondada Pop")
}

enum class IconShape(val displayName: String) {
    SQUIRCLE("Squircle"),
    CIRCLE("Círculo"),
    ROUNDED_SQUARE("Quadrado Suave"),
    HEXAGON("Hexágono"),
    TEARDROP("Gota")
}

enum class AppGridLayout(val displayName: String, val description: String) {
    SPHERE_3D("Esfera 3D Interativa", "Esfera tridimensional giratória com física de inércia, profundidade e rotação por toque."),
    CYLINDER_CAROUSEL("Cilindro 3D Holográfico", "Carrossel circular rotativo em anel 3D com perspectiva curva."),
    HELIX_SPIRAL("Espiral Helix Cósmica", "Vórtice em espiral tridimensional que gira e desliza com fluidez."),
    WAVE_RIBBON("Onda Flutuante Dinâmica", "Fita senoidal flutuante com levitação harmônica e resposta ao toque."),
    FLOATING_GRID("Grade Holográfica Flutuante", "Grade com inclinação 3D dinâmica, elevação e física magnética.")
}

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String = "",
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false,
    val clickCount: Int = 0
) {
    val displayLabel: String
        get() = if (customLabel.isNotBlank()) customLabel else label

    val uniqueKey: String
        get() = "$packageName/$activityName"
}

data class WeatherInfo(
    val temperature: Int = 24,
    val condition: String = "Ensolarado",
    val highTemp: Int = 28,
    val lowTemp: Int = 18,
    val city: String = "São Paulo",
    val iconEmoji: String = "☀️",
    val humidity: Int = 62,
    val windSpeed: Int = 14
)

data class WidgetConfig(
    val showMusicPlayer: Boolean = true,
    val showQuickNotes: Boolean = false,
    val showDeviceMonitor: Boolean = true,
    val showQuickToggles: Boolean = true,
    val showForecastCard: Boolean = false,
    val stickyNoteText: String = "Lembrete: Personalizar meus ícones e temas!"
)

data class WallpaperPreset(
    val id: String,
    val name: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
)

val PRESET_WALLPAPERS = listOf(
    WallpaperPreset("default_cyber", "Cyberpunk Noir", 0xFF0D1117, 0xFF161B22),
    WallpaperPreset("nebula_dark", "Nebulosa Cósmica", 0xFF0B0D1B, 0xFF1E1435),
    WallpaperPreset("aurora_deep", "Aurora Nórdica", 0xFF061A24, 0xFF0F383D),
    WallpaperPreset("sunset_synth", "Pôr do Sol Synthwave", 0xFF240E29, 0xFF4A154B),
    WallpaperPreset("emerald_night", "Noite Esmeralda", 0xFF091F18, 0xFF0E382B)
)

data class ThirdPartyTheme(
    val id: String,
    val name: String,
    val accentColorHex: Long,
    val shape: IconShape,
    val font: FontType,
    val author: String = "Comunidade Lili"
)

val PRESET_THIRD_PARTY_THEMES = listOf(
    ThirdPartyTheme("theme_tokyo", "Tokyo Cyber Drift", 0xFF00E5FF, IconShape.HEXAGON, FontType.MONOSPACE),
    ThirdPartyTheme("theme_nord", "Nordic Frost Minimal", 0xFF88C0D0, IconShape.SQUIRCLE, FontType.SANS),
    ThirdPartyTheme("theme_solar", "Solar Gold Luxury", 0xFFFFD700, IconShape.CIRCLE, FontType.SERIF),
    ThirdPartyTheme("theme_bubble", "Pastel Pop Fantasy", 0xFFFF80AB, IconShape.ROUNDED_SQUARE, FontType.ROUNDED)
)
