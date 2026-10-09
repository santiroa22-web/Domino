package com.udistrital.domino.logica

import kotlin.random.Random

/**
 * Motor de reglas de "Dominó Conquista".
 *
 * Contiene TODA la lógica del juego como funciones puras: recibe un
 * [EstadoJuego] y devuelve uno nuevo, sin efectos secundarios ni dependencias de
 * Android. Esto cumple el requisito de "separar la lógica del juego de la
 * interfaz gráfica" y permite probarla con pruebas unitarias.
 *
 * Reglas implementadas (resumen):
 *  - Cada jugador inicia con 7 fichas; el resto va al pozo.
 *  - La partida se desarrolla por turnos.
 *  - En su turno el jugador coloca una ficha en una casilla válida y conquista
 *    ese territorio.
 *  - Regla del dominó sobre la grilla: la primera ficha puede ir en cualquier
 *    casilla; a partir de ahí la casilla destino debe estar vacía, ser adyacente
 *    a una ficha ya colocada y la ficha elegida debe compartir un valor con
 *    alguna ficha vecina (conectar con la cadena).
 *  - Casillas especiales: Bonus (+3), Tesoro (+5), Trampa (-2),
 *    Fortaleza (requiere controlar un territorio adyacente = "dos conquistas"),
 *    Portal (permite reubicar una ficha ya colocada).
 *  - Si el jugador no tiene movimiento válido, roba del pozo; si no puede y el
 *    pozo está vacío, pierde el turno.
 *  - La partida termina cuando un jugador se queda sin fichas, nadie puede
 *    mover, o se alcanza el número máximo de rondas.
 *  - Gana quien tenga mayor puntaje; en empate, quien tenga más territorios.
 */
object MotorDomino {

    const val FICHAS_POR_JUGADOR = 7
    const val PUNTOS_BASE_TERRITORIO = 1

    // ---------------------------------------------------------------------
    // Creación de la partida
    // ---------------------------------------------------------------------

    /** Genera las 28 fichas de un dominó doble-seis (0|0 .. 6|6). */
    fun crearFichas(): List<Ficha> {
        val fichas = mutableListOf<Ficha>()
        var id = 0
        for (a in 0..6) {
            for (b in a..6) {
                fichas.add(Ficha(id = id++, ladoA = a, ladoB = b))
            }
        }
        return fichas
    }

    /**
     * Crea el tablero [filas] x [columnas] repartiendo casillas especiales de
     * forma aleatoria (determinista si se fija [random]).
     */
    fun crearTablero(filas: Int, columnas: Int, random: Random): List<List<Casilla>> {
        val total = filas * columnas
        // Distribución de especiales proporcional al tamaño del tablero.
        val especiales = mutableListOf<TipoCasilla>()
        repeat(maxOf(2, total / 10)) { especiales.add(TipoCasilla.BONUS) }
        repeat(maxOf(2, total / 12)) { especiales.add(TipoCasilla.TESORO) }
        repeat(maxOf(2, total / 12)) { especiales.add(TipoCasilla.TRAMPA) }
        repeat(maxOf(1, total / 25)) { especiales.add(TipoCasilla.FORTALEZA) }
        repeat(maxOf(1, total / 25)) { especiales.add(TipoCasilla.PORTAL) }

        // Índices barajados para colocar las especiales sin solaparse.
        val indices = (0 until total).shuffled(random)
        val tipoPorIndice = HashMap<Int, TipoCasilla>()
        especiales.take(total).forEachIndexed { i, tipo -> tipoPorIndice[indices[i]] = tipo }

        return (0 until filas).map { f ->
            (0 until columnas).map { c ->
                val idx = f * columnas + c
                Casilla(
                    posicion = Posicion(f, c),
                    tipo = tipoPorIndice[idx] ?: TipoCasilla.NORMAL
                )
            }
        }
    }

    /**
     * Inicia una nueva partida.
     * [configJugadores] es la lista (id, nombre, color, esIA) de participantes.
     */
    fun nuevaPartida(
        configJugadores: List<Jugador>,
        filas: Int = 5,
        columnas: Int = 5,
        maxRondas: Int = 20,
        seed: Long = System.nanoTime()
    ): EstadoJuego {
        require(configJugadores.size >= 2) { "Se necesitan al menos 2 jugadores" }
        val random = Random(seed)

        val mazo = crearFichas().shuffled(random).toMutableList()
        val jugadoresConMano = configJugadores.map { j ->
            val mano = ArrayList<Ficha>()
            repeat(FICHAS_POR_JUGADOR) { if (mazo.isNotEmpty()) mano.add(mazo.removeAt(0)) }
            j.copy(mano = mano)
        }

        return EstadoJuego(
            tablero = crearTablero(filas, columnas, random),
            jugadores = jugadoresConMano,
            turnoActual = 0,
            pozo = mazo,
            ronda = 1,
            maxRondas = maxRondas,
            fase = FaseJuego.EN_CURSO,
            mensaje = "Comienza la partida. Turno de ${jugadoresConMano.first().nombre}."
        )
    }

