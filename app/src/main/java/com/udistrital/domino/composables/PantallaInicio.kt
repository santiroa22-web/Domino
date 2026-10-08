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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FondoGradienteArriba = Color(0xFF0D2D23)
val FondoGradienteCentro = Color(0xFF1B5E46)
val FondoGradienteAbajo = Color(0xFF071913)

val VerdeBotonJugar = Color(0xFF4CAF50)
val AmarilloDorado = Color(0xFFFFD54F)
val DoradoBrillante = Color(0xFFFFEA00)
val BlancoSuave = Color(0xFFFFFFFF)

@Composable
fun PantallaInicio(
    onIniciarSesionClick: () -> Unit = {},
    onCrearCuentaClick: () -> Unit = {},
    onContinuarInvitadoClick: () -> Unit = {}
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
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            TituloJuego3DSinFicha()

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 1. Botón Iniciar Sesión
                BotonArcadePrincipal(
                    texto = "Iniciar Sesión",
                    onClick = onIniciarSesionClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. NUEVO BOTÓN: Continuar como invitado
                BotonInvitado(
                    onClick = onContinuarInvitadoClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Enlace inferior a Registro
                val textoRegistro = buildAnnotatedString {
                    append("¿No tienes cuenta? ")
                    withStyle(
                        style = SpanStyle(
                            color = AmarilloDorado,
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Regístrate aquí")
                    }
                }

                Text(
                    text = textoRegistro,
                    color = BlancoSuave,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onCrearCuentaClick() }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun BotonInvitado(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "escalaInvitado"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(if (isPressed) 2.dp else 8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1565C0)) // Azul brillante
            .border(2.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AmarilloDorado,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Continuar como invitado",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TituloJuego3DSinFicha() {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .offset(y = 5.dp)
                .shadow(20.dp, RoundedCornerShape(32.dp), spotColor = DoradoBrillante)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF381000))
                .padding(horizontal = 40.dp, vertical = 22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DOMINÓ", fontSize = 42.sp, fontWeight = FontWeight.Black)
                Text(text = "KINGDOM", fontSize = 30.sp, fontWeight = FontWeight.Black)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2A2A2A), Color(0xFF121212))
                    )
                )
                .border(
                    width = 3.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(DoradoBrillante, AmarilloDorado, Color(0xFF8D5B00))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 38.dp, vertical = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Text(
                        text = "DOMINÓ",
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        modifier = Modifier.offset(y = 3.dp)
                    )
                    Text(
                        text = "DOMINÓ",
                        color = BlancoSuave,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }

                Box {
                    Text(
                        text = "KINGDOM",
                        color = Color.Black.copy(alpha = 0.7f),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 6.sp,
                        modifier = Modifier.offset(y = 2.dp)
                    )
                    Text(
                        text = "KINGDOM",
                        color = AmarilloDorado,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 6.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FichasFondoDecorativasLlenas() {
    Box(modifier = Modifier.fillMaxSize()) {
        FichaDominoDecorativa(modifier = Modifier.offset(x = (-20).dp, y = 40.dp).rotate(-30f), escala = 1.0f)
        FichaDominoDecorativa(modifier = Modifier.align(Alignment.TopEnd).offset(x = 25.dp, y = 70.dp).rotate(35f), escala = 0.9f)
        FichaDominoDecorativa(modifier = Modifier.align(Alignment.CenterStart).offset(x = (-30).dp, y = (-80).dp).rotate(18f), escala = 1.1f)
        FichaDominoDecorativa(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 30.dp, y = (-60).dp).rotate(-22f), escala = 1.0f)
    }
}

@Composable
fun FichaDominoDecorativa(modifier: Modifier = Modifier, escala: Float = 1.0f) {
    Box(
        modifier = modifier
            .scale(escala)
            .size(width = 58.dp, height = 96.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.size(9.dp).background(Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape))
            Box(modifier = Modifier.fillMaxWidth(0.65f).height(2.dp).background(Color.White.copy(alpha = 0.2f)))
            Box(modifier = Modifier.size(9.dp).background(Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape))
        }
    }
}

@Composable
fun BotonArcadePrincipal(texto: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "escala"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(if (isPressed) 3.dp else 12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(VerdeBotonJugar)
            .border(3.dp, Color.White, RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = texto, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}