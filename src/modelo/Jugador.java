package modelo;

import java.util.ArrayList;
import java.util.List;

public class Jugador {

	// ── Variables ─────────────────────────────────────────────────────────────

	private int id; // id auto-generado en BD (0 si aún no persistido)
	private String nick;
	private int puntuacion; // puntuación acumulada en todas las partidas
	private List<Heroe> personajes; // héroes asociados al jugador

	// ── Constructores ─────────────────────────────────────────────────────────

	// Constructor para un jugador nuevo (aún sin id de BD). La puntuación empieza
	// en 0.

	public Jugador(String nick) {
		this.nick = nick;
		this.puntuacion = 0;
		this.personajes = new ArrayList<>();
		this.id = 0;
	}

    //Constructor para reconstruir un jugador cargado desde la BD.

	public Jugador(int id, String nick, int puntuacion) {
		this.id = id;
		this.nick = nick;
		this.puntuacion = puntuacion;
		this.personajes = new ArrayList<>();
	}

	// ── Getters y setters ─────────────────────────────────────────────────────

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public int getPuntuacion() {
		return puntuacion;
	}

	// puntuacion nueva puntuación (se usa al recargar desde BD) 
	public void setPuntuacion(int puntuacion) {
		this.puntuacion = puntuacion;
	}

	// lista de héroes asociados a este jugador en memoria 
	public List<Heroe> getPersonajes() {
		return personajes;
	}

	// ── Métodos ───────────────────────────────────────────────────────────────

	/**
	 * Añade puntos a la puntuación total del jugador (+10 por victoria).
	 */
	public void sumarPuntos(int cantidad) {
		this.puntuacion += cantidad;
	}

	/**
	 * Añade un héroe a la lista en memoria de este jugador. No persiste en BD; solo
	 * actualiza el estado local.
	 */
	public void agregarPersonaje(Heroe heroe) {
		personajes.add(heroe);
	}

	// ── ToString ──────────────────────────────────────────────────────────────

	@Override
	public String toString() {
		return String.format("Jugador[%d] %s – %d pts", id, nick, puntuacion);
	}
}
