package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.domino.AppConfig
import com.udistrital.domino.data.HistorialFirebaseRepository
import com.udistrital.domino.data.HistorialLocalRepository
import com.udistrital.domino.data.HistorialRepository
import com.udistrital.domino.data.RegistroPartida
import kotlinx.coroutines.launch

/** ViewModel de la pantalla "Historial de partidas". */
class HistorialViewModel(
    private val repo: HistorialRepository =
        if (AppConfig.MODO_EN_LINEA) HistorialFirebaseRepository() else HistorialLocalRepository()
) : ViewModel() {

    var partidas by mutableStateOf<List<RegistroPartida>>(emptyList())
        private set

    var cargando by mutableStateOf(false)
        private set

    /** Se llama cada vez que se abre la pantalla para traer lo último de Firestore. */
    fun cargar() {
        viewModelScope.launch {
            cargando = true
            partidas = runCatching { repo.obtenerHistorial() }.getOrDefault(emptyList())
            cargando = false
        }
    }
}
