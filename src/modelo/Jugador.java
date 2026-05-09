package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un jugador registrado en el sistema.
 * Un jugador tiene un nick único, una puntuación acumulada
 * y puede poseer varios héroes (relación 1-a-muchos).
 */
public class Jugador {

    private int          id;           // id en BD
    private String       nick;
    private int          puntuacion;
    private List<Heroe>  personajes;   // héroes asociados

    public Jugador(String nick) {
        this.nick       = nick;
        this.puntuacion = 0;
        this.personajes = new ArrayList<>();
        this.id         = 0;
    }

    public Jugador(int id, String nick, int puntuacion) {
        this.id         = id;
        this.nick       = nick;
        this.puntuacion = puntuacion;
        this.personajes = new ArrayList<>();
    }

    // ── Lógica de puntuación ──────────────────────────────────────────────────

    /** Añade puntos por victoria en combate. */
    public void sumarPuntos(int cantidad) {
        this.puntuacion += cantidad;
    }

    // ── Gestión de personajes ─────────────────────────────────────────────────

    public void agregarPersonaje(Heroe h) {
        personajes.add(h);
    }

    public List<Heroe> getPersonajes() {
        return personajes;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public String getNick()            { return nick; }
    public void   setNick(String nick) { this.nick = nick; }

    public int    getPuntuacion()              { return puntuacion; }
    public void   setPuntuacion(int p)         { this.puntuacion = p; }

    @Override
    public String toString() {
        return String.format("Jugador[%d] %s – %d pts", id, nick, puntuacion);
    }
}
