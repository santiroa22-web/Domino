package com.udistrital.domino.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.udistrital.domino.logica.TipoCasilla

/**
 * Mapea cada [TipoCasilla] a su apariencia (color de terreno, color de acento e
 * icono) para dibujar el tablero. Es la "traducción visual" de la lógica, que
 * vive aparte del motor del juego.
 */
object CasillaUI {

    fun colorTerreno(tipo: TipoCasilla): Color = when (tipo) {
        TipoCasilla.NORMAL -> Color(0xFF5E7D4F)
        TipoCasilla.BONUS -> Color(0xFF6E6433)
        TipoCasilla.TESORO -> Color(0xFF2F6E63)
        TipoCasilla.TRAMPA -> Color(0xFF7A3B36)
        TipoCasilla.FORTALEZA -> Color(0xFF5A5A5A)
        TipoCasilla.PORTAL -> Color(0xFF4A3A6B)
    }

    fun colorAcento(tipo: TipoCasilla): Color = when (tipo) {
        TipoCasilla.NORMAL -> Color(0xFF8FB07A)
        TipoCasilla.BONUS -> Color(0xFFFFD54F)
        TipoCasilla.TESORO -> Color(0xFF4DD0E1)
        TipoCasilla.TRAMPA -> Color(0xFFFF6E6E)
        TipoCasilla.FORTALEZA -> Color(0xFFB0BEC5)
        TipoCasilla.PORTAL -> Color(0xFFB388FF)
    }

    fun icono(tipo: TipoCasilla): ImageVector? = when (tipo) {
        TipoCasilla.NORMAL -> null
        TipoCasilla.BONUS -> Icons.Filled.Star
        TipoCasilla.TESORO -> Icons.Filled.Diamond
        TipoCasilla.TRAMPA -> Icons.Filled.Dangerous
        TipoCasilla.FORTALEZA -> Icons.Filled.Shield
        TipoCasilla.PORTAL -> Icons.Filled.Autorenew
    }

    /** Texto de puntos para mostrar en leyendas (ej. "+5", "-2"). */
    fun textoPuntos(tipo: TipoCasilla): String = when {
        tipo == TipoCasilla.FORTALEZA -> "x2"
        tipo.puntos > 0 -> "+${tipo.puntos}"
        tipo.puntos < 0 -> "${tipo.puntos}"
        else -> ""
    }
}
