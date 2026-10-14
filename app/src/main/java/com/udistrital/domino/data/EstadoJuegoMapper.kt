package com.udistrital.domino.data

import com.udistrital.domino.logica.Casilla
import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.FaseJuego
import com.udistrital.domino.logica.Ficha
import com.udistrital.domino.logica.Jugador
import com.udistrital.domino.logica.MotorDomino
import com.udistrital.domino.logica.Posicion
import com.udistrital.domino.logica.TipoCasilla

/**
 * Traduce [EstadoJuego] <-> Map<String, Any?> para guardarlo en Firestore.
 *
 * Por qué un mapper manual y no `toObject()`:
 *  - Firestore NO admite arrays anidados, y el tablero es List<List<Casilla>>.
 *    Aquí se aplana a una lista de casillas con su fila/columna.
 *  - Los números vuelven como Long, los enums deben guardarse como String y las
 *    data classes del motor no tienen constructor vacío.
 *
 * Es Kotlin puro (sin dependencias de Firebase ni Android), así que se prueba
 * con JUnit normal (ver EstadoJuegoMapperTest).
 */
object EstadoJuegoMapper {

    fun aMapa(e: EstadoJuego): Map<String, Any?> = mapOf(
        "filas" to e.filas,
        "columnas" to e.columnas,
        "turnoActual" to e.turnoActual,
        "turnoDe" to e.jugadorEnTurno.id,
        "ronda" to e.ronda,
        "maxRondas" to e.maxRondas,
        "fase" to e.fase.name,
        "mensaje" to e.mensaje,
        "accionPortalPendiente" to e.accionPortalPendiente,
        "tablero" to e.todasLasCasillas().map(::casillaAMapa),
        "jugadores" to e.jugadores.map(::jugadorAMapa),
        "pozo" to e.pozo.map(::fichaAMapa),
        // Datos derivados (el motor los recalcula), guardados para que el
        // historial / la consola de Firebase los muestren sin recalcular.
        "puntajes" to e.jugadores.associate { it.id to MotorDomino.puntaje(e, it.id) },
        "territorios" to e.jugadores.associate { it.id to MotorDomino.territoriosConquistados(e, it.id) }
    )

    fun desdeMapa(m: Map<String, Any?>): EstadoJuego {
        val filas = m.entero("filas")
        val columnas = m.entero("columnas")
        val porPosicion = m.listaDeMapas("tablero").map(::casillaDesdeMapa).associateBy { it.posicion }
        val tablero = (0 until filas).map { f ->
            (0 until columnas).map { c ->
                porPosicion[Posicion(f, c)] ?: Casilla(Posicion(f, c), TipoCasilla.NORMAL)
            }
        }
        return EstadoJuego(
            tablero = tablero,
            jugadores = m.listaDeMapas("jugadores").map(::jugadorDesdeMapa),
            turnoActual = m.entero("turnoActual"),
            pozo = m.listaDeMapas("pozo").map(::fichaDesdeMapa),
            ronda = m.entero("ronda"),
            maxRondas = m.entero("maxRondas"),
            fase = FaseJuego.valueOf(m["fase"] as String),
            mensaje = m["mensaje"] as String?,
            accionPortalPendiente = m["accionPortalPendiente"] as Boolean? ?: false
        )
    }

    // ---- Ficha ----
    fun fichaAMapa(f: Ficha): Map<String, Any?> =
        mapOf("id" to f.id, "a" to f.ladoA, "b" to f.ladoB, "r" to f.rotada)

    fun fichaDesdeMapa(m: Map<String, Any?>) = Ficha(
        id = m.entero("id"),
        ladoA = m.entero("a"),
        ladoB = m.entero("b"),
        rotada = m["r"] as Boolean? ?: false
    )

    // ---- Casilla ----
    private fun casillaAMapa(c: Casilla): Map<String, Any?> = mapOf(
        "f" to c.posicion.fila,
        "c" to c.posicion.columna,
        "tipo" to c.tipo.name,
        "ficha" to c.ficha?.let(::fichaAMapa),
        "duenio" to c.conquistadaPor
    )

    @Suppress("UNCHECKED_CAST")
    private fun casillaDesdeMapa(m: Map<String, Any?>) = Casilla(
        posicion = Posicion(m.entero("f"), m.entero("c")),
        tipo = TipoCasilla.valueOf(m["tipo"] as String),
        ficha = (m["ficha"] as Map<String, Any?>?)?.let(::fichaDesdeMapa),
        conquistadaPor = m["duenio"] as String?
    )

    // ---- Jugador ----
    private fun jugadorAMapa(j: Jugador): Map<String, Any?> = mapOf(
        "id" to j.id,
        "nombre" to j.nombre,
        "color" to j.color,
        "esIA" to j.esIA,
        "mano" to j.mano.map(::fichaAMapa)
    )

    private fun jugadorDesdeMapa(m: Map<String, Any?>) = Jugador(
        id = m["id"] as String,
        nombre = m["nombre"] as String,
        color = (m["color"] as Number).toLong(),
        esIA = m["esIA"] as Boolean? ?: false,
        mano = m.listaDeMapas("mano").map(::fichaDesdeMapa)
    )
}

/** Firestore devuelve los enteros como Long: se normalizan a Int. */
internal fun Map<String, Any?>.entero(clave: String): Int = (this[clave] as Number).toInt()

@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any?>.listaDeMapas(clave: String): List<Map<String, Any?>> =
    (this[clave] as? List<Map<String, Any?>>).orEmpty()
