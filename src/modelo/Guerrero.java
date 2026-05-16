package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Guerrero – equilibrado entre ataque y defensa.
 * <p>
 * Habilidades disponibles en combate:
 * <ul>
 * <li><b>Golpe Devastador</b> (habilidad especial): ataque con poder ×
 * 1,5.</li>
 * <li><b>Postura de Hierro</b>: aumenta la defensa en
 * {@value #BONUS_DEFENSA_POSTURA} puntos durante el resto del combate. Usable
 * una vez por fase.</li>
 * </ul>
 */
public class Guerrero extends Heroe {

	// ── Postura de Hierro ─────────────────────────────────────────────────────
	private static final int BONUS_DEFENSA_POSTURA = 8;
	private boolean posturaDeHierroActiva = false;

	// ── Catálogo de habilidades ───────────────────────────────────────────────
	private static final List<String[]> HABILIDADES = Collections.unmodifiableList(Arrays.asList(
			new String[] { "Golpe Devastador",
					"Un ataque brutal que inflige 1,5 veces tu poder menos la defensa del enemigo." },
			new String[] { "Postura de Hierro", "Adoptas una postura defensiva que aumenta tu defensa en "
					+ BONUS_DEFENSA_POSTURA + " puntos durante el resto del combate. Usable una vez por fase." }));

	// ── Constructor ───────────────────────────────────────────────────────────

	public Guerrero(String nombre) {
		super(nombre, /* hp */ 120, /* def */ 15, /* poder */ 18, "Golpe Devastador",
				"Un ataque brutal que inflige 1,5 veces tu poder menos la defensa del enemigo.");
	}

	// ── Identificación ────────────────────────────────────────────────────────

	@Override
	public String getTipo() {
		return "GUERRERO";
	}

	@Override
	public String getIcono() {
		return "⚔️";
	}

	@Override
	public String getRutaImagen() {
		return "/recursos/imagen/guerrero.png";
	}

	// ── Habilidad especial (Golpe Devastador) ─────────────────────────────────

	/**
	 * Golpe Devastador: calcula daño con poder × 1,5, descontando defensa del
	 * objetivo.
	 */
	@Override
	protected String aplicarHabilidad(Personaje objetivo) {
		int poderAmpliado = (int) Math.round(getPoder() * 1.5);
		int danio = Math.max(1, poderAmpliado - objetivo.getDefensa());
		objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
		return String.format("⚔️  ¡Golpe Devastador! %s recibe %d puntos de daño brutal.", objetivo.getNombre(), danio);
	}

	// ── Habilidad adicional: Postura de Hierro ────────────────────────────────

	/** @return {@code true} si la Postura de Hierro ya está activa este combate. */
	public boolean isPosturaDeHierroActiva() {
		return posturaDeHierroActiva;
	}

	/**
	 * Activa la Postura de Hierro: incrementa la defensa del guerrero en
	 * {@value #BONUS_DEFENSA_POSTURA} puntos permanentemente durante la fase.
	 *
	 * @return mensaje descriptivo del efecto, o {@code null} si ya estaba activa
	 */
	public String usarPosturaDeHierro() {
		if (posturaDeHierroActiva)
			return null;
		posturaDeHierroActiva = true;
		setDefensa(getDefensa() + BONUS_DEFENSA_POSTURA);
		return String.format("🛡️  ¡%s adopta la Postura de Hierro! +%d DEF. (DEF total: %d)", getNombre(),
				BONUS_DEFENSA_POSTURA, getDefensa());
	}

	/**
	 * Al inicio de cada nueva fase se revierte el bonus de defensa para que el
	 * guerrero no acumule beneficios entre fases.
	 */
	@Override
	public void reiniciarHabilidad() {
		if (posturaDeHierroActiva) {
			setDefensa(getDefensa() - BONUS_DEFENSA_POSTURA);
			posturaDeHierroActiva = false;
		}
	}

	// ── Catálogo ──────────────────────────────────────────────────────────────

	/**
	 * @return lista inmutable de habilidades del guerrero; cada elemento es un
	 *         array {@code String[2]} donde [0] = nombre y [1] = descripción.
	 */
	public List<String[]> getHabilidadesGuerrero() {
		return HABILIDADES;
	}
}
