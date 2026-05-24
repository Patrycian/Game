package modelo;

import java.util.List;

public abstract class Heroe extends Personaje {

	private String mensajeDefensa = null;

	// ── Constructor ───────────────────────────────────────────────────────────

	protected Heroe(String nombre, int puntosGolpe, int defensa, int poder) {
		super(nombre, puntosGolpe, defensa, poder);
	}

	// ── Getters/Setters ─────────────────────────────────────────────────────

	public abstract String getRutaImagen();

	public abstract List<Habilidad> getHabilidades();

	// ── Métodos ───────────────────────────────────────────────────────────────

	/**
	 * Reinicia el estado de las habilidades al inicio de una nueva fase. Las
	 * subclases lo sobreescriben si tienen estado que limpiar (ej: buffs activos).
	 */
	public void reiniciarHabilidad() {
	}

	/**
	 * Registra un mensaje que sustituye al del ataque enemigo en el log de combate.
	 */
	protected void registrarMensajeDefensa(String mensaje) {
		this.mensajeDefensa = mensaje;
	}

	/**
	 * El motor llama a este método tras el ataque enemigo. Lee y borra el mensaje,
	 * lo llama el motor de combate después de cada ataque enemigo
	 */
	public String consumirMensajeDefensa() {
		String mensaje = mensajeDefensa; //guarda el valor
		mensajeDefensa = null; //borra
		return mensaje;//devuelve el valor anterior
	}
}