    // ---------------------------------------------------------------------
    // Validación de movimientos
    // ---------------------------------------------------------------------

    /** Casillas ocupadas adyacentes a [p]. */
    private fun vecinasOcupadas(estado: EstadoJuego, p: Posicion): List<Casilla> =
        p.adyacentes().mapNotNull { estado.casilla(it) }.filter { it.ocupada }

    /**
     * Comprueba si colocar [ficha] en [posicion] es un movimiento válido para el
     * jugador [jugadorId].
     */
    fun movimientoValido(
        estado: EstadoJuego,
        ficha: Ficha,
        posicion: Posicion,
        jugadorId: String
    ): Boolean {
        val casilla = estado.casilla(posicion) ?: return false
        if (casilla.ocupada) return false

        // Primera ficha de la partida: puede ir en cualquier casilla vacía.
        if (!estado.hayFichasEnTablero()) return true

        // A partir de la segunda: debe conectar con la cadena de dominó.
        val vecinas = vecinasOcupadas(estado, posicion)
        if (vecinas.isEmpty()) return false
        val conecta = vecinas.any { vecina -> ficha.conectaCon(vecina.ficha!!) }
        if (!conecta) return false

        // Regla de la Fortaleza: "necesita dos conquistas para ser controlada".
        // Se interpreta como que el jugador solo puede conquistarla si ya
        // controla al menos un territorio adyacente (primera conquista = el
        // vecino, segunda = la fortaleza).
        if (casilla.tipo == TipoCasilla.FORTALEZA) {
            val controlaVecino = posicion.adyacentes()
                .mapNotNull { estado.casilla(it) }
                .any { it.conquistadaPor == jugadorId }
            if (!controlaVecino) return false
        }
        return true
    }

    /** Todas las posiciones donde [jugadorId] puede colocar [ficha]. */
    fun posicionesValidas(estado: EstadoJuego, ficha: Ficha, jugadorId: String): List<Posicion> =
        estado.todasLasCasillas()
            .map { it.posicion }
            .filter { movimientoValido(estado, ficha, it, jugadorId) }

    /** true si [jugadorId] tiene al menos un movimiento posible con su mano. */
    fun jugadorTieneMovimiento(estado: EstadoJuego, jugadorId: String): Boolean {
        val jugador = estado.jugador(jugadorId) ?: return false
        return jugador.mano.any { ficha -> posicionesValidas(estado, ficha, jugadorId).isNotEmpty() }
    }

    // ---------------------------------------------------------------------
    // Aplicar movimientos
    // ---------------------------------------------------------------------

    /**
     * Aplica un movimiento de colocación. Supone que ya fue validado (si no lo
     * es, devuelve el estado sin cambios con un mensaje de error).
     *
     * Coloca la ficha, conquista el territorio, la quita de la mano y:
     *  - Si conquistó un Portal, deja [accionPortalPendiente] = true (el turno NO
     *    avanza hasta resolver el portal).
     *  - En otro caso, verifica fin y pasa el turno al siguiente jugador.
     */
    fun aplicarMovimiento(estado: EstadoJuego, movimiento: Movimiento, jugadorId: String): EstadoJuego {
        if (estado.terminada) return estado
        if (estado.jugadorEnTurno.id != jugadorId) {
            return estado.copy(mensaje = "No es el turno de ese jugador.")
        }
        if (!movimientoValido(estado, movimiento.ficha, movimiento.posicion, jugadorId)) {
            return estado.copy(mensaje = "Movimiento inválido.")
        }

        val casilla = estado.casilla(movimiento.posicion)!!
        val jugador = estado.jugador(jugadorId)!!

        var nuevo = estado
            // Colocar ficha y marcar territorio conquistado.
            .conCasilla(movimiento.posicion) {
                it.copy(ficha = movimiento.ficha, conquistadaPor = jugadorId)
            }
            // Quitar la ficha de la mano del jugador.
            .conJugador(jugadorId) { j ->
                j.copy(mano = j.mano.filterNot { it.id == movimiento.ficha.id })
            }

        val puntosGanados = PUNTOS_BASE_TERRITORIO + casilla.tipo.puntos
        val detalle = when (casilla.tipo) {
            TipoCasilla.NORMAL -> "conquistó ${movimiento.posicion.etiqueta}"
            TipoCasilla.BONUS -> "¡Bonus! +${casilla.tipo.puntos} en ${movimiento.posicion.etiqueta}"
            TipoCasilla.TESORO -> "¡Tesoro! +${casilla.tipo.puntos} en ${movimiento.posicion.etiqueta}"
            TipoCasilla.TRAMPA -> "¡Trampa! ${casilla.tipo.puntos} en ${movimiento.posicion.etiqueta}"
            TipoCasilla.FORTALEZA -> "¡Fortaleza conquistada en ${movimiento.posicion.etiqueta}!"
            TipoCasilla.PORTAL -> "¡Portal activado en ${movimiento.posicion.etiqueta}!"
        }
        nuevo = nuevo.copy(mensaje = "${jugador.nombre} $detalle (${if (puntosGanados >= 0) "+" else ""}$puntosGanados).")

        // Portal: otorga una acción de reubicación antes de pasar el turno.
        if (casilla.tipo == TipoCasilla.PORTAL && puedeUsarPortal(nuevo, jugadorId)) {
            return nuevo.copy(accionPortalPendiente = true)
        }

        return avanzarTrasJugada(nuevo)
    }

