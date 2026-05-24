package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Clerigo extends Magico {

	private static final int BONUS_DEF = 8;
	private static final int COSTE_BENDICION = 7;
	private static final int COSTE_CURACION = 10;

	private boolean bendicionActiva = false;

	// ── Constructor ───────────────────────────────────────────────────────────

	public Clerigo(String nombre) {
		super(nombre, /* hp */ 100, /* def */ 12, /* poder */ 14, /* pm */ 20);
	}

	// ── Getters/setters ─────────────────────────────────────────────────────

	@Override
	public String getTipo() { //Importante para BD
		return "CLERIGO";
	}

	@Override
	public String getIcono() {
		return "✝️";
	}

	@Override
	public String getRutaImagen() {
		return "/recursos/imagen/clerigo.png";
	}

	@Override
	public List<Habilidad> getHabilidades() {
		return habilidades;
	}

	// ── Métodos ───────────────────────────────────────────────────────────────

	/**
	 * Al inicio de cada nueva fase revierte el bonus de defensa de la Bendición
	 * Sagrada para que no se acumule entre combates.
	 */
	@Override
	public void reiniciarHabilidad() {
		desactivarBendicion();
	}

	/**
	 * Quita el bonus de defensa de la Bendición Sagrada si estaba activa.
	 */
	private void desactivarBendicion() {
		if (bendicionActiva) {
			bendicionActiva = false;
			setDefensa(getDefensa() - BONUS_DEF);
		}
	}

	// ── Habilidades ───────────────────────────────────────────────────────────

	private final List<Habilidad> habilidades = Collections.unmodifiableList(Arrays.asList(

			new Habilidad() { //habilidad 1
				@Override
				public String getNombre() {
					return "Curación Divina";
				}

				@Override
				public String getDescripcion() {
					return "Invoca la gracia divina para" + " recuperar el 40 % de la vida" + " máxima del clérigo."
							+ "  Coste: " + COSTE_CURACION + " PM";
				}

				@Override
				public TipoRecurso getTipoRecurso() {
					return TipoRecurso.MANA;
				}

				@Override
				public int getCoste() {
					return COSTE_CURACION;
				}

				@Override
				public boolean afectaAlEnemigo() {
					return false;
				}

				@Override
				public String getRutaAudio() {
					return "/recursos/audio/defensaMagica.mp3";
				}

				@Override
				public boolean puedeUsarse(Heroe heroe) {
					return ((Magico) heroe).getPm() >= COSTE_CURACION; //comprobamos pm
				}

				@Override
				public String ejecutar(Heroe heroe, Personaje objetivo) {
					gastarPm(COSTE_CURACION);
					int curacion = (int) Math.round(getPuntosGolpeMax() * 0.4);//calculamos el 40% del HP máx
					curar(curacion);
					return String.format("✨ ¡Curación Divina! %s recupera %d puntos de vida. (HP: %d/%d)", getNombre(),
							curacion, getPuntosGolpe(), getPuntosGolpeMax());
				}
			},

			new Habilidad() { //habilidad 2
				@Override
				public String getNombre() {
					return "Bendición Sagrada";
				}

				@Override
				public String getDescripcion() {
					return "Invoca un aura divina que aumenta" + " temporalmente la defensa del clérigo."
							+ " Usable una vez por fase." + "  Coste: " + COSTE_BENDICION + " PM";
				}

				@Override
				public TipoRecurso getTipoRecurso() {
					return TipoRecurso.MANA;
				}

				@Override
				public int getCoste() {
					return COSTE_BENDICION;
				}

				@Override
				public boolean afectaAlEnemigo() {
					return false;
				}

				@Override
				public String getRutaAudio() {
					return "/recursos/audio/defensaMagica.mp3";
				}

				@Override
				public boolean puedeUsarse(Heroe heroe) {
					return !bendicionActiva && ((Magico) heroe).getPm() >= COSTE_BENDICION;
				}

				@Override
				public String ejecutar(Heroe heroe, Personaje objetivo) {
					gastarPm(COSTE_BENDICION);
					bendicionActiva = true;
					setDefensa(getDefensa() + BONUS_DEF);
					return String.format(
							"✝️ ¡Bendición Sagrada! %s invoca un aura divina." + " Defensa aumentada en %d. (DEF: %d)",
							getNombre(), BONUS_DEF, getDefensa());
				}
			}));
}
