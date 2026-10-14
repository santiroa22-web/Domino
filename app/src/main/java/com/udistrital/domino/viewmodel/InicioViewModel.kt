package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.domino.data.AuthFirebaseRepository
import com.udistrital.domino.data.AuthRepository
import com.udistrital.domino.data.ResultadoAuth
import com.udistrital.domino.data.Usuario
import com.udistrital.domino.model.EstadoInicioUI
import kotlinx.coroutines.launch

/** ViewModel de la pantalla de inicio (sesión guardada e ingreso como invitado). */
class InicioViewModel(
    private val auth: AuthRepository = AuthFirebaseRepository()
) : ViewModel() {

    var uiState by mutableStateOf(EstadoInicioUI())
        private set

    /** Usuario con sesión ya iniciada en este dispositivo (Firebase la recuerda). */
    val usuarioActual: Usuario? get() = auth.usuarioActual

    /** Entra con una cuenta anónima de Firebase (requiere habilitar "Anónimo"). */
    fun onEntrarComoInvitado(onExito: (Usuario) -> Unit) {
        viewModelScope.launch {
            when (val r = auth.entrarComoInvitado()) {
                is ResultadoAuth.Exito -> onExito(r.usuario)
                is ResultadoAuth.Error -> uiState = uiState.copy(mensajeError = r.mensaje)
            }
        }
    }

    fun cerrarSesion() = auth.cerrarSesion()
}
