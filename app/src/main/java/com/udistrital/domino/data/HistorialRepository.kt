package com.udistrital.domino.data

/** Un registro de partida terminada para la pantalla de Historial. */
data class RegistroPartida(
    val id: String,
    val oponente: String,
    val gano: Boolean,
    val misPuntos: Int,
    val puntosOponente: Int,
    val fecha: String
)

/**
 * Historial de partidas del usuario. La UI (pantalla Historial) habla con esta
 * abstracción.
 */
interface HistorialRepository {
    fun obtenerHistorial(): List<RegistroPartida>
}

/**
 * Implementación local con datos de ejemplo, para poder ver la pantalla de
 * Historial funcionando mientras no hay base de datos.
 */
class HistorialLocalRepository : HistorialRepository {
    override fun obtenerHistorial(): List<RegistroPartida> = listOf(
        RegistroPartida("1", "Carlos", true, 18, 12, "28 sep. 2026"),
        RegistroPartida("2", "Luis", false, 10, 18, "25 sep. 2026"),
        RegistroPartida("3", "María", true, 21, 15, "22 sep. 2026"),
        RegistroPartida("4", "Pedro", false, 12, 18, "20 sep. 2026"),
        RegistroPartida("5", "Ana", true, 18, 14, "18 sep. 2026")
    )
}

/**
 * ⚠️ PENDIENTE DE CONEXIÓN A LA BASE DE DATOS (Firebase/Firestore).
 *
 * Debe leer la colección "usuarios/{uid}/partidas" (o similar), ordenada por
 * fecha descendente, y mapear cada documento a [RegistroPartida]. Al terminar
 * cada partida, el JuegoViewModel/repositorio debería escribir aquí el registro.
 */
class HistorialFirebaseRepository : HistorialRepository {
    override fun obtenerHistorial(): List<RegistroPartida> =
        TODO("Leer historial de partidas del usuario desde Firestore")
}
