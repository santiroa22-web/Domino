package com.udistrital.domino.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.domino.data.RegistroPartida

@Composable
fun HistorialScreen(
    partidas: List<RegistroPartida>,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text("Historial de partidas", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(16.dp))

            if (partidas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no has jugado partidas.", color = Color.White.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(partidas, key = { it.id }) { p -> TarjetaHistorial(p) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaHistorial(p: RegistroPartida) {
    val colorResultado = if (p.gano) VerdeBotonJugar else Color(0xFFFF6E6E)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x33000000))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(colorResultado.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (p.gano) Icons.Default.EmojiEvents else Icons.Default.Person,
                contentDescription = null,
                tint = colorResultado
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(if (p.gano) "Victoria" else "Derrota", color = colorResultado, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text("vs ${p.oponente}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${p.misPuntos} - ${p.puntosOponente}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(p.fecha, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
        }
    }
}