    /**
     * El jugador en turno no puede o no quiere colocar: intenta robar del pozo.
     * Si el pozo está vacío, pierde el turno.
     */
    fun robarOPasar(estado: EstadoJuego, jugadorId: String): EstadoJuego {
        if (estado.terminada) return estado
        if (estado.jugadorEnTurno.id != jugadorId) return estado

        val jugador = estado.jugador(jugadorId)!!
        if (estado.pozo.isNotEmpty()) {
            val fichaRobada = estado.pozo.first()
            val nuevo = estado
                .copy(pozo = estado.pozo.drop(1))
                .conJugador(jugadorId) { it.copy(mano = it.mano + fichaRobada) }
                .copy(mensaje = "${jugador.nombre} robó una ficha del pozo.")
            // Si tras robar puede jugar, sigue siendo su turno.
            return if (jugadorTieneMovimiento(nuevo, jugadorId)) nuevo
            else avanzarTrasJugada(nuevo.copy(mensaje = "${jugador.nombre} robó y aún no puede jugar. Pierde el turno."))
        }
        // Pozo vacío: pierde el turno.
        return avanzarTrasJugada(estado.copy(mensaje = "${jugador.nombre} no puede jugar y pierde el turno."))
    }

    // ---------------------------------------------------------------------
    // Portal
    // ---------------------------------------------------------------------

    /** Fichas del jugador que están colocadas en el tablero y podría reubicar. */
    fun fichasReubicables(estado: EstadoJuego, jugadorId: String): List<Casilla> =
        estado.todasLasCasillas().filter { it.conquistadaPor == jugadorId && it.ocupada && it.tipo != TipoCasilla.PORTAL }

    /** Destinos válidos para reubicar la ficha que está en [origen] vía portal. */
    fun destinosPortal(estado: EstadoJuego, origen: Posicion, jugadorId: String): List<Posicion> {
        val casillaOrigen = estado.casilla(origen) ?: return emptyList()
        val ficha = casillaOrigen.ficha ?: return emptyList()
        // Se evalúa como si la ficha de origen ya no estuviera en el tablero.
        val sinOrigen = estado.conCasilla(origen) { it.copy(ficha = null, conquistadaPor = null) }
        return posicionesValidas(sinOrigen, ficha, jugadorId).filter { it != origen }
    }

    private fun puedeUsarPortal(estado: EstadoJuego, jugadorId: String): Boolean =
        fichasReubicables(estado, jugadorId).any { destinosPortal(estado, it.posicion, jugadorId).isNotEmpty() }

    /** Reubica la ficha de [origen] a [destino] y luego pasa el turno. */
    fun usarPortal(estado: EstadoJuego, origen: Posicion, destino: Posicion, jugadorId: String): EstadoJuego {
        if (!estado.accionPortalPendiente) return estado
        if (destino !in destinosPortal(estado, origen, jugadorId)) {
            return estado.copy(mensaje = "Destino de portal inválido.")
        }
        val ficha = estado.casilla(origen)!!.ficha!!
        val nuevo = estado
            .conCasilla(origen) { it.copy(ficha = null, conquistadaPor = null) }
            .conCasilla(destino) { it.copy(ficha = ficha, conquistadaPor = jugadorId) }
            .copy(
                accionPortalPendiente = false,
                mensaje = "Ficha movida por portal de ${origen.etiqueta} a ${destino.etiqueta}."
            )
        return avanzarTrasJugada(nuevo)
    }

    /** El jugador decide no usar el portal; se pasa el turno normalmente. */
    fun omitirPortal(estado: EstadoJuego): EstadoJuego {
        if (!estado.accionPortalPendiente) return estado
        return avanzarTrasJugada(estado.copy(accionPortalPendiente = false))
    }

    // ---------------------------------------------------------------------
    // Avance de turno y fin de partida
    // ---------------------------------------------------------------------

    /** Verifica fin de partida y, si continúa, pasa el turno al siguiente. */
    private fun avanzarTrasJugada(estado: EstadoJuego): EstadoJuego {
        val finalizado = verificarFin(estado)
        if (finalizado.terminada) return finalizado
        return pasarTurno(finalizado)
    }

    /** Pasa el turno al siguiente jugador (incrementando la ronda al dar la vuelta). */
    fun pasarTurno(estado: EstadoJuego): EstadoJuego {
        val siguiente = (estado.turnoActual + 1) % estado.jugadores.size
        val nuevaRonda = if (siguiente == 0) estado.ronda + 1 else estado.ronda
        val conTurno = estado.copy(turnoActual = siguiente, ronda = nuevaRonda)
        // Si al pasar ronda se supera el máximo, termina.
        return verificarFin(conTurno)
    }

    /**
     * Determina si la partida terminó:
     *  - algún jugador se quedó sin fichas,
     *  - se superó el número máximo de rondas,
     *  - o nadie puede mover y el pozo está vacío (juego bloqueado).
     */
    fun verificarFin(estado: EstadoJuego): EstadoJuego {
        if (estado.terminada) return estado

        val alguienSinFichas = estado.jugadores.any { it.mano.isEmpty() }
        val superoRondas = estado.ronda > estado.maxRondas
        val nadiePuedeMover = estado.pozo.isEmpty() &&
            estado.jugadores.none { jugadorTieneMovimiento(estado, it.id) }

        return if (alguienSinFichas || superoRondas || nadiePuedeMover) {
            estado.copy(
                fase = FaseJuego.FINALIZADA,
                mensaje = "La partida ha terminado."
            )
        } else estado
    }

    // ---------------------------------------------------------------------
    // Puntaje y resultado
    // ---------------------------------------------------------------------

    /** Casillas controladas por [jugadorId]. */
    fun territoriosDe(estado: EstadoJuego, jugadorId: String): List<Casilla> =
        estado.todasLasCasillas().filter { it.conquistadaPor == jugadorId }

    /** Número de territorios conquistados por [jugadorId]. */
    fun territoriosConquistados(estado: EstadoJuego, jugadorId: String): Int =
        territoriosDe(estado, jugadorId).size

    /** Puntaje total de [jugadorId]: base por territorio + puntos de cada tipo. */
    fun puntaje(estado: EstadoJuego, jugadorId: String): Int =
        territoriosDe(estado, jugadorId).sumOf { PUNTOS_BASE_TERRITORIO + it.tipo.puntos }

    /** Suma de puntos positivos por casillas especiales (Bonus + Tesoro + Fortaleza). */
    fun bonos(estado: EstadoJuego, jugadorId: String): Int =
        territoriosDe(estado, jugadorId)
            .filter { it.tipo.puntos > 0 }
            .sumOf { it.tipo.puntos }

    /** Suma de penalizaciones (Trampas) de [jugadorId]. */
    fun penalizaciones(estado: EstadoJuego, jugadorId: String): Int =
        territoriosDe(estado, jugadorId)
            .filter { it.tipo.puntos < 0 }
            .sumOf { it.tipo.puntos }

    /**
     * Calcula el resultado final. Gana el de mayor puntaje; si hay empate en
     * puntaje, gana quien tenga más territorios. Si persiste, se marca empate.
     */
    fun resultado(estado: EstadoJuego): ResultadoPartida {
        val puntajes = estado.jugadores.associate { it.id to puntaje(estado, it.id) }
        val territorios = estado.jugadores.associate { it.id to territoriosConquistados(estado, it.id) }
        val bonos = estado.jugadores.associate { it.id to bonos(estado, it.id) }
        val penal = estado.jugadores.associate { it.id to penalizaciones(estado, it.id) }

        val maxPuntaje = puntajes.values.maxOrNull()
        val lideres = estado.jugadores.filter { puntajes[it.id] == maxPuntaje }
        val ganador: Jugador?
        val empate: Boolean
        if (lideres.size == 1) {
            ganador = lideres.first()
            empate = false
        } else {
            // Desempate por territorios.
            val maxTerr = lideres.maxOf { territorios[it.id] ?: 0 }
            val finalistas = lideres.filter { (territorios[it.id] ?: 0) == maxTerr }
            if (finalistas.size == 1) {
                ganador = finalistas.first()
                empate = false
            } else {
                ganador = null
                empate = true
            }
        }

        return ResultadoPartida(
            puntajes = puntajes,
            territorios = territorios,
            bonos = bonos,
            penalizaciones = penal,
            ganadorId = ganador?.id,
            empate = empate
        )
    }
}
