package modelo;

/**
 * Contenedor de sesión que viaja entre controladores durante una partida.
 *
 * <p>Centraliza todo el estado de una run activa de forma que cualquier
 * controlador pueda acceder a él sin necesidad de pasar múltiples parámetros
 * sueltos. Se crea en {@code SeleccionHeroeController} al iniciar una partida
 * nueva, o se reconstruye en {@code CargarPartidaController} al reanudar una
 * guardada.</p>
 *
 * <p>Campos principales:</p>
 * <ul>
 *   <li>{@code jugador} – el jugador que controla la partida (con nick y puntuación).</li>
 *   <li>{@code heroe}   – el personaje elegido (Mago, Guerrero o Clérigo).</li>
 *   <li>{@code faseActual} – número de fase del combate en curso (1 a 4).</li>
 *   <li>{@code partida}    – referencia a la fila de la tabla {@code partidas} en BD;
 *       puede ser {@code null} si la partida aún no se ha guardado.</li>
 * </ul>
 */
public class GameSession {

    private Jugador jugador;
    private Heroe   heroe;
    private int     faseActual;   // Fase activa: 1 = primera sala, 4 = jefe final
    private Partida partida;      // Referencia BD; null hasta la primera acción de guardado

    /**
     * Crea una sesión nueva, comenzando en la fase 1 y sin partida guardada.
     *
     * @param jugador jugador que controla la partida
     * @param heroe   héroe seleccionado para esta run
     */
    public GameSession(Jugador jugador, Heroe heroe) {
        this.jugador    = jugador;
        this.heroe      = heroe;
        this.faseActual = 1;
        this.partida    = null;
    }

    // ── Helpers de fase ───────────────────────────────────────────────────────

    /**
     * Indica si la fase actual es la 4 (jefe final, el Dragón).
     *
     * @return {@code true} si estamos en la fase 4
     */
    public boolean esFaseFinal()  { return faseActual == 4; }

    /**
     * Indica si quedan fases por completar después de la actual.
     *
     * @return {@code true} si {@code faseActual} es menor que 4
     */
    public boolean hayMasFases()  { return faseActual < 4; }

    /**
     * Avanza al número de fase siguiente (máximo 4).
     * Se llama en {@code MazmorraController} cuando el héroe vence a un enemigo
     * y no es la fase final.
     */
    public void avanzarFase() {
        if (faseActual < 4) faseActual++;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /** @return el jugador propietario de esta sesión */
    public Jugador getJugador()          { return jugador; }
    /** @param j nuevo jugador (raro en uso normal; útil en pruebas) */
    public void    setJugador(Jugador j) { this.jugador = j; }

    /** @return el héroe activo en esta sesión */
    public Heroe   getHeroe()            { return heroe; }
    /** @param h nuevo héroe (raro en uso normal; útil en pruebas) */
    public void    setHeroe(Heroe h)     { this.heroe = h; }

    /** @return número de fase actual (1-4) */
    public int     getFaseActual()       { return faseActual; }
    /**
     * Permite fijar directamente la fase, utilizado al reanudar una partida
     * guardada en la que {@code fase > 1}.
     *
     * @param f nueva fase (1-4)
     */
    public void    setFaseActual(int f)  { this.faseActual = f; }

    /**
     * @return la partida guardada en BD, o {@code null} si aún no se ha persistido
     */
    public Partida getPartida()          { return partida; }
    /**
     * Asigna la referencia a la partida guardada tras el primer {@code INSERT} en BD.
     *
     * @param p objeto Partida con el id generado por la base de datos
     */
    public void    setPartida(Partida p) { this.partida = p; }
}
