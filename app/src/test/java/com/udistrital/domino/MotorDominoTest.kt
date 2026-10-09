package com.udistrital.domino

import com.udistrital.domino.logica.Casilla
import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.EstrategiaIA
import com.udistrital.domino.logica.FaseJuego
import com.udistrital.domino.logica.Ficha
import com.udistrital.domino.logica.Jugador
import com.udistrital.domino.logica.MotorDomino
import com.udistrital.domino.logica.Movimiento
import com.udistrital.domino.logica.Posicion
import com.udistrital.domino.logica.TipoCasilla
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias del motor del juego (lógica pura, sin Android).
 * Validan las reglas principales de "Dominó Conquista".
 */
class MotorDominoTest {

    private fun tableroNormal(filas: Int, cols: Int): List<List<Casilla>> =
        (0 until filas).map { f -> (0 until cols).map { c -> Casilla(Posicion(f, c), TipoCasilla.NORMAL) } }

    private fun estado(
        tablero: List<List<Casilla>>,
        jugadores: List<Jugador>,
        turno: Int = 0,
        pozo: List<Ficha> = emptyList(),
        ronda: Int = 1,
        max: Int = 20
    ) = EstadoJuego(tablero, jugadores, turno, pozo, ronda, max, FaseJuego.EN_CURSO)

    @Test
    fun hay28FichasYSieteDobles() {
        val fichas = MotorDomino.crearFichas()
        assertEquals(28, fichas.size)
        assertEquals(7, fichas.count { it.esDoble })
        assertEquals(28, fichas.map { it.id }.toSet().size) // ids únicos
    }

    @Test
    fun nuevaPartidaReparteSieteFichasYLlenaElPozo() {
        val e = MotorDomino.nuevaPartida(
            listOf(Jugador("p1", "Ana", 0), Jugador("p2", "Beto", 0)),
            seed = 7L
        )
        assertTrue(e.jugadores.all { it.mano.size == 7 })
        assertEquals(28 - 14, e.pozo.size)
    }

    @Test
    fun primeraFichaEsValidaEnCualquierCasilla() {
        val e = MotorDomino.nuevaPartida(
            listOf(Jugador("p1", "Ana", 0), Jugador("p2", "Beto", 0)),
            seed = 7L
        )
        val f = e.jugadorEnTurno.mano.first()
        assertEquals(25, MotorDomino.posicionesValidas(e, f, "p1").size)
    }

    @Test
    fun segundaFichaDebeConectarYSerAdyacente() {
        val tab = tableroNormal(3, 3).map { it.toMutableList() }.toMutableList()
        tab[0][0] = tab[0][0].copy(ficha = Ficha(0, 3, 5), conquistadaPor = "p1")
        val e = estado(
            tab,
            listOf(
                Jugador("p1", "A", 0, mano = listOf(Ficha(1, 5, 2), Ficha(2, 1, 6))),
                Jugador("p2", "B", 0)
            )
        )
        // 5|2 comparte el 5 con 3|5 y es adyacente -> válido
        assertTrue(MotorDomino.movimientoValido(e, Ficha(1, 5, 2), Posicion(0, 1), "p1"))
        // 1|6 es adyacente pero no comparte valor -> inválido
        assertFalse(MotorDomino.movimientoValido(e, Ficha(2, 1, 6), Posicion(0, 1), "p1"))
        // casilla no adyacente -> inválido
        assertFalse(MotorDomino.movimientoValido(e, Ficha(1, 5, 2), Posicion(2, 2), "p1"))
    }

    @Test
    fun fortalezaRequiereControlarUnTerritorioAdyacente() {
        val tab = tableroNormal(1, 3).map { it.toMutableList() }.toMutableList()
        tab[0][1] = tab[0][1].copy(tipo = TipoCasilla.FORTALEZA)
        tab[0][0] = tab[0][0].copy(ficha = Ficha(0, 3, 3), conquistadaPor = "p1") // p1 controla vecino
        val e = estado(
            tab,
            listOf(
                Jugador("p1", "A", 0, mano = listOf(Ficha(1, 3, 4))),
                Jugador("p2", "B", 0, mano = listOf(Ficha(2, 3, 0)))
            )
        )
        // p1 controla el vecino -> puede conquistar la fortaleza
        assertTrue(MotorDomino.movimientoValido(e, Ficha(1, 3, 4), Posicion(0, 1), "p1"))
        // p2 no controla vecino -> no puede, aunque la ficha conecte
        val e2 = e.copy(turnoActual = 1)
        assertFalse(MotorDomino.movimientoValido(e2, Ficha(2, 3, 0), Posicion(0, 1), "p2"))
    }

