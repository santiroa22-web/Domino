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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.domino.logica.MotorDomino
import com.udistrital.domino.logica.TipoCasilla
import com.udistrital.domino.viewmodel.JuegoViewModel

@Composable
fun JuegoScreen(
    vm: JuegoViewModel,
    onSalir: () -> Unit,
    onFinPartida: () -> Unit
) {
    val estado by vm.estado.collectAsState()
    val ui = vm.ui

    // Navega a la pantalla de resultado cuando la partida termina.
    LaunchedEffect(estado?.terminada) {
        if (estado?.terminada == true) onFinPartida()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FondoGradienteArriba, FondoGradienteCentro, FondoGradienteAbajo)
                )
            )
    ) {
        val e = estado
        if (e == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Preparando partida...", color = Color.White, fontSize = 16.sp)
            }
            return@Box
        }

        val yo = e.jugador(vm.miId)
        val oponente = e.jugadores.firstOrNull { it.id != vm.miId }
        val miTurno = vm.esMiTurno()
        val hayMovimiento = MotorDomino.jugadorTieneMovimiento(e, vm.miId)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Barra superior
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { vm.mostrarDialogoSalir(true) }) {
                    Icon(Icons.Default.Close, contentDescription = "Salir", tint = Color.White)
                }
                Text(
                    "Sala ${vm.codigoSala}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { vm.mostrarTiposCasilla(true) }) {
                    Icon(Icons.Default.Info, contentDescription = "Tipos de casillas", tint = Color.White)
                }
            }

            // Marcadores + turno
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (yo != null) {
                    MarcadorJugador(
                        nombre = yo.nombre,
                        color = Color(yo.color),
                        puntaje = MotorDomino.puntaje(e, yo.id),
                        territorios = MotorDomino.territoriosConquistados(e, yo.id),
                        enTurno = e.jugadorEnTurno.id == yo.id,
                        modifier = Modifier.weight(1f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 6.dp)) {
                    Text("Turno", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Box(
                        modifier = Modifier.size(22.dp).clip(CircleShape)
                            .border(3.dp, Color(e.jugadorEnTurno.color), CircleShape)
                    )
                    Text("R${e.ronda}", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                }
                if (oponente != null) {
                    MarcadorJugador(
                        nombre = oponente.nombre,
                        color = Color(oponente.color),
                        puntaje = MotorDomino.puntaje(e, oponente.id),
                        territorios = MotorDomino.territoriosConquistados(e, oponente.id),
                        enTurno = e.jugadorEnTurno.id == oponente.id,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mensaje de estado
            e.mensaje?.let { msg ->
                Text(
                    msg,
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33000000))
                        .padding(vertical = 6.dp, horizontal = 10.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Tablero
            TableroView(
                estado = e,
                posicionesValidas = vm.posicionesValidas(),
                origenesPortal = vm.origenesPortal(),
                origenPortalSeleccionado = ui.origenPortal,
                onTap = { vm.onCasillaTap(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // Zona de controles
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        e.accionPortalPendiente && miTurno -> ControlesPortal(
                            instruccion = if (ui.origenPortal == null)
                                "Portal activado: toca una de tus fichas para moverla."
                            else "Toca una casilla resaltada como destino.",
                            onOmitir = { vm.omitirPortal() }
                        )

                        !miTurno -> Text(
                            "Turno de ${e.jugadorEnTurno.nombre}...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        !hayMovimiento -> BotonControl(
                            texto = "Robar / Pasar turno",
                            icono = Icons.Default.Refresh,
                            color = Color(0xFF1565C0),
                            onClick = { vm.robarOPasar() }
                        )

                        ui.fichaSeleccionadaId != null -> ControlesColocacion(
                            onRotar = { vm.rotarFicha() },
                            onCancelar = { vm.cancelarSeleccion() },
                            onColocar = { vm.colocarEnUnica() }
                        )

                        else -> Text(
                            "Selecciona una ficha de tu mano",
                            color = AmarilloDorado,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // Mano del jugador local
                    if (yo != null) {
                        Text("Tus fichas", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        ManoJugador(
                            fichas = yo.mano,
                            seleccionadaId = ui.fichaSeleccionadaId,
                            habilitada = miTurno && !e.accionPortalPendiente,
                            onSeleccionar = { vm.seleccionarFicha(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (ui.mostrarTiposCasilla) {
            DialogoTiposCasilla(onCerrar = { vm.mostrarTiposCasilla(false) })
        }
        if (ui.mostrarDialogoSalir) {
            AlertDialog(
                onDismissRequest = { vm.mostrarDialogoSalir(false) },
                title = { Text("Salir de la partida") },
                text = { Text("¿Seguro que quieres abandonar la partida actual?") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.mostrarDialogoSalir(false)
                        vm.abandonar()
                        onSalir()
                    }) { Text("Salir", color = Color(0xFFFF5252)) }
                },
                dismissButton = {
                    TextButton(onClick = { vm.mostrarDialogoSalir(false) }) { Text("Seguir jugando") }
                },
                containerColor = Color(0xFF1B5E46)
            )
        }
    }
}

@Composable
private fun ControlesColocacion(onRotar: () -> Unit, onCancelar: () -> Unit, onColocar: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Toca una casilla resaltada para colocar",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonControl(texto = "Colocar", icono = Icons.Default.Check, color = VerdeBotonJugar, onClick = onColocar, compacto = true)
            BotonControl(texto = "Rotar", icono = Icons.Default.RotateRight, color = Color(0xFF6A1B9A), onClick = onRotar, compacto = true)
            BotonControl(texto = "Cancelar", icono = Icons.Default.Close, color = Color(0xFF5A5A5A), onClick = onCancelar, compacto = true)
        }
    }
}

@Composable
private fun ControlesPortal(instruccion: String, onOmitir: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            instruccion,
            color = Color(0xFFB388FF),
            fontSize = 13.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        BotonControl(texto = "Omitir portal", icono = Icons.Default.Close, color = Color(0xFF5A5A5A), onClick = onOmitir, compacto = true)
    }
}

@Composable
private fun BotonControl(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    compacto: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = if (compacto) 14.dp else 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, color = Color.White, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

/** Diálogo con la leyenda de tipos de casilla (mockup 8). */
@Composable
fun DialogoTiposCasilla(onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        confirmButton = { TextButton(onClick = onCerrar) { Text("Entendido", color = AmarilloDorado) } },
        title = { Text("Casillas especiales", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Black) },
        text = {
            Column {
                TipoCasilla.entries.forEach { tipo ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp))
                                .background(CasillaUI.colorTerreno(tipo)),
                            contentAlignment = Alignment.Center
                        ) {
                            CasillaUI.icono(tipo)?.let {
                                Icon(it, contentDescription = null, tint = CasillaUI.colorAcento(tipo), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(tipo.etiqueta, color = CasillaUI.colorAcento(tipo), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 14.sp)
                            Text(tipo.descripcion, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF17352A)
    )
}
