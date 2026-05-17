package modelo;

/**
 * Tipos de objeto que un enemigo puede soltar al ser derrotado.
 *
 * <ul>
 *   <li>{@link #POCION_VIDA}   – añade una Poción de Curación al inventario del héroe.</li>
 *   <li>{@link #POCION_MAGICA} – añade una Poción Mágica al inventario del héroe.</li>
 * </ul>
 *
 * @see Enemigo#generarDrop()
 */
public enum TipoDrop {

    /** Poción de Curación (restaura HP). */
    POCION_VIDA,

    /** Poción Mágica (restaura PM). */
    POCION_MAGICA
}
