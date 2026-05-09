package modelo;

/**
 * Contenedor de sesión que viaja entre controladores durante una partida.
 * Centraliza el estado actual: jugador, héroe elegido, fase y partida BD.
 */
public class GameSession {

    private Jugador jugador;
    private Heroe   heroe;
    private int     faseActual;   // 1-4
    private Partida partida;      // referencia a la fila en BD (puede ser null si no se ha guardado aún)

    public GameSession(Jugador jugador, Heroe heroe) {
        this.jugador    = jugador;
        this.heroe      = heroe;
        this.faseActual = 1;
        this.partida    = null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean esFaseFinal()  { return faseActual == 4; }
    public boolean hayMasFases()  { return faseActual < 4; }

    public void avanzarFase() {
        if (faseActual < 4) faseActual++;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    public Jugador getJugador()          { return jugador; }
    public void    setJugador(Jugador j) { this.jugador = j; }

    public Heroe   getHeroe()            { return heroe; }
    public void    setHeroe(Heroe h)     { this.heroe = h; }

    public int     getFaseActual()       { return faseActual; }
    public void    setFaseActual(int f)  { this.faseActual = f; }

    public Partida getPartida()          { return partida; }
    public void    setPartida(Partida p) { this.partida = p; }
}
