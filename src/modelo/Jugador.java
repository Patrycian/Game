package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un jugador registrado en el sistema.
 *
 * <p>Un jugador tiene un nick único, una puntuación acumulada entre partidas
 * y puede poseer varios héroes (relación 1-a-muchos con la tabla
 * {@code personajes} de la BD).</p>
 *
 * <p>La puntuación crece +10 puntos por cada combate ganado y se persiste en BD
 * a través de {@code dao.JugadorDAO.actualizarPuntuacion}.</p>
 */
public class Jugador {

    private int          id;           // id auto-generado en BD (0 si aún no persistido)
    private String       nick;         // nombre único del jugador
    private int          puntuacion;   // puntuación acumulada en todas las partidas
    private List<Heroe>  personajes;   // héroes asociados (cargados en memoria según necesidad)

    /**
     * Constructor para un jugador nuevo (aún sin id de BD).
     * La puntuación empieza en 0.
     *
     * @param nick nombre único del jugador
     */
    public Jugador(String nick) {
        this.nick       = nick;
        this.puntuacion = 0;
        this.personajes = new ArrayList<>();
        this.id         = 0;
    }

    /**
     * Constructor para reconstruir un jugador cargado desde la BD.
     *
     * @param id         id de la tabla {@code jugadores}
     * @param nick       nombre del jugador
     * @param puntuacion puntuación acumulada guardada en BD
     */
    public Jugador(int id, String nick, int puntuacion) {
        this.id         = id;
        this.nick       = nick;
        this.puntuacion = puntuacion;
        this.personajes = new ArrayList<>();
    }

    // ── Lógica de puntuación ──────────────────────────────────────────────────

    /**
     * Añade puntos a la puntuación total del jugador.
     * Se llama desde {@code MazmorraController} al ganar un combate (+10 por victoria).
     *
     * @param cantidad puntos a añadir (debe ser positivo)
     */
    public void sumarPuntos(int cantidad) {
        this.puntuacion += cantidad;
    }

    // ── Gestión de personajes ─────────────────────────────────────────────────

    /**
     * Añade un héroe a la lista en memoria de este jugador.
     * No persiste en BD; solo actualiza el estado local.
     *
     * @param h héroe a añadir
     */
    public void agregarPersonaje(Heroe h) {
        personajes.add(h);
    }

    /**
     * @return lista de héroes asociados a este jugador en memoria
     */
    public List<Heroe> getPersonajes() {
        return personajes;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /** @return id en BD (0 si aún no persistido) */
    public int    getId()              { return id; }
    /** @param id id asignado por la BD tras el INSERT */
    public void   setId(int id)        { this.id = id; }

    /** @return nick único del jugador */
    public String getNick()            { return nick; }
    /** @param nick nuevo nick */
    public void   setNick(String nick) { this.nick = nick; }

    /** @return puntuación acumulada */
    public int    getPuntuacion()              { return puntuacion; }
    /** @param p nueva puntuación (se usa al recargar desde BD) */
    public void   setPuntuacion(int p)         { this.puntuacion = p; }

    @Override
    public String toString() {
        return String.format("Jugador[%d] %s – %d pts", id, nick, puntuacion);
    }
}
