package modelo;

/**
 * Representa el estado de una run guardada en la base de datos.
 * Se usa tanto para guardar como para cargar una partida.
 */
public class Partida {

    public enum Estado { EN_CURSO, COMPLETADA, DERROTA }

    private int    id;
    private int    idJugador;
    private int    idPersonaje;
    private int    faseActual;    // 1-4
    private int    hpActual;
    private Estado estado;

    /** Constructor para crear una nueva partida (sin id aún). */
    public Partida(int idJugador, int idPersonaje, int faseActual, int hpActual) {
        this.id          = 0;
        this.idJugador   = idJugador;
        this.idPersonaje = idPersonaje;
        this.faseActual  = faseActual;
        this.hpActual    = hpActual;
        this.estado      = Estado.EN_CURSO;
    }

    /** Constructor para recuperar una partida existente desde la BD. */
    public Partida(int id, int idJugador, int idPersonaje,
                   int faseActual, int hpActual, Estado estado) {
        this.id          = id;
        this.idJugador   = idJugador;
        this.idPersonaje = idPersonaje;
        this.faseActual  = faseActual;
        this.hpActual    = hpActual;
        this.estado      = estado;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public int    getIdJugador()       { return idJugador; }
    public int    getIdPersonaje()     { return idPersonaje; }

    public int    getFaseActual()      { return faseActual; }
    public void   setFaseActual(int f) { this.faseActual = f; }

    public int    getHpActual()        { return hpActual; }
    public void   setHpActual(int hp)  { this.hpActual = hp; }

    public Estado getEstado()          { return estado; }
    public void   setEstado(Estado e)  { this.estado = e; }

    @Override
    public String toString() {
        return String.format("Partida[%d] fase:%d hp:%d estado:%s",
                id, faseActual, hpActual, estado);
    }
}
