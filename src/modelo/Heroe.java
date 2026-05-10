package modelo;

/**
 * Superclase abstracta de los héroes jugables. Añade la habilidad especial
 * (usable una vez por combate) y el vínculo con el jugador propietario.
 */
public abstract class Heroe extends Personaje {

	private String nombreHabilidad;
	private String descHabilidad;

	/**
	 * Mensaje de defensa pendiente: lo registra una subclase cuando absorbe o
	 * reduce un ataque entrante (p. ej. Escudo Arcano del Mago).
	 * El motor lo consume para sustituir el mensaje de golpe del enemigo.
	 */
	private String mensajeDefensa = null;

	protected Heroe(String nombre, int puntosGolpe, int defensa, int poder, String nombreHabilidad,
			String descHabilidad) {
		super(nombre, puntosGolpe, defensa, poder);
		this.nombreHabilidad = nombreHabilidad;
		this.descHabilidad = descHabilidad;
	}

	// ── Habilidad especial ────────────────────────────────────────────────────

	/**
	 * Activa la habilidad especial del héroe sobre el objetivo indicado.
	 *
	 * @param objetivo el personaje sobre el que se aplica (puede ser el enemigo o
	 *                 él mismo)
	 * @return descripción de lo que ocurrió
	 */
	public final String usarHabilidad(Personaje objetivo) {
		return aplicarHabilidad(objetivo);
	}

	/**
	 * Implementación concreta de la habilidad especial. Las subclases la definen y
	 * devuelven un texto descriptivo del efecto.
	 */
	protected abstract String aplicarHabilidad(Personaje objetivo);

	/**
	 * Gancho que se llama al inicio de cada nueva fase.
	 * La implementación base no hace nada; las subclases pueden sobreescribirlo
	 * para revertir efectos temporales (p. ej. Bendición Sagrada del Clérigo).
	 */
	public void reiniciarHabilidad() { }

	public String getNombreHabilidad() {
		return nombreHabilidad;
	}

	public String getDescHabilidad() {
		return descHabilidad;
	}

	// ── Mecanismo de absorción de daño ────────────────────────────────────────

	/**
	 * Permite a una subclase registrar un mensaje que sustituirá al del ataque
	 * enemigo en el log de combate (p. ej. "¡Escudo Arcano absorbió el golpe!").
	 * Se llama típicamente desde un override de {@link #recibirAtaque}.
	 */
	protected void registrarMensajeDefensa(String msg) {
		this.mensajeDefensa = msg;
	}

	/**
	 * El motor llama a este método tras {@code enemigo.realizarAtaque(heroe)}.
	 * Si devuelve un valor no nulo, ese texto reemplaza el mensaje del ataque en el log.
	 * Se consume al leerlo (no persiste entre llamadas).
	 *
	 * @return mensaje de defensa registrado, o {@code null} si no hay ninguno
	 */
	public String consumirMensajeDefensa() {
		String m = mensajeDefensa;
		mensajeDefensa = null;
		return m;
	}

	/**
	 * Devuelve la ruta del recurso de imagen del héroe (PNG en /recursos/imagen/).
	 * Cada subclase debe implementarlo con su propio archivo.
	 */
	public abstract String getRutaImagen();
}
