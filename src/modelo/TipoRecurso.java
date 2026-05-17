package modelo;

/**
 * Tipo de recurso que consume una habilidad al ejecutarse.
 *
 * <ul>
 *   <li>{@link #ENERGIA} – Puntos de Energía del Guerrero.</li>
 *   <li>{@link #MANA}    – Puntos de Magia de Mago o Clérigo.</li>
 *   <li>{@link #NINGUNO} – La habilidad no tiene coste de recurso.</li>
 * </ul>
 */
public enum TipoRecurso {
    ENERGIA,
    MANA,
    NINGUNO
}
