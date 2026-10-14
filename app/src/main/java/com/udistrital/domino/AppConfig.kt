package com.udistrital.domino

/**
 * Interruptor global de la app.
 *
 * MODO_EN_LINEA = true  -> salas y partidas sincronizadas con Firestore (2 dispositivos).
 * MODO_EN_LINEA = false -> modo local contra la CPU (sirve de respaldo si no hay internet
 *                          en la sustentación). El motor y las pantallas son los mismos.
 */
object AppConfig {
    const val MODO_EN_LINEA = true
}
