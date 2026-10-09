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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
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
import com.udistrital.domino.model.EstadoSalaEsperaUI

@Composable
fun SalaEsperaScreen(
    state: EstadoSalaEsperaUI,
    onComenzar: () -> Unit,
    onCancelar: () -> Unit,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text("Sala de partida", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(24.dp))

            // Tarjeta del código de sala
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Código de sala", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.codigo,
                        color = AmarilloDorado,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 8.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Comparte este código con tu amigo para que se una a la partida.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // Jugadores en la sala
            FilaJugadorSala(nombre = state.nombreAnfitrion, listo = true, conectado = true)
            Spacer(Modifier.height(12.dp))
            FilaJugadorSala(nombre = state.nombreOponente, listo = state.oponenteConectado, conectado = state.oponenteConectado)

            Spacer(Modifier.weight(1f))

            if (state.oponenteConectado) {
                BotonArcadePrincipal(texto = "Comenzar partida", onClick = onComenzar)
            } else {
                Text(
                    "Esperando a que se una el oponente...",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Cancelar partida",
                color = Color(0xFFFF5252),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCancelar() }
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun FilaJugadorSala(nombre: String, listo: Boolean, conectado: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x33000000))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(if (conectado) VerdeBotonJugar else Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Text(nombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (conectado) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(VerdeBotonJugar.copy(alpha = 0.25f))
                    .border(1.dp, VerdeBotonJugar, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(if (listo) "Listo" else "Conectado", color = Color(0xFF9CFF9C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AmarilloDorado, strokeWidth = 2.dp)
        }
    }
}
