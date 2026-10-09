package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.domino.data.ConfigPartida
import com.udistrital.domino.data.PartidaLocalRepository
import com.udistrital.domino.data.PartidaRepository
import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.Ficha
import com.udistrital.domino.logica.MotorDomino
import com.udistrital.domino.logica.Movimiento
import com.udistrital.domino.logica.Posicion
import com.udistrital.domino.logica.ResultadoPartida
import com.udistrital.domino.model.EstadoJuegoUI
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel central de la partida. Orquesta la interacción del jugador local con
 * el [PartidaRepository] (hoy local; mañana Firebase) y mantiene el estado de
 * interfaz (ficha seleccionada, modo portal, diálogos).
 *
 * No contiene reglas del juego: todas viven en [MotorDomino]. Aquí solo se
 * traduce lo que toca el usuario en llamadas al repositorio.
 */
class JuegoViewModel : ViewModel() {

    // Para conectar Firebase, basta cambiar esta línea por el repositorio remoto.
    private val repo: PartidaRepository = PartidaLocalRepository(viewModelScope)

    /** Estado lógico observable de la partida. */
    val estado: StateFlow<EstadoJuego?> = repo.estado

    /** Estado de interfaz (selección, portal, diálogos). */
    var ui by mutableStateOf(EstadoJuegoUI())
        private set

    val miId: String get() = repo.miId
    val codigoSala: String get() = repo.codigoSala

    fun iniciar(config: ConfigPartida) {
        ui = EstadoJuegoUI()
        repo.iniciar(config)
    }

    // -------- Selección de ficha --------

    /** Ficha actualmente seleccionada de la mano del jugador local. */
    fun fichaSeleccionada(): Ficha? {
        val id = ui.fichaSeleccionadaId ?: return null
        return manoLocal().firstOrNull { it.id == id }?.let { it.copy(rotada = it.id in ui.rotadas) }
    }

    fun manoLocal(): List<Ficha> = estado.value?.jugador(miId)?.mano.orEmpty()

    fun esMiTurno(): Boolean {
        val e = estado.value ?: return false
        return !e.terminada && e.jugadorEnTurno.id == miId
    }

    fun seleccionarFicha(id: Int) {
        if (!esMiTurno() || ui.modoPortal) return
        ui = ui.copy(fichaSeleccionadaId = if (ui.fichaSeleccionadaId == id) null else id)
    }

    /** Rota (visualmente) la ficha seleccionada. */
    fun rotarFicha() {
        val id = ui.fichaSeleccionadaId ?: return
        val nuevas = ui.rotadas.toMutableSet()
        if (!nuevas.add(id)) nuevas.remove(id)
        ui = ui.copy(rotadas = nuevas)
    }

    fun cancelarSeleccion() {
        ui = ui.copy(fichaSeleccionadaId = null)
    }

    // -------- Posiciones resaltadas --------

    /** Posiciones válidas para resaltar según el contexto (colocar o portal). */
    fun posicionesValidas(): Set<Posicion> {
        val e = estado.value ?: return emptySet()
        if (!esMiTurno()) return emptySet()
        if (e.accionPortalPendiente) {
            val origen = ui.origenPortal ?: return emptySet()
            return MotorDomino.destinosPortal(e, origen, miId).toSet()
        }
        val ficha = fichaSeleccionada() ?: return emptySet()
        return MotorDomino.posicionesValidas(e, ficha, miId).toSet()
    }

    /** Casillas propias que pueden reubicarse cuando hay un portal pendiente. */
    fun origenesPortal(): Set<Posicion> {
        val e = estado.value ?: return emptySet()
        if (!e.accionPortalPendiente || !esMiTurno()) return emptySet()
        return MotorDomino.fichasReubicables(e, miId)
            .filter { MotorDomino.destinosPortal(e, it.posicion, miId).isNotEmpty() }
            .map { it.posicion }
            .toSet()
    }

    // -------- Toque en una casilla --------

    fun onCasillaTap(pos: Posicion) {
        val e = estado.value ?: return
        if (!esMiTurno()) return

        if (e.accionPortalPendiente) {
            if (ui.origenPortal == null) {
                if (pos in origenesPortal()) ui = ui.copy(origenPortal = pos, modoPortal = true)
            } else {
                if (pos in posicionesValidas()) {
                    repo.usarPortal(ui.origenPortal!!, pos)
                    ui = ui.copy(modoPortal = false, origenPortal = null)
                }
            }
            return
        }

        // Colocación normal.
        val ficha = fichaSeleccionada() ?: return
        if (pos in posicionesValidas()) {
            repo.colocarFicha(Movimiento(ficha, pos))
            ui = ui.copy(fichaSeleccionadaId = null)
        }
    }

    // -------- Acciones de controles --------

    /** Coloca la ficha seleccionada si solo hay una posición válida (botón "Colocar"). */
    fun colocarEnUnica() {
        val validas = posicionesValidas()
        if (validas.size == 1) onCasillaTap(validas.first())
    }

    fun robarOPasar() {
        if (!esMiTurno()) return
        repo.robarOPasar()
        ui = ui.copy(fichaSeleccionadaId = null)
    }

    fun omitirPortal() {
        repo.omitirPortal()
        ui = ui.copy(modoPortal = false, origenPortal = null)
    }

    fun reiniciar() {
        ui = EstadoJuegoUI()
        repo.reiniciar()
    }

    fun abandonar() {
        repo.abandonar()
        ui = EstadoJuegoUI()
    }

    // -------- Diálogos --------

    fun mostrarTiposCasilla(v: Boolean) { ui = ui.copy(mostrarTiposCasilla = v) }
    fun mostrarDialogoSalir(v: Boolean) { ui = ui.copy(mostrarDialogoSalir = v) }

    // -------- Resultado --------

    fun resultado(): ResultadoPartida? = estado.value?.let { MotorDomino.resultado(it) }
}
