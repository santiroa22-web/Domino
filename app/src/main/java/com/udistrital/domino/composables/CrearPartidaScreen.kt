package com.udistrital.domino.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.domino.model.EstadoCrearPartidaUI

@Composable
fun CrearPartidaScreen(
    state: EstadoCrearPartidaUI,
    opcionesJugadores: List<Int>,
    opcionesDificultad: List<String>,
    onNumJugadoresChange: (Int) -> Unit,
    onDificultadChange: (String) -> Unit,
    onExpandirJugadores: (Boolean) -> Unit,
    onExpandirDificultad: (Boolean) -> Unit,
    onCrearPartida: () -> Unit,
    onVolver: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FondoGradienteArriba, FondoGradienteCentro, FondoGradienteAbajo)
                )
            )
    ) {
        FichasFondoDecorativasLlenas()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Barra superior
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text("Nueva partida", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(24.dp))

            // Tarjeta: modo de juego
            TarjetaSeccion {
                Text("Modo de juego", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(VerdeBotonJugar),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(state.modo, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Conquista territorios en el tablero con fichas de dominó.",
                            color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Número de jugadores
            SelectorDesplegable(
                etiqueta = "Número de jugadores",
                icono = Icons.Default.Group,
                valorMostrado = "${state.numJugadores} jugadores" + if (state.numJugadores == 2) " (1 vs 1)" else "",
                expandido = state.expandirJugadores,
                onExpandir = onExpandirJugadores
            ) {
                opcionesJugadores.forEach { n ->
                    DropdownMenuItem(
                        text = { Text("$n jugadores" + if (n == 2) " (1 vs 1)" else "") },
                        onClick = { onNumJugadoresChange(n) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nivel de dificultad
            SelectorDesplegable(
                etiqueta = "Nivel de dificultad",
                icono = Icons.Default.BarChart,
                valorMostrado = state.dificultad,
                expandido = state.expandirDificultad,
                onExpandir = onExpandirDificultad
            ) {
                opcionesDificultad.forEach { d ->
                    DropdownMenuItem(text = { Text(d) }, onClick = { onDificultadChange(d) })
                }
            }

            Spacer(Modifier.weight(1f))

            BotonArcadePrincipal(texto = "Crear partida", onClick = onCrearPartida)
        }
    }
}

@Composable
private fun TarjetaSeccion(contenido: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x33000000))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column { contenido() }
    }
}

@Composable
fun SelectorDesplegable(
    etiqueta: String,
    icono: ImageVector,
    valorMostrado: String,
    expandido: Boolean,
    onExpandir: (Boolean) -> Unit,
    items: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(etiqueta, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33000000))
                    .border(1.5.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .clickable { onExpandir(true) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(valorMostrado, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
            }
            DropdownMenu(expanded = expandido, onDismissRequest = { onExpandir(false) }) {
                items()
            }
        }
    }
}
