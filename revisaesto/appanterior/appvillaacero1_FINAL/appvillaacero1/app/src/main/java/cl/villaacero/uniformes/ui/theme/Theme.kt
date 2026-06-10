package cl.villaacero.uniformes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandAzul,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = BrandAzulClaro,
    onPrimaryContainer = BrandAzul,
    background = SurfaceLight,
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight
)

@Composable
fun ConfeccionesVillaAceroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
