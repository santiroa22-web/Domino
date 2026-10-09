package com.udistrital.domino.data

import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.Movimiento
import com.udistrital.domino.logica.Posicion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ⚠️ PENDIENTE DE CONEXIÓN A LA BASE DE DATOS (Firebase).
 *
 * Esta clase es el "hueco" donde entrará la sincronización en tiempo real. Tiene
 * exactamente la misma interfaz que [PartidaLocalRepository], por lo que para
 * pasar de local a en línea solo hay que construir este repositorio en lugar del
 * local (ver JuegoViewModel) — ni el motor ni las pantallas cambian.
 *
 * Qué falta implementar aquí con Firestore:
 *  1. iniciar(config): crear el documento "partidas/{codigoSala}" con el
 *     EstadoJuego inicial serializado y empezar a escucharlo con addSnapshotListener.
 *  2. El listener debe deserializar el documento y publicarlo en [_estado].
 *  3. colocarFicha / robarOPasar / usarPortal / omitirPortal: aplicar el
 *     movimiento con MotorDomino y ESCRIBIR el nuevo EstadoJuego en el documento
 *     (o usar una transacción para evitar choques entre jugadores).
 *  4. miId debe venir del usuario autenticado (FirebaseAuth.getInstance().currentUser).
 *  5. Mapear EstadoJuego <-> Map<String, Any> (Firestore no guarda clases
 *     anidadas complejas directamente; conviene un modelo DTO plano).
 */
class PartidaFirebaseRepository : PartidaRepository {

    private val _estado = MutableStateFlow<EstadoJuego?>(null)
    override val estado: StateFlow<EstadoJuego?> = _estado.asStateFlow()

    override val miId: String get() = TODO("Obtener del usuario autenticado de Firebase")
    override val codigoSala: String get() = TODO("Código de la sala en Firestore")

    override fun iniciar(config: ConfigPartida) {
        TODO("Crear documento de partida en Firestore y suscribirse a cambios")
    }

    override fun colocarFicha(movimiento: Movimiento) {
        TODO("Aplicar movimiento y escribir el nuevo estado en Firestore")
    }

    override fun robarOPasar() {
        TODO("Robar/pasar y escribir el nuevo estado en Firestore")
    }

    override fun usarPortal(origen: Posicion, destino: Posicion) {
        TODO("Resolver portal y escribir el nuevo estado en Firestore")
    }

    override fun omitirPortal() {
        TODO("Omitir portal y escribir el nuevo estado en Firestore")
    }

    override fun reiniciar() {
        TODO("Reiniciar documento de partida en Firestore")
    }

    override fun abandonar() {
        TODO("Quitar el listener y actualizar la sala en Firestore")
    }
}
