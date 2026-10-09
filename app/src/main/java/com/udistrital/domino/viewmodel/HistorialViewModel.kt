package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.udistrital.domino.data.HistorialLocalRepository
import com.udistrital.domino.data.HistorialRepository
import com.udistrital.domino.data.RegistroPartida

/** ViewModel de la pantalla "Historial de partidas". */
class HistorialViewModel : ViewModel() {

    // Para conectar Firebase, reemplazar por HistorialFirebaseRepository.
    private val repo: HistorialRepository = HistorialLocalRepository()

    var partidas by mutableStateOf<List<RegistroPartida>>(emptyList())
        private set

    init {
        cargar()
    }

    fun cargar() {
        partidas = repo.obtenerHistorial()
    }
}
