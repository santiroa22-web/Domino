package com.udistrital.domino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.udistrital.domino.composables.LobbyScreen
import com.udistrital.domino.composables.LoginScreen
import com.udistrital.domino.composables.PantallaInicio
import com.udistrital.domino.composables.RegistroScreen
import com.udistrital.domino.viewmodel.InicioViewModel
import com.udistrital.domino.viewmodel.LobbyViewModel
import com.udistrital.domino.viewmodel.LoginViewModel
import com.udistrital.domino.viewmodel.RegistroViewModel

class MainActivity : ComponentActivity() {

    // Instancias de ViewModels
    private val inicioViewModel: InicioViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val registroViewModel: RegistroViewModel by viewModels()
    private val lobbyViewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultar barras de hora, batería y navegación (Pantalla Completa)
        ocultarBarrasDelSistema()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavegacion(
                    inicioViewModel = inicioViewModel,
                    loginViewModel = loginViewModel,
                    registroViewModel = registroViewModel,
                    lobbyViewModel = lobbyViewModel
                )
            }
        }
    }

    private fun ocultarBarrasDelSistema() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Oculta las barras superiores e inferiores
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Si el usuario desliza desde el borde, aparecen de forma temporal y se vuelven a ocultar
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

// Definición de las rutas de la app
object Rutas {
    const val INICIO = "inicio"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val LOBBY = "lobby"
}

@Composable
fun AppNavegacion(
    inicioViewModel: InicioViewModel,
    loginViewModel: LoginViewModel,
    registroViewModel: RegistroViewModel,
    lobbyViewModel: LobbyViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO
    ) {
        // 1. PANTALLA DE INICIO
        composable(Rutas.INICIO) {
            PantallaInicio(
                onIniciarSesionClick = {
                    navController.navigate(Rutas.LOGIN)
                },
                onContinuarInvitadoClick = {
                    inicioViewModel.onEntrarComoInvitado { _ ->
                        navController.navigate(Rutas.LOBBY) {
                            popUpTo(Rutas.INICIO) { inclusive = true }
                        }
                    }
                },
                onCrearCuentaClick = {
                    navController.navigate(Rutas.REGISTRO)
                }
            )
        }

        // 2. PANTALLA DE LOGIN
        composable(Rutas.LOGIN) {
            LoginScreen(
                state = loginViewModel.uiState,
                onCorreoChange = { loginViewModel.onCorreoChange(it) },
                onContrasenaChange = { loginViewModel.onContrasenaChange(it) },
                onIniciarSesionClick = {
                    loginViewModel.iniciarSesion {
                        navController.navigate(Rutas.LOBBY) {
                            popUpTo(Rutas.INICIO) { inclusive = true }
                        }
                    }
                },
                onVolverInicioClick = {
                    navController.popBackStack()
                },
                onIrARegistroClick = {
                    navController.navigate(Rutas.REGISTRO) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 3. PANTALLA DE REGISTRO
        composable(Rutas.REGISTRO) {
            RegistroScreen(
                state = registroViewModel.uiState,
                onNombreChange = { registroViewModel.onNombreChange(it) },
                onCorreoChange = { registroViewModel.onCorreoChange(it) },
                onContrasenaChange = { registroViewModel.onContrasenaChange(it) },
                onConfirmarContrasenaChange = { registroViewModel.onConfirmarContrasenaChange(it) },
                onRegistrarClick = {
                    registroViewModel.registrarUsuario {
                        navController.navigate(Rutas.LOBBY) {
                            popUpTo(Rutas.INICIO) { inclusive = true }
                        }
                    }
                },
                onVolverInicioClick = {
                    navController.popBackStack()
                },
                onIrALoginClick = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.REGISTRO) { inclusive = true }
                    }
                }
            )
        }

        // 4. LOBBY / MENÚ PRINCIPAL
        composable(Rutas.LOBBY) {
            LobbyScreen(
                state = lobbyViewModel.uiState,
                onCrearNuevaPartidaClick = {
                    // Acción para crear nueva partida
                },
                onUnirsePartidaClick = {
                    lobbyViewModel.mostrarDialogoUnirse(true)
                },
                onVerPartidasDisponiblesClick = {
                    // Acción para ver lista de partidas abiertas
                },
                onCodigoChange = { lobbyViewModel.onCodigoChange(it) },
                onConfirmarUnirseClick = {
                    lobbyViewModel.unirseAPartidaPorCodigo { codigo ->
                        // Acción para unirse con código
                    }
                },
                onCerrarDialogoUnirse = {
                    lobbyViewModel.mostrarDialogoUnirse(false)
                }
            )
        }
    }
}