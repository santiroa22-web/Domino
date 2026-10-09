package com.udistrital.domino.logica

/**
 * Ficha de dominó. Tiene dos lados con un valor de puntos (0..6 en un dominó
 * doble-seis estándar).
 *
 * [rotada] solo afecta la forma en que se muestra la ficha (qué lado queda a la
 * izquierda), no su identidad: una ficha 3|5 sigue siendo la misma ficha esté
 * rotada o no. La validación de movimientos comprueba ambos lados, por lo que la
 * rotación es una ayuda visual para el jugador al conectar con la cadena.
 *
 * LÓGICA PURA: sin dependencias de Android.
 */
data class Ficha(
    val id: Int,
    val ladoA: Int,
    val ladoB: Int,
    val rotada: Boolean = false
) {
    /** true si los dos lados son iguales (ej. 4|4). Los dobles son más fuertes. */
    val esDoble: Boolean get() = ladoA == ladoB

    /** Suma de los dos lados, usada como desempate y para la IA. */
    val suma: Int get() = ladoA + ladoB

    /** Valor que se dibuja a la izquierda según la rotación actual. */
    val valorIzquierdo: Int get() = if (rotada) ladoB else ladoA

    /** Valor que se dibuja a la derecha según la rotación actual. */
    val valorDerecho: Int get() = if (rotada) ladoA else ladoB

    /** Devuelve una copia rotada (intercambia visualmente los lados). */
    fun rotar(): Ficha = copy(rotada = !rotada)

    /** true si alguno de los lados tiene el valor [v]. */
    fun tieneValor(v: Int): Boolean = ladoA == v || ladoB == v

    /** true si esta ficha conecta (comparte algún valor) con [otra]. */
    fun conectaCon(otra: Ficha): Boolean =
        tieneValor(otra.ladoA) || tieneValor(otra.ladoB)

    override fun toString(): String = "[$ladoA|$ladoB]"
}
