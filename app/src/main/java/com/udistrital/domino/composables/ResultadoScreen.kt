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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.domino.viewmodel.JuegoViewModel

@Composable
fun ResultadoScreen(
    vm: JuegoViewModel,
    onJugarDeNuevo: () -> Unit,
    onVolverMenu: () -> Unit
) {
    val estado by vm.estado.collectAsState()
    val e = estado
    val resultado = vm.resultado()

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

        if (e == null || resultado == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin datos de la partida", color = Color.White)
            }
            return@Box
        }

        val yo = e.jugador(vm.miId)
        val oponente = e.jugadores.firstOrNull { it.id != vm.miId }

        val titulo = when {
            resultado.empate -> "¡Empate!"
            resultado.ganadorId == vm.miId -> "¡Ganaste!"
            else -> "¡Perdiste!"
        }
        val colorTitulo = when {
            resultado.empate -> AmarilloDorado
            resultado.ganadorId == vm.miId -> AmarilloDorado
            else -> Color(0xFFFF6E6E)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmarilloDorado, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(8.dp))
            Text(titulo, color = colorTitulo, fontSize = 40.sp, fontWeight = FontWeight.Black)

            Spacer(Modifier.height(24.dp))

            // Puntajes de ambos jugadores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (yo != null) ColumnaJugadorResultado(yo.nombre, Color(yo.color), resultado.puntajes[yo.id] ?: 0)
                if (oponente != null) ColumnaJugadorResultado(oponente.nombre, Color(oponente.color), resultado.puntajes[oponente.id] ?: 0)
            }

            Spacer(Modifier.height(20.dp))

            // Tabla comparativa
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                val idYo = yo?.id
                val idOp = oponente?.id
                FilaTabla("Territorios conquistados", resultado.territorios[idYo] ?: 0, resultado.territorios[idOp] ?: 0)
                FilaTabla("Bonos", resultado.bonos[idYo] ?: 0, resultado.bonos[idOp] ?: 0)
                FilaTabla("Penalizaciones", resultado.penalizaciones[idYo] ?: 0, resultado.penalizaciones[idOp] ?: 0)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                Spacer(Modifier.height(6.dp))
                FilaTabla("Total", resultado.puntajes[idYo] ?: 0, resultado.puntajes[idOp] ?: 0, destacar = true)
            }

            Spacer(Modifier.weight(1f))

            BotonArcadePrincipal(texto = "Jugar de nuevo", onClick = onJugarDeNuevo)
            Spacer(Modifier.height(12.dp))
            Text(
                "Volver al menú",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33000000))
                    .padding(vertical = 14.dp)
                    .clickable { onVolverMenu() },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ColumnaJugadorResultado(nombre: String, color: Color, puntos: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(nombre.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("$puntos", color = AmarilloDorado, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("puntos", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

@Composable
private fun FilaTabla(etiqueta: String, valorYo: Int, valorOp: Int, destacar: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$valorYo",
            color = if (destacar) AmarilloDorado else Color.White,
            fontSize = if (destacar) 18.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.Center
        )
        Text(
            etiqueta,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            "$valorOp",
            color = if (destacar) AmarilloDorado else Color.White,
            fontSize = if (destacar) 18.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.Center
        )
    }
}
