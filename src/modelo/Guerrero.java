package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Guerrero extends Heroe {

	private static final int BONUS_DEFENSA_POSTURA = 8;

	public static final int ENERGIA_MAX = 50;
	public static final int COSTE_GOLPE_DEVASTADOR = 20;
	public static final int COSTE_POSTURA_HIERRO = 15;
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

	public boolean isPosturaDeHierroActiva() {
		return posturaDeHierroActiva;
	}

	public int getEnergia() {
		return energia;
	}

	public int getEnergiaMax() {
		return ENERGIA_MAX;
	}

	/**
	 * Fracción de Energía restante (0.0 – 1.0) para alimentar la barra de
	 * progreso.
	 */
	public double getPorcentajeEnergia() {
		return (double) energia / ENERGIA_MAX;
	}

	@Override
	public List<Habilidad> getHabilidades() {
		return habilidades;
	}

	// ── Métodos ───────────────────────────────────────────────────────────────

	public boolean gastarEnergia(int coste) {
		if (energia < coste) {
			return false;
		}
		energia = Math.max(0, energia - coste); //controlamos (otra vez) negativos
		return true;
	}


	public void regenerarEnergia() {
		energia = Math.min(ENERGIA_MAX, energia + REGEN_POR_TURNO);
	}
	
	/**
	 * Resetea Postura de Hierro al inicio de cada nueva fase para que no persista de
	 * un combate al siguiente y restaura energía al máximo.
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

	private final List<Habilidad> habilidades = Collections.unmodifiableList(Arrays.asList(

			new Habilidad() { //habilidad 1
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

			new Habilidad() { //habilidad 2
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
				public String getRutaAudio() {
					return "/recursos/audio/energia.mp3";
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
