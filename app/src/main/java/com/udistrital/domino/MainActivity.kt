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
import com.udistrital.domino.composables.CrearPartidaScreen
import com.udistrital.domino.composables.HistorialScreen
import com.udistrital.domino.composables.JuegoScreen
import com.udistrital.domino.composables.LobbyScreen
import com.udistrital.domino.composables.LoginScreen
import com.udistrital.domino.composables.PantallaInicio
import com.udistrital.domino.composables.RegistroScreen
import com.udistrital.domino.composables.ResultadoScreen
import com.udistrital.domino.composables.SalaEsperaScreen
import com.udistrital.domino.data.ConfigPartida
import com.udistrital.domino.viewmodel.CrearPartidaViewModel
import com.udistrital.domino.viewmodel.HistorialViewModel
import com.udistrital.domino.viewmodel.InicioViewModel
import com.udistrital.domino.viewmodel.JuegoViewModel
import com.udistrital.domino.viewmodel.LobbyViewModel
import com.udistrital.domino.viewmodel.LoginViewModel
import com.udistrital.domino.viewmodel.RegistroViewModel
import com.udistrital.domino.viewmodel.SalaEsperaViewModel

class MainActivity : ComponentActivity() {

    // Instancias de ViewModels
    private val inicioViewModel: InicioViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val registroViewModel: RegistroViewModel by viewModels()
    private val lobbyViewModel: LobbyViewModel by viewModels()
    private val crearPartidaViewModel: CrearPartidaViewModel by viewModels()
    private val salaEsperaViewModel: SalaEsperaViewModel by viewModels()
    private val juegoViewModel: JuegoViewModel by viewModels()
    private val historialViewModel: HistorialViewModel by viewModels()

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
                    lobbyViewModel = lobbyViewModel,
                    crearPartidaViewModel = crearPartidaViewModel,
                    salaEsperaViewModel = salaEsperaViewModel,
                    juegoViewModel = juegoViewModel,
                    historialViewModel = historialViewModel
                )
            }
        }
    }

    private fun ocultarBarrasDelSistema() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
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
    const val CREAR_PARTIDA = "crear_partida"
    const val SALA_ESPERA = "sala_espera"
    const val JUEGO = "juego"
    const val RESULTADO = "resultado"
    const val HISTORIAL = "historial"
}

@Composable
fun AppNavegacion(
    inicioViewModel: InicioViewModel,
    loginViewModel: LoginViewModel,
    registroViewModel: RegistroViewModel,
    lobbyViewModel: LobbyViewModel,
    crearPartidaViewModel: CrearPartidaViewModel,
    salaEsperaViewModel: SalaEsperaViewModel,
    juegoViewModel: JuegoViewModel,
    historialViewModel: HistorialViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO
    ) {
        // 1. PANTALLA DE INICIO
        composable(Rutas.INICIO) {
            PantallaInicio(
                onIniciarSesionClick = { navController.navigate(Rutas.LOGIN) },
                onContinuarInvitadoClick = {
                    inicioViewModel.onEntrarComoInvitado { _ ->
                        navController.navigate(Rutas.LOBBY) {
                            popUpTo(Rutas.INICIO) { inclusive = true }
                        }
                    }
                },
                onCrearCuentaClick = { navController.navigate(Rutas.REGISTRO) }
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
                onVolverInicioClick = { navController.popBackStack() },
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
                onVolverInicioClick = { navController.popBackStack() },
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
                onCrearNuevaPartidaClick = { navController.navigate(Rutas.CREAR_PARTIDA) },
                onUnirsePartidaClick = { lobbyViewModel.mostrarDialogoUnirse(true) },
                onVerPartidasDisponiblesClick = { navController.navigate(Rutas.HISTORIAL) },
                onCodigoChange = { lobbyViewModel.onCodigoChange(it) },
                onConfirmarUnirseClick = {
                    lobbyViewModel.unirseAPartidaPorCodigo { codigo ->
                        val config = ConfigPartida(nombreJugador = lobbyViewModel.uiState.nombreUsuario)
                        salaEsperaViewModel.abrirSala(
                            config = config,
                            nombreJugador = lobbyViewModel.uiState.nombreUsuario,
                            codigoExistente = codigo
                        )
                        navController.navigate(Rutas.SALA_ESPERA)
                    }
                },
                onCerrarDialogoUnirse = { lobbyViewModel.mostrarDialogoUnirse(false) }
            )
        }

        // 5. CREAR PARTIDA
        composable(Rutas.CREAR_PARTIDA) {
            CrearPartidaScreen(
                state = crearPartidaViewModel.uiState,
                opcionesJugadores = crearPartidaViewModel.opcionesJugadores,
                opcionesDificultad = crearPartidaViewModel.opcionesDificultad,
                onNumJugadoresChange = { crearPartidaViewModel.onNumJugadoresChange(it) },
                onDificultadChange = { crearPartidaViewModel.onDificultadChange(it) },
                onExpandirJugadores = { crearPartidaViewModel.expandirJugadores(it) },
                onExpandirDificultad = { crearPartidaViewModel.expandirDificultad(it) },
                onCrearPartida = {
                    val nombre = lobbyViewModel.uiState.nombreUsuario
                    val config = crearPartidaViewModel.construirConfig(nombre)
                    salaEsperaViewModel.abrirSala(config = config, nombreJugador = nombre)
                    navController.navigate(Rutas.SALA_ESPERA)
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // 6. SALA DE ESPERA
        composable(Rutas.SALA_ESPERA) {
            SalaEsperaScreen(
                state = salaEsperaViewModel.uiState,
                onComenzar = {
                    val config = salaEsperaViewModel.configParaIniciar()
                    if (config != null) {
                        juegoViewModel.iniciar(config)
                        navController.navigate(Rutas.JUEGO) {
                            popUpTo(Rutas.LOBBY) { inclusive = false }
                        }
                    }
                },
                onCancelar = {
                    salaEsperaViewModel.cancelar()
                    navController.popBackStack(Rutas.LOBBY, inclusive = false)
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // 7. JUEGO / TABLERO
        composable(Rutas.JUEGO) {
            JuegoScreen(
                vm = juegoViewModel,
                onSalir = {
                    navController.popBackStack(Rutas.LOBBY, inclusive = false)
                },
                onFinPartida = {
                    navController.navigate(Rutas.RESULTADO) {
                        popUpTo(Rutas.JUEGO) { inclusive = true }
                    }
                }
            )
        }

        // 8. RESULTADO
        composable(Rutas.RESULTADO) {
            ResultadoScreen(
                vm = juegoViewModel,
                onJugarDeNuevo = {
                    juegoViewModel.reiniciar()
                    navController.navigate(Rutas.JUEGO) {
                        popUpTo(Rutas.RESULTADO) { inclusive = true }
                    }
                },
                onVolverMenu = {
                    juegoViewModel.abandonar()
                    navController.popBackStack(Rutas.LOBBY, inclusive = false)
                }
            )
        }

        // 9. HISTORIAL
        composable(Rutas.HISTORIAL) {
            HistorialScreen(
                partidas = historialViewModel.partidas,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
