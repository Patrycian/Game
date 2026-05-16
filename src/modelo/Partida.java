package modelo;

/**
 * Representa el estado persistido de una run en la base de datos.
 *
 * <p>Se usa tanto para crear una nueva entrada como para recargar una partida
 * en curso. Cada campo se mapea directamente a una columna de la tabla
 * {@code partidas}.</p>
 *
 * <p>El ciclo de vida de una partida es:</p>
 * <ol>
 *   <li>{@code EN_CURSO} – la partida fue creada y el héroe sigue vivo.</li>
 *   <li>{@code COMPLETADA} – el héroe derrotó al Dragón (fase 4).</li>
 *   <li>{@code DERROTA} – el héroe murió antes de completar todas las fases.</li>
 * </ol>
 */
public class Partida {

    /**
     * Estado posible de una partida.
     * <ul>
     *   <li>{@code EN_CURSO}   – partida activa, puede reanudarse.</li>
     *   <li>{@code COMPLETADA} – el jugador ganó la mazmorra completa.</li>
     *   <li>{@code DERROTA}    – el héroe fue derrotado.</li>
     * </ul>
     */
    public enum Estado { EN_CURSO, COMPLETADA, DERROTA }

    private int    id;            // id auto-generado por BD (0 si es nueva)
    private int    idJugador;    // FK → jugadores.id
    private int    idPersonaje;  // FK → personajes.id
    private int    faseActual;   // Fase en la que se guardó (1-4)
    private int    hpActual;     // HP del héroe en el momento del guardado
    private int    pmActual;     // PM del héroe mágico al guardar (0 si no es mágico)
    private Estado estado;       // Estado actual de la partida
    private String tipoEnemigo;  // Tipo del enemigo activo al guardar (null si no aplica)
    private int    hpEnemigo;    // HP del enemigo al guardar (0 si fue derrotado o nueva fase)
    private int    pmEnemigo;    // PM del enemigo al guardar (0 si no usa magia o fue derrotado)

    /**
     * Constructor para crear una nueva partida antes de persistirla en BD.
     * El estado se inicializa a {@code EN_CURSO} y el id a 0 (sin asignar).
     *
     * @param idJugador   id del jugador propietario
     * @param idPersonaje id del héroe elegido
     * @param faseActual  fase en que se crea la partida (normalmente 1)
     * @param hpActual    HP inicial del héroe
     */
    public Partida(int idJugador, int idPersonaje, int faseActual, int hpActual) {
        this.id          = 0;
        this.idJugador   = idJugador;
        this.idPersonaje = idPersonaje;
        this.faseActual  = faseActual;
        this.hpActual    = hpActual;
        this.pmActual    = 0;
        this.estado      = Estado.EN_CURSO;
        this.tipoEnemigo = null;
        this.hpEnemigo   = 0;
        this.pmEnemigo   = 0;
    }

    /**
     * Constructor para reconstruir una partida existente cargada desde la BD.
     *
     * @param id          id de la tabla {@code partidas}
     * @param idJugador   FK al jugador
     * @param idPersonaje FK al personaje
     * @param faseActual  fase guardada
     * @param hpActual    HP guardado del héroe
     * @param pmActual    PM guardados del héroe mágico (0 si no es mágico)
     * @param estado      estado guardado ({@code EN_CURSO}, {@code COMPLETADA} o {@code DERROTA})
     * @param tipoEnemigo tipo del enemigo activo al guardar, o {@code null} si nueva fase
     * @param hpEnemigo   HP del enemigo al guardar (0 si derrotado o nueva fase)
     * @param pmEnemigo   PM del enemigo al guardar (0 si no usa magia)
     */
    public Partida(int id, int idJugador, int idPersonaje,
                   int faseActual, int hpActual, int pmActual, Estado estado,
                   String tipoEnemigo, int hpEnemigo, int pmEnemigo) {
        this.id          = id;
        this.idJugador   = idJugador;
        this.idPersonaje = idPersonaje;
        this.faseActual  = faseActual;
        this.hpActual    = hpActual;
        this.pmActual    = pmActual;
        this.estado      = estado;
        this.tipoEnemigo = tipoEnemigo;
        this.hpEnemigo   = hpEnemigo;
        this.pmEnemigo   = pmEnemigo;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /** @return id en BD, o 0 si la partida aún no fue persistida */
    public int    getId()              { return id; }
    /** @param id id asignado por BD tras el INSERT */
    public void   setId(int id)        { this.id = id; }

    /** @return id del jugador propietario (FK → jugadores) */
    public int    getIdJugador()       { return idJugador; }
    /** @return id del personaje asociado (FK → personajes) */
    public int    getIdPersonaje()     { return idPersonaje; }

    /** @return fase activa cuando se guardó la partida (1-4) */
    public int    getFaseActual()      { return faseActual; }
    /** @param f nueva fase activa */
    public void   setFaseActual(int f) { this.faseActual = f; }

    /** @return HP del héroe en el momento del último guardado */
    public int    getHpActual()        { return hpActual; }
    /** @param hp HP actualizados antes de persistir */
    public void   setHpActual(int hp)  { this.hpActual = hp; }

    /** @return PM del héroe mágico en el momento del último guardado (0 si no es mágico) */
    public int    getPmActual()        { return pmActual; }
    /** @param pm PM actualizados antes de persistir */
    public void   setPmActual(int pm)  { this.pmActual = Math.max(0, pm); }

    /** @return estado actual de la partida */
    public Estado getEstado()          { return estado; }
    /** @param e nuevo estado a persistir */
    public void   setEstado(Estado e)  { this.estado = e; }

    /** @return tipo del enemigo activo al guardar, o {@code null} si no aplica */
    public String getTipoEnemigo()              { return tipoEnemigo; }
    /** @param t tipo del enemigo (p. ej. "GOBLIN", "DRAGON"); {@code null} para limpiar */
    public void   setTipoEnemigo(String t)      { this.tipoEnemigo = t; }

    /** @return HP del enemigo al guardar (0 si fue derrotado o nueva fase) */
    public int    getHpEnemigo()                { return hpEnemigo; }
    /** @param hp HP actuales del enemigo antes de persistir */
    public void   setHpEnemigo(int hp)          { this.hpEnemigo = Math.max(0, hp); }

    /** @return PM del enemigo al guardar (0 si no usa magia o fue derrotado) */
    public int    getPmEnemigo()                { return pmEnemigo; }
    /** @param pm PM actuales del enemigo antes de persistir */
    public void   setPmEnemigo(int pm)          { this.pmEnemigo = Math.max(0, pm); }

    @Override
    public String toString() {
        return String.format("Partida[%d] fase:%d hp:%d estado:%s enemigo:%s(hp:%d)",
                id, faseActual, hpActual, estado, tipoEnemigo, hpEnemigo);
    }
}
