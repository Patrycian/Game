package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Guerrero – equilibrado entre ataque y defensa.
 *
 * <p>
 * Dispone de una <b>barra de Energía</b> (EN) que representa la resistencia
 * física del combatiente. Cada habilidad tiene un coste en EN; al final de cada
 * turno de ataque normal el guerrero recupera {@value #REGEN_POR_TURNO} EN.
 * </p>
 *
 * <h3>Habilidades disponibles en combate:</h3>
 * <ul>
 * <li><b>Golpe Devastador</b>: ataque con poder × 1,5 menos la defensa enemiga.
 * Coste: {@value #COSTE_GOLPE_DEVASTADOR} EN.</li>
 * <li><b>Postura de Hierro</b>: aumenta la defensa en
 * {@value #BONUS_DEFENSA_POSTURA} puntos durante el resto del combate. Usable
 * una vez por fase. Coste: {@value #COSTE_POSTURA_HIERRO} EN.</li>
 * </ul>
 *
 * <h3>Sistema de Energía:</h3>
 * <ul>
 * <li>Máximo: {@value #ENERGIA_MAX} EN.</li>
 * <li>Se restaura al máximo al comenzar cada fase
 * ({@link #reiniciarHabilidad()}).</li>
 * <li>Regenera {@value #REGEN_POR_TURNO} EN al final de cada turno de ataque
 * normal.</li>
 * </ul>
 */
public class Guerrero extends Heroe {

	// ── Variables ─────────────────────────────────────────────────────────────

	private static final int BONUS_DEFENSA_POSTURA = 8;

	/** Energía máxima del guerrero. */
	public static final int ENERGIA_MAX = 50;

	/** Coste en EN del Golpe Devastador. */
	public static final int COSTE_GOLPE_DEVASTADOR = 20;

	/** Coste en EN de la Postura de Hierro. */
	public static final int COSTE_POSTURA_HIERRO = 15;

	/**
	 * Energía recuperada automáticamente al final de cada turno de ataque normal.
	 */
	public static final int REGEN_POR_TURNO = 8;

	private boolean posturaDeHierroActiva = false;
	private int energia = ENERGIA_MAX;

	// ── Constructor ───────────────────────────────────────────────────────────

	public Guerrero(String nombre) {
		super(nombre, /* hp */ 120, /* def */ 15, /* poder */ 18);
	}

	// ── Getters y setters ─────────────────────────────────────────────────────

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

	/** @return {@code true} si la Postura de Hierro está activa en este combate */
	public boolean isPosturaDeHierroActiva() {
		return posturaDeHierroActiva;
	}

	/** @return energía actual del guerrero (0 – {@value #ENERGIA_MAX}) */
	public int getEnergia() {
		return energia;
	}

	/** @return energía máxima ({@value #ENERGIA_MAX}) */
	public int getEnergiaMax() {
		return ENERGIA_MAX;
	}

	/**
	 * Fracción de Energía restante (0.0 – 1.0), para alimentar la barra de
	 * progreso.
	 *
	 * @return {@code energia / ENERGIA_MAX}
	 */
	public double getPorcentajeEnergia() {
		return (double) energia / ENERGIA_MAX;
	}

	/**
	 * Lista inmutable de las dos habilidades del guerrero. Cada {@link Habilidad}
	 * encapsula nombre, descripción, coste, condición de uso y lógica de ejecución.
	 *
	 * @return catálogo de habilidades del guerrero
	 */
	@Override
	public List<Habilidad> getHabilidades() {
		return habilidades;
	}

	// ── Métodos ───────────────────────────────────────────────────────────────

	/**
	 * Intenta gastar {@code coste} puntos de Energía.
	 *
	 * @param coste puntos de EN a consumir
	 * @return {@code true} si había suficiente EN y se descontó; {@code false} si
	 *         no
	 */
	public boolean gastarEnergia(int coste) {
		if (energia < coste) {
			return false;
		}
		energia = Math.max(0, energia - coste);
		return true;
	}

	/**
	 * Regenera {@value #REGEN_POR_TURNO} puntos de Energía al final de cada turno
	 * de ataque normal, sin sobrepasar {@value #ENERGIA_MAX}.
	 */
	public void regenerarEnergia() {
		energia = Math.min(ENERGIA_MAX, energia + REGEN_POR_TURNO);
	}

	/**
	 * Al inicio de cada nueva fase revierte el bonus de defensa de la Postura de
	 * Hierro y restaura la Energía al máximo para que el guerrero empiece fresco.
	 */
	@Override
	public void reiniciarHabilidad() {
		if (posturaDeHierroActiva) {
			setDefensa(getDefensa() - BONUS_DEFENSA_POSTURA);
			posturaDeHierroActiva = false;
		}
		energia = ENERGIA_MAX;
	}

	// ── Habilidades ───────────────────────────────────────────────────────────

	/**
	 * Catálogo de habilidades inicializado una sola vez. Los objetos anónimos
	 * capturan la instancia de Guerrero y acceden directamente a sus campos y
	 * métodos.
	 */
	private final List<Habilidad> habilidades = Collections.unmodifiableList(Arrays.asList(

			new Habilidad() {
				@Override
				public String getNombre() {
					return "Golpe Devastador";
				}

				@Override
				public String getDescripcion() {
					return "Ataque brutal que inflige 1,5× tu poder" + " menos la defensa enemiga." + "  Coste: "
							+ COSTE_GOLPE_DEVASTADOR + " EN";
				}

				@Override
				public TipoRecurso getTipoRecurso() {
					return TipoRecurso.ENERGIA;
				}

				@Override
				public int getCoste() {
					return COSTE_GOLPE_DEVASTADOR;
				}

				@Override
				public boolean puedeUsarse(Heroe heroe) {
					return energia >= COSTE_GOLPE_DEVASTADOR;
				}

				@Override
				public String ejecutar(Heroe heroe, Personaje objetivo) {
					gastarEnergia(COSTE_GOLPE_DEVASTADOR);
					int poderAmpliado = (int) Math.round(getPoder() * 1.5);
					int danio = Math.max(1, poderAmpliado - objetivo.getDefensa());
					objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
					return String.format("⚔️  ¡GOLPE DEVASTADOR! %s recibe %d de daño brutal.  [%s: %d/%d HP]",
							objetivo.getNombre(), danio, objetivo.getNombre(), objetivo.getPuntosGolpe(),
							objetivo.getPuntosGolpeMax());
				}
			},

			new Habilidad() {
				@Override
				public String getNombre() {
					return "Postura de Hierro";
				}

				@Override
				public String getDescripcion() {
					return "Adoptas una postura defensiva:" + " +" + BONUS_DEFENSA_POSTURA + " DEF"
							+ " durante el resto del combate." + " Usable una vez por fase." + "  Coste: "
							+ COSTE_POSTURA_HIERRO + " EN";
				}

				@Override
				public TipoRecurso getTipoRecurso() {
					return TipoRecurso.ENERGIA;
				}

				@Override
				public int getCoste() {
					return COSTE_POSTURA_HIERRO;
				}

				@Override
				public boolean afectaAlEnemigo() {
					return false;
				}

				@Override
				public boolean puedeUsarse(Heroe heroe) {
					return !posturaDeHierroActiva && energia >= COSTE_POSTURA_HIERRO;
				}

				@Override
				public String ejecutar(Heroe heroe, Personaje objetivo) {
					gastarEnergia(COSTE_POSTURA_HIERRO);
					posturaDeHierroActiva = true;
					setDefensa(getDefensa() + BONUS_DEFENSA_POSTURA);
					return String.format("🛡️  ¡%s adopta la Postura de Hierro!  +%d DEF  [DEF total: %d]", getNombre(),
							BONUS_DEFENSA_POSTURA, getDefensa());
				}
			}));
}
