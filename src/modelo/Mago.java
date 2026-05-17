package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mago – alto poder mágico, baja defensa y vida.
 *
 * <h3>Habilidades disponibles en combate:</h3>
 * <ul>
 * <li><b>Bola de Fuego</b>: inflige poder × 2 de daño mágico directo, ignorando
 * la defensa del enemigo. Sin coste de PM.</li>
 * <li><b>Escudo Arcano</b>: barrera mágica que absorbe por completo el
 * siguiente ataque enemigo. Coste: {@value #COSTE_ESCUDO} PM.</li>
 * </ul>
 */
public class Mago extends Magico {

	// ── Variables ─────────────────────────────────────────────────────────────

	private static final int COSTE_ESCUDO = 10;

	private boolean escudoActivo = false;

	// ── Constructor ───────────────────────────────────────────────────────────

	public Mago(String nombre) {
		super(nombre, /* hp */ 80, /* def */ 5, /* poder */ 25, /* pm */ 30);
	}

	// ── Getters y setters ─────────────────────────────────────────────────────

	@Override
	public String getTipo() {
		return "MAGO";
	}

	@Override
	public String getIcono() {
		return "🧙";
	}

	@Override
	public String getRutaImagen() {
		return "/recursos/imagen/mago.png";
	}

	/**
	 * Lista inmutable de las dos habilidades del mago.
	 *
	 * @return catálogo de habilidades del mago
	 */
	@Override
	public List<Habilidad> getHabilidades() {
		return habilidades;
	}

	// ── Métodos ───────────────────────────────────────────────────────────────

	/**
	 * Si el Escudo Arcano está activo, absorbe el golpe por completo (daño 0) y
	 * registra un mensaje descriptivo que el motor mostrará en el log. El escudo se
	 * consume tras absorber un único ataque.
	 */
	@Override
	public int recibirAtaque(Personaje atacante) {
		if (escudoActivo) {
			escudoActivo = false;
			registrarMensajeDefensa("🛡 ¡Escudo Arcano! El ataque de " + atacante.getNombre()
					+ " fue absorbido por completo. " + getNombre() + " no recibe daño.");
			return 0;
		}
		return super.recibirAtaque(atacante);
	}

	/**
	 * Resetea el Escudo Arcano al inicio de cada nueva fase para que no persista de
	 * un combate al siguiente.
	 */
	@Override
	public void reiniciarHabilidad() {
		escudoActivo = false;
	}

	// ── Habilidades ───────────────────────────────────────────────────────────

	private final List<Habilidad> habilidades = Collections.unmodifiableList(Arrays.asList(

			new Habilidad() {
				@Override
				public String getNombre() {
					return "Bola de Fuego";
				}

				@Override
				public String getDescripcion() {
					return "Lanza una bola de fuego que inflige" + " el doble de tu poder (poder × 2)"
							+ " de daño mágico directo," + " ignorando la defensa del enemigo.";
				}

				@Override
				public TipoRecurso getTipoRecurso() {
					return TipoRecurso.NINGUNO;
				}

				@Override
				public int getCoste() {
					return 0;
				}

				@Override
				public boolean puedeUsarse(Heroe heroe) {
					return true;
				}

				@Override
				public String ejecutar(Heroe heroe, Personaje objetivo) {
					int danio = getPoder() * 2;
					objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
					return String.format("🔥 ¡Bola de Fuego! %s recibe %d puntos de daño mágico directo.",
							objetivo.getNombre(), danio);
				}
			},

			new Habilidad() {
				@Override
				public String getNombre() {
					return "Escudo Arcano";
				}

				@Override
				public String getDescripcion() {
					return "Envuelve al mago en una barrera mágica" + " que absorbe por completo el siguiente"
							+ " ataque enemigo." + "  Coste: " + COSTE_ESCUDO + " PM";
				}

				@Override
				public TipoRecurso getTipoRecurso() {
					return TipoRecurso.MANA;
				}

				@Override
				public int getCoste() {
					return COSTE_ESCUDO;
				}

				@Override
				public boolean afectaAlEnemigo() {
					return false;
				}

				@Override
				public boolean puedeUsarse(Heroe heroe) {
					return ((Magico) heroe).getPm() >= COSTE_ESCUDO && !escudoActivo;
				}

				@Override
				public String ejecutar(Heroe heroe, Personaje objetivo) {
					gastarPm(COSTE_ESCUDO);
					escudoActivo = true;
					return "🛡 ¡Escudo Arcano! " + getNombre() + " se envuelve en una barrera mágica."
							+ " El siguiente ataque enemigo será absorbido.";
				}
			}));
}