    @Test
    fun puntajeSumaBaseMasEspeciales() {
        val tab = tableroNormal(1, 3).map { it.toMutableList() }.toMutableList()
        tab[0][0] = tab[0][0].copy(tipo = TipoCasilla.TESORO, ficha = Ficha(0, 0, 0), conquistadaPor = "p1")
        tab[0][1] = tab[0][1].copy(tipo = TipoCasilla.TRAMPA, ficha = Ficha(1, 0, 1), conquistadaPor = "p1")
        tab[0][2] = tab[0][2].copy(ficha = Ficha(2, 0, 2), conquistadaPor = "p2")
        val e = estado(tab, listOf(Jugador("p1", "A", 0), Jugador("p2", "B", 0)))
        // p1: tesoro(1+5) + trampa(1-2) = 6 - 1 = 5
        assertEquals(5, MotorDomino.puntaje(e, "p1"))
        assertEquals(1, MotorDomino.puntaje(e, "p2"))
        assertEquals(5, MotorDomino.bonos(e, "p1"))
        assertEquals(-2, MotorDomino.penalizaciones(e, "p1"))
        assertEquals(2, MotorDomino.territoriosConquistados(e, "p1"))
    }

    @Test
    fun aplicarMovimientoConquistaSacaDeManoYPasaTurno() {
        val f = Ficha(0, 6, 6)
        val e = estado(
            tableroNormal(3, 3),
            listOf(
                Jugador("p1", "A", 0, mano = listOf(f, Ficha(1, 1, 2))),
                Jugador("p2", "B", 0, mano = listOf(Ficha(2, 3, 4)))
            ),
            pozo = listOf(Ficha(9, 5, 5)) // evita que la partida se bloquee
        )
        val e2 = MotorDomino.aplicarMovimiento(e, Movimiento(f, Posicion(1, 1)), "p1")
        assertEquals("p1", e2.casilla(Posicion(1, 1))!!.conquistadaPor)
        assertTrue(e2.jugador("p1")!!.mano.none { it.id == 0 })
        assertEquals(1, e2.turnoActual)
    }

    @Test
    fun partidaTerminaCuandoUnJugadorSeQuedaSinFichas() {
        val f = Ficha(0, 2, 2)
        val e = estado(
            tableroNormal(3, 3),
            listOf(
                Jugador("p1", "A", 0, mano = listOf(f)),
                Jugador("p2", "B", 0, mano = listOf(Ficha(1, 0, 0)))
            )
        )
        val e2 = MotorDomino.aplicarMovimiento(e, Movimiento(f, Posicion(1, 1)), "p1")
        assertTrue(e2.terminada)
    }

    @Test
    fun resultadoEligeGanadorPorPuntaje() {
        val tab = tableroNormal(1, 2).map { it.toMutableList() }.toMutableList()
        tab[0][0] = tab[0][0].copy(tipo = TipoCasilla.TESORO, ficha = Ficha(0, 0, 0), conquistadaPor = "p1")
        tab[0][1] = tab[0][1].copy(ficha = Ficha(1, 0, 0), conquistadaPor = "p2")
        val e = estado(tab, listOf(Jugador("p1", "A", 0), Jugador("p2", "B", 0)))
            .copy(fase = FaseJuego.FINALIZADA)
        val r = MotorDomino.resultado(e)
        assertEquals("p1", r.ganadorId)
        assertFalse(r.empate)
    }

    @Test
    fun iaSiempreSugiereMovimientoValido() {
        val e = MotorDomino.nuevaPartida(
            listOf(Jugador("p1", "A", 0, esIA = true), Jugador("p2", "B", 0, esIA = true)),
            seed = 99L
        )
        val mov = EstrategiaIA().mejorMovimiento(e, "p1")
        assertNotNull(mov)
        assertTrue(MotorDomino.movimientoValido(e, mov!!.ficha, mov.posicion, "p1"))
    }

    @Test
    fun unaPartidaCompletaEntreDosIAsTermina() {
        val ia = EstrategiaIA()
        var e = MotorDomino.nuevaPartida(
            listOf(Jugador("p1", "A", 0, esIA = true), Jugador("p2", "B", 0, esIA = true)),
            seed = 123L
        )
        var pasos = 0
        while (!e.terminada && pasos < 2000) {
            pasos++
            val id = e.jugadorEnTurno.id
            e = if (e.accionPortalPendiente) {
                val d = ia.decidirPortal(e, id)
                if (d != null) MotorDomino.usarPortal(e, d.first, d.second, id) else MotorDomino.omitirPortal(e)
            } else {
                val mov = ia.mejorMovimiento(e, id)
                if (mov != null) MotorDomino.aplicarMovimiento(e, mov, id) else MotorDomino.robarOPasar(e, id)
            }
        }
        assertTrue(e.terminada)
    }
}
