package modelo;

/**
 * Superclase abstracta de los héroes jugables. Añade la habilidad especial
 * (usable una vez por combate) y el vínculo con el jugador propietario.
 */
public abstract class Heroe extends Personaje {

	private boolean habilidadUsada; // la habilidad especial se agota por combate
	private String nombreHabilidad;
	private String descHabilidad;

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

	/**
	 * Devuelve la ruta del recurso de imagen del héroe (PNG en /recursos/imagen/).
	 * Cada subclase debe implementarlo con su propio archivo.
	 */
	public abstract String getRutaImagen();
}
