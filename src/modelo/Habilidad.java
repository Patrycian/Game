package modelo;

/**
 * Contrato que deben cumplir todas las habilidades de los héroes.
 *
 * <p>Cada implementación encapsula su nombre, descripción, coste y lógica de
 * ejecución, de modo que el controlador puede tratarlas de forma uniforme sin
 * depender de {@code instanceof} ni de enums de acción específicos.</p>
 *
 * <p>Uso básico:</p>
 * <pre>
 *   for (Habilidad h : heroe.getHabilidades()) {
 *       if (h.puedeUsarse(heroe)) {
 *           String resultado = h.ejecutar(heroe, objetivo);
 *       }
 *   }
 * </pre>
 */
public interface Habilidad {

    /** @return nombre de la habilidad (p. ej. "Bola de Fuego") */
    String getNombre();

    /** @return descripción breve para mostrar en la UI */
    String getDescripcion();

    /** @return tipo de recurso que consume ({@link TipoRecurso}) */
    TipoRecurso getTipoRecurso();

    /**
     * @return coste en unidades del recurso correspondiente;
     *         0 si {@code getTipoRecurso() == NINGUNO}
     */
    int getCoste();

    /**
     * Comprueba si el héroe dispone del recurso necesario para usar esta habilidad.
     *
     * @param heroe héroe que intenta usar la habilidad
     * @return {@code true} si puede usarse ahora mismo
     */
    boolean puedeUsarse(Heroe heroe);

    /**
     * Ejecuta la habilidad, aplica sus efectos y devuelve un mensaje descriptivo.
     *
     * <p>El método es responsable de consumir el recurso necesario antes de
     * aplicar el efecto.</p>
     *
     * @param heroe    héroe que usa la habilidad (fuente del efecto)
     * @param objetivo personaje objetivo (enemigo o el propio héroe, según la habilidad)
     * @return texto descriptivo del efecto para el log de combate
     */
    String ejecutar(Heroe heroe, Personaje objetivo);

    /**
     * Indica si la habilidad afecta al enemigo (true) o al propio héroe (false).
     * Por defecto devuelve {@code true}; las habilidades de auto-curación o
     * buff deben sobreescribir este método devolviendo {@code false}.
     *
     * @return {@code true} si el objetivo de {@link #ejecutar} debe ser el enemigo
     */
    default boolean afectaAlEnemigo() {
        return true;
    }

    /**
     * Ruta del recurso de audio que debe reproducirse al usar esta habilidad,
     * relativa al classpath (p. ej. {@code "/recursos/audio/bolaFuego.mp3"}).
     * Devuelve {@code null} por defecto; las habilidades con sonido propio
     * deben sobreescribir este método.
     *
     * @return ruta del clip de audio, o {@code null} si la habilidad no tiene sonido propio
     */
    default String getRutaAudio() {
        return null;
    }
}
