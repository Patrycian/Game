package modelo;

/**
 * Superclase abstracta de los enemigos de la mazmorra. Extiende Personaje y
 * añade:
 * <ul>
 * <li>Comportamiento de ataque automático (IA básica).</li>
 * <li>Sistema de Puntos de Magia (PM) para enemigos que usan hechizos. Los
 * enemigos sin magia se construyen con pm = 0 y el sistema queda inactivo.</li>
 * </ul>
 */
public abstract class Enemigo extends Personaje {

	// ── Puntos de Magia ──────────────────────────────────────────────────────
	private int pm;
	private int pmMax;

	// ── Constructores ────────────────────────────────────────────────────────

	/**
	 * Constructor sin magia (pm = 0). Mantiene compatibilidad con Ogro y Goblin.
	 */
	protected Enemigo(String nombre, int puntosGolpe, int defensa, int poder) {
		this(nombre, puntosGolpe, defensa, poder, 0);
	}

	/**
	 * Constructor con PM para enemigos mágicos (Saga, Dragón).
	 *
	 * @param pm PM máximos del enemigo (0 si no usa magia)
	 */
	protected Enemigo(String nombre, int puntosGolpe, int defensa, int poder, int pm) {
		super(nombre, puntosGolpe, defensa, poder);
		this.pm = pm;
		this.pmMax = pm;
	}

	// ── Gestión de PM ────────────────────────────────────────────────────────

	/** @return PM actuales del enemigo. */
	public int getPm() {
		return pm;
	}

	/**
	 * Fija los PM actuales del enemigo, garantizando que estén entre 0 y pmMax.
	 * Se usa al reanudar una partida guardada para restaurar el estado del enemigo.
	 *
	 * @param pm PM a restaurar (se clampea a [0, pmMax])
	 */
	public void setPm(int pm) {
		this.pm = Math.max(0, Math.min(pmMax, pm));
	}

	/** @return PM máximos del enemigo (0 si no usa magia). */
	public int getPmMax() {
		return pmMax;
	}

	/**
	 * @return {@code true} si este enemigo tiene sistema de magia activo (pmMax >
	 *         0).
	 */
	public boolean tienePmMax() {
		return pmMax > 0;
	}

	/**
	 * Intenta gastar {@code coste} PM.
	 *
	 * @param coste puntos de magia a consumir
	 * @return {@code true} si había suficientes PM y se han consumido;
	 *         {@code false} si no había suficientes (sin cambios).
	 */
	public boolean gastarPm(int coste) {
		if (pm < coste) 
			return false;
		pm -= coste;
		return true;
	}

	/** @return porcentaje de PM actual respecto al máximo (0.0 – 1.0). */
	public double getPorcentajePm() {
		return pmMax == 0 ? 0.0 : (double) pm / pmMax;
	}

	// ── Imagen ───────────────────────────────────────────────────────────────

	/**
	 * Ruta del recurso de imagen del enemigo.
	 * El nombre del archivo coincide con el nombre de la clase en minúsculas.
	 * Por ejemplo: Goblin → /recursos/imagen/goblin.png
	 *
	 * @return ruta del recurso de imagen
	 */
	public String getRutaImagen() {
		return "/recursos/imagen/" + getClass().getSimpleName().toLowerCase() + ".png";
	}

	// ── Ataque ───────────────────────────────────────────────────────────────

	/**
	 * El enemigo realiza su turno de ataque contra el héroe. Por defecto es un
	 * ataque básico; las subclases pueden sobrescribir para añadir comportamiento
	 * especial (p. ej. la Saga o el Dragón).
	 *
	 * @param objetivo el héroe que recibe el ataque
	 * @return descripción del ataque realizado
	 */
	public String realizarAtaque(Heroe objetivo) {
		int danio = objetivo.recibirAtaque(this);
		return String.format("%s %s ataca a %s y causa %d puntos de daño. (HP restante: %d)", getIcono(), getNombre(),
				objetivo.getNombre(), danio, objetivo.getPuntosGolpe());
	}
}
