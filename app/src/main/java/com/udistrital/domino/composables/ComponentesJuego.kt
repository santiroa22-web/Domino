package com.udistrital.domino.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.domino.logica.Casilla
import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.Ficha
import com.udistrital.domino.logica.Posicion
import com.udistrital.domino.logica.TipoCasilla

// =====================================================================
//  COMPONENTES REUTILIZABLES: fichas, casillas, tablero, jugadores
//  (requisito del taller: "utilizar componentes reutilizables")
// =====================================================================

/** Orientación con la que se dibuja una ficha de dominó. */
enum class OrientacionFicha { VERTICAL, HORIZONTAL }

/** Devuelve el color Compose de un jugador a partir de su id. */
fun colorJugador(estado: EstadoJuego, id: String?): Color {
    val c = estado.jugadores.firstOrNull { it.id == id }?.color ?: return Color.Gray
    return Color(c)
}

/** Patrón de puntos (pips) de una cara de dominó en una grilla 3x3. */
private fun patronPips(valor: Int): Set<Pair<Int, Int>> = when (valor) {
    1 -> setOf(1 to 1)
    2 -> setOf(0 to 0, 2 to 2)
    3 -> setOf(0 to 0, 1 to 1, 2 to 2)
    4 -> setOf(0 to 0, 0 to 2, 2 to 0, 2 to 2)
    5 -> setOf(0 to 0, 0 to 2, 1 to 1, 2 to 0, 2 to 2)
    6 -> setOf(0 to 0, 1 to 0, 2 to 0, 0 to 2, 1 to 2, 2 to 2)
    else -> emptySet()
}

/** Una cara de ficha de dominó con sus puntos. */
@Composable
fun CaraDomino(valor: Int, faceSize: Dp, dotColor: Color) {
    val dot = faceSize * 0.17f
    val patron = patronPips(valor)
    Box(modifier = Modifier.size(faceSize), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize().padding(faceSize * 0.15f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (r in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0..2) {
                        Box(
                            modifier = Modifier
                                .size(dot)
                                .clip(CircleShape)
                                .background(if ((r to c) in patron) dotColor else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

/** Ficha de dominó completa (dos caras). Componente reutilizable. */
@Composable
fun FichaDominoView(
    ficha: Ficha,
    modifier: Modifier = Modifier,
    orientacion: OrientacionFicha = OrientacionFicha.VERTICAL,
    faceSize: Dp = 34.dp,
    seleccionada: Boolean = false,
    colorFondo: Color = Color(0xFFF7F1E1)
) {
    val v1 = ficha.valorIzquierdo
    val v2 = ficha.valorDerecho
    val dotColor = Color(0xFF2A2A2A)
    val borde = if (seleccionada) Color(0xFFFFEA00) else Color(0xFF3E2723).copy(alpha = 0.45f)
    val shape = RoundedCornerShape(faceSize * 0.18f)
    val divisor = dotColor.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .shadow(if (seleccionada) 10.dp else 3.dp, shape)
            .clip(shape)
            .background(colorFondo)
            .border(if (seleccionada) 3.dp else 1.5.dp, borde, shape)
            .padding(faceSize * 0.12f),
        contentAlignment = Alignment.Center
    ) {
        if (orientacion == OrientacionFicha.VERTICAL) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CaraDomino(v1, faceSize, dotColor)
                Box(Modifier.width(faceSize * 0.82f).height(1.5.dp).background(divisor))
                CaraDomino(v2, faceSize, dotColor)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CaraDomino(v1, faceSize, dotColor)
                Box(Modifier.height(faceSize * 0.82f).width(1.5.dp).background(divisor))
                CaraDomino(v2, faceSize, dotColor)
            }
        }
    }
}

/** Una casilla/territorio del tablero. Componente reutilizable. */
@Composable
fun CasillaView(
    casilla: Casilla,
    resaltada: Boolean,
    esOrigenPortal: Boolean,
    colorDueno: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    val terreno = CasillaUI.colorTerreno(casilla.tipo)
    val acento = CasillaUI.colorAcento(casilla.tipo)
    val icono = CasillaUI.icono(casilla.tipo)

    val colorBorde = when {
        resaltada -> Color(0xFFFFEA00)
        esOrigenPortal -> Color(0xFFB388FF)
        colorDueno != null -> colorDueno
        else -> Color.Black.copy(alpha = 0.25f)
    }
    val anchoBorde = if (resaltada || esOrigenPortal) 3.dp else if (colorDueno != null) 2.5.dp else 1.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(terreno)
            .border(anchoBorde, colorBorde, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Tinte del dueño del territorio.
        if (colorDueno != null) {
            Box(Modifier.fillMaxSize().background(colorDueno.copy(alpha = 0.30f)))
        }
        when {
            casilla.ocupada -> {
                FichaDominoView(
                    ficha = casilla.ficha!!,
                    orientacion = OrientacionFicha.HORIZONTAL,
                    faceSize = 15.dp
                )
            }
            icono != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icono,
                        contentDescription = casilla.tipo.etiqueta,
                        tint = acento,
                        modifier = Modifier.size(22.dp)
                    )
                    val txt = CasillaUI.textoPuntos(casilla.tipo)
                    if (txt.isNotEmpty()) {
                        Text(txt, color = acento, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/** Tablero completo. Componente reutilizable que compone casillas. */
@Composable
fun TableroView(
    estado: EstadoJuego,
    posicionesValidas: Set<Posicion>,
    origenesPortal: Set<Posicion>,
    origenPortalSeleccionado: Posicion?,
    onTap: (Posicion) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        for (f in 0 until estado.filas) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until estado.columnas) {
                    val pos = Posicion(f, c)
                    val cas = estado.casilla(pos) ?: continue
                    CasillaView(
                        casilla = cas,
                        resaltada = pos in posicionesValidas,
                        esOrigenPortal = pos in origenesPortal || pos == origenPortalSeleccionado,
                        colorDueno = cas.conquistadaPor?.let { colorJugador(estado, it) },
                        onClick = { onTap(pos) },
                        modifier = Modifier.weight(1f).padding(2.dp)
                    )
                }
            }
        }
    }
}

/** Marcador de un jugador (avatar, nombre, territorios y puntaje). Reutilizable. */
@Composable
fun MarcadorJugador(
    nombre: String,
    color: Color,
    puntaje: Int,
    territorios: Int,
    enTurno: Boolean,
    modifier: Modifier = Modifier
) {
    val fondo = if (enTurno) color.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.30f)
    val borde = if (enTurno) color else Color.White.copy(alpha = 0.15f)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(fondo)
            .border(2.dp, borde, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(nombre.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(3.dp))
                Text("$territorios territorios", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.width(10.dp))
        Text("$puntaje", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 22.sp)
    }
}

/** Mano del jugador: fila de fichas seleccionables. Reutilizable. */
@Composable
fun ManoJugador(
    fichas: List<Ficha>,
    seleccionadaId: Int?,
    habilitada: Boolean,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        itemsIndexed(fichas, key = { _, f -> f.id }) { _, ficha ->
            val sel = ficha.id == seleccionadaId
            Box(
                modifier = Modifier
                    .padding(top = if (sel) 0.dp else 8.dp)
                    .clickable(enabled = habilitada) { onSeleccionar(ficha.id) }
            ) {
                FichaDominoView(
                    ficha = ficha,
                    orientacion = OrientacionFicha.VERTICAL,
                    faceSize = 30.dp,
                    seleccionada = sel,
                    colorFondo = if (habilitada) Color(0xFFF7F1E1) else Color(0xFFBDB6A6)
                )
            }
        }
    }
}
