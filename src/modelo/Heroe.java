package modelo;

/**
 * Superclase abstracta de los héroes jugables. Añade la habilidad especial
 * (usable una vez por combate) y el vínculo con el jugador propietario.
 */
public abstract class Heroe extends Personaje {

	private boolean habilidadUsada; // la habilidad especial se agota por combate
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
		this.habilidadUsada = false;
	}

	// ── Habilidad especial ────────────────────────────────────────────────────

	/**
	 * Intenta activar la habilidad especial del héroe. Si ya fue usada en este
	 * combate, no tiene efecto y devuelve null.
	 *
	 * @param objetivo el personaje sobre el que se aplica (puede ser el enemigo o
	 *                 él mismo)
	 * @return descripción de lo que ocurrió, o null si la habilidad ya estaba
	 *         gastada
	 */
	public final String usarHabilidad(Personaje objetivo) {
		if (habilidadUsada)
			return null;
		habilidadUsada = true;
		return aplicarHabilidad(objetivo);
	}

	/**
	 * Implementación concreta de la habilidad especial. Las subclases la definen y
	 * devuelven un texto descriptivo del efecto.
	 */
	protected abstract String aplicarHabilidad(Personaje objetivo);

	/** Reinicia la habilidad para un nuevo combate. */
	public void reiniciarHabilidad() {
		habilidadUsada = false;
	}

	public boolean isHabilidadUsada() {
		return habilidadUsada;
	}

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
