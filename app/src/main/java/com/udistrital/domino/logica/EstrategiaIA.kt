package com.udistrital.domino.logica

import kotlin.random.Random

/**
 * IA sencilla para el oponente en modo local (sin conexión a Firebase todavía).
 *
 * Estrategia "codiciosa": entre todos los movimientos posibles elige el que da
 * más puntos inmediatos (prefiere Tesoro/Bonus/Fortaleza y evita Trampa). Sirve
 * para que la partida sea jugable y demostrable sin un segundo dispositivo.
 *
 * Cuando exista el modo en línea, este oponente se reemplaza por los movimientos
 * del otro jugador recibidos desde la base de datos; el motor no cambia.
 */
class EstrategiaIA(private val random: Random = Random.Default) {

    /** Mejor movimiento de colocación para [jugadorId], o null si no hay ninguno. */
    fun mejorMovimiento(estado: EstadoJuego, jugadorId: String): Movimiento? {
        val jugador = estado.jugador(jugadorId) ?: return null
        val candidatos = mutableListOf<Pair<Movimiento, Int>>()

        for (ficha in jugador.mano) {
            for (pos in MotorDomino.posicionesValidas(estado, ficha, jugadorId)) {
                val casilla = estado.casilla(pos) ?: continue
                // Valor del movimiento = puntos de la casilla; desempata la suma de la ficha.
                val valor = (MotorDomino.PUNTOS_BASE_TERRITORIO + casilla.tipo.puntos) * 10 + ficha.suma
                candidatos.add(Movimiento(ficha, pos) to valor)
            }
        }
        if (candidatos.isEmpty()) return null
        val mejorValor = candidatos.maxOf { it.second }
        val mejores = candidatos.filter { it.second == mejorValor }.map { it.first }
        return mejores[random.nextInt(mejores.size)]
    }

    /**
     * Decide una acción de portal para la IA: reubica a la casilla de mayor valor
     * si le conviene. Devuelve (origen, destino) o null para omitir.
     */
    fun decidirPortal(estado: EstadoJuego, jugadorId: String): Pair<Posicion, Posicion>? {
        var mejor: Pair<Posicion, Posicion>? = null
        var mejorGanancia = 0
        for (origen in MotorDomino.fichasReubicables(estado, jugadorId)) {
            val valorOrigen = origen.tipo.puntos
            for (destino in MotorDomino.destinosPortal(estado, origen.posicion, jugadorId)) {
                val valorDestino = estado.casilla(destino)?.tipo?.puntos ?: 0
                val ganancia = valorDestino - valorOrigen
                if (ganancia > mejorGanancia) {
                    mejorGanancia = ganancia
                    mejor = origen.posicion to destino
                }
            }
        }
        return mejor
    }
}
