package com.udistrital.domino.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.domino.model.EstadoLobbyUI

@Composable
fun LobbyScreen(
    state: EstadoLobbyUI = EstadoLobbyUI(),
    onCrearNuevaPartidaClick: () -> Unit = {},
    onUnirsePartidaClick: () -> Unit = {},
    onVerPartidasDisponiblesClick: () -> Unit = {},
    onCodigoChange: (String) -> Unit = {},
    onConfirmarUnirseClick: () -> Unit = {},
    onCerrarDialogoUnirse: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FondoGradienteArriba, FondoGradienteCentro, FondoGradienteAbajo)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        FichasFondoDecorativasLlenas()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. ENCABEZADO DE USUARIO (Foto, Nombre y Monedas)
            HeaderUsuario(nombre = state.nombreUsuario, monedas = state.monedas)

            // 2. BOTONES PRINCIPALES DEL MENÚ
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaMenuArcade(
                    titulo = "Nueva partida",
                    subtitulo = "Crea una sala y comparte el código",
                    colorFondo = Color(0xFF2E7D32),
                    icono = Icons.Default.Add,
                    onClick = onCrearNuevaPartidaClick
                )

                TarjetaMenuArcade(
                    titulo = "Unirse a partida",
                    subtitulo = "Ingresa un código de sala",
                    colorFondo = Color(0xFF1565C0),
                    icono = Icons.Default.QrCodeScanner,
                    onClick = onUnirsePartidaClick
                )

                TarjetaMenuArcade(
                    titulo = "Partidas disponibles",
                    subtitulo = "Únete a una partida abierta",
                    colorFondo = Color(0xFF6A1B9A),
                    icono = Icons.Default.Group,
                    onClick = onVerPartidasDisponiblesClick
                )
            }

            // 3. BARRA INFERIOR DE NAVEGACIÓN
            BarraNavegacionInferior()
        }

        // Pop-up para ingresar código de partida
        if (state.mostrandoDialogoUnirse) {
            DialogoIngresarCodigo(
                codigo = state.codigoIngreso,
                mensajeError = state.mensajeError,
                onCodigoChange = onCodigoChange,
                onConfirmar = onConfirmarUnirseClick,
                onDismiss = onCerrarDialogoUnirse
            )
        }
    }
}

@Composable
fun HeaderUsuario(nombre: String, monedas: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Perfil
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(AmarilloDorado)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF3E2723),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Jugador", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(text = nombre, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Monedas / Puntos
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x66000000))
                .border(1.5.dp, AmarilloDorado, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AmarilloDorado,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$monedas",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun TarjetaMenuArcade(
    titulo: String,
    subtitulo: String,
    colorFondo: Color,
    icono: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "escalaMenu"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(if (isPressed) 3.dp else 10.dp, RoundedCornerShape(24.dp))
            .border(2.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitulo,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun BarraNavegacionInferior() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC000000)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, AmarilloDorado.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = "Inicio", tint = AmarilloDorado, modifier = Modifier.size(28.dp))
            Icon(imageVector = Icons.Default.Group, contentDescription = "Historial", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
            Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun DialogoIngresarCodigo(
    codigo: String,
    mensajeError: String?,
    onCodigoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ingresar a Sala",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "Escribe el código que te dio tu compañero:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = codigo,
                    onValueChange = onCodigoChange,
                    singleLine = true,
                    placeholder = { Text("Ej: A7K3") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmarilloDorado,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                mensajeError?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("UNIRSE", color = AmarilloDorado, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF1B5E46)
    )
}