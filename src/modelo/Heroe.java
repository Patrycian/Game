package modelo;

import java.util.List;

/**
 * Superclase abstracta de los héroes jugables.
 *
 * <p>Cada subclase define sus atributos en su propio constructor y proporciona
 * la lista de {@link Habilidad} disponibles en combate a través de
 * {@link #getHabilidades()}. El controlador itera esa lista de forma uniforme,
 * sin necesidad de {@code instanceof} ni enums de acción específicos.</p>
 */
public abstract class Heroe extends Personaje {

    // ── Variables ─────────────────────────────────────────────────────────────

    /**
     * Mensaje de defensa pendiente: lo registra una subclase cuando absorbe o
     * reduce un ataque entrante (p. ej. Escudo Arcano del Mago). El motor lo
     * consume para sustituir el mensaje de golpe del enemigo en el log.
     */
    private String mensajeDefensa = null;

    // ── Constructor ───────────────────────────────────────────────────────────

    protected Heroe(String nombre, int puntosGolpe, int defensa, int poder) {
        super(nombre, puntosGolpe, defensa, poder);
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /** @return ruta del recurso de imagen del héroe */
    public abstract String getRutaImagen();

    /**
     * Devuelve la lista de habilidades disponibles para este héroe en combate.
     * Cada {@link Habilidad} encapsula su nombre, descripción, coste y lógica
     * de ejecución, de modo que el controlador puede tratarlas de forma uniforme.
     *
     * @return lista inmutable de habilidades del héroe
     */
    public abstract List<Habilidad> getHabilidades();

    // ── Métodos ───────────────────────────────────────────────────────────────

    /**
     * Reinicia el estado de las habilidades al inicio de una nueva fase.
     * Las subclases lo sobreescriben si tienen estado que limpiar (buffs activos,
     * contadores de uso, etc.).
     */
    public void reiniciarHabilidad() {
    }

    /**
     * Permite a una subclase registrar un mensaje que sustituye al del ataque
     * enemigo en el log de combate (ejemplo: "¡Escudo Arcano absorbió el golpe!").
     *
     * @param mensaje texto descriptivo de la defensa activada
     */
    protected void registrarMensajeDefensa(String mensaje) {
        this.mensajeDefensa = mensaje;
    }

    /**
     * El motor llama a este método tras {@code enemigo.realizarAtaque(heroe)}.
     * Si devuelve un valor no nulo, ese texto reemplaza el mensaje del ataque en el log.
     * Se consume al leerlo para que no persista entre llamadas.
     *
     * @return el mensaje de defensa registrado, o {@code null} si no hay ninguno
     */
    public String consumirMensajeDefensa() {
        String mensaje = mensajeDefensa;
        mensajeDefensa  = null;
        return mensaje;
    }
}
