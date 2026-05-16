package modelo;

/**
 * Mago – alto poder mágico, defensa baja. Habilidad especial: Bola de Fuego
 * (daño doble al enemigo, una vez por combate).
 * <p>
 * Extiende {@link Magico}: tiene 30 PM y conoce dos habilidades mágicas.
 */
public class Mago extends Magico {

	// ── Escudo Arcano ────────────────────────────────────────────────────────
	private static final int COSTE_ESCUDO = 10;
	private boolean escudoActivo = false;

	public Mago(String nombre) {
		super(nombre, /* hp */ 80, /* def */ 5, /* poder */ 25, /* pm */ 30,
				"Bola de Fuego",
				"Lanza una bola de fuego que inflige el doble de tu poder (sin contar la defensa enemiga).");

		agregarHabilidadMagica("Bola de Fuego",
				"Inflige poder × 2 de daño mágico directo, ignorando la defensa del enemigo.");
		agregarHabilidadMagica("Escudo Arcano",
				"Envuelve al mago en una barrera mágica que reduce el daño del siguiente ataque recibido.");
	}

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

	// ── Reinicio de habilidades (inicio de cada fase) ────────────────────────

	/**
	 * Resetea el Escudo Arcano al inicio de cada nueva fase para que no persista
	 * de un combate al siguiente. Si el escudo quedó activo al final de la fase
	 * anterior (por ejemplo, el enemigo murió antes de atacar), se anula aquí.
	 */
	@Override
	public void reiniciarHabilidad() {
		super.reiniciarHabilidad();
		escudoActivo = false;
	}

	// ── Override: absorción de daño con el escudo ────────────────────────────

	/**
	 * Si el Escudo Arcano está activo, absorbe el golpe por completo (daño 0)
	 * y registra un mensaje descriptivo que el motor mostrará en el log.
	 * El escudo se consume tras absorber un único ataque.
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

	// ── Habilidades mágicas adicionales ──────────────────────────────────────

	@Override
	public String ejecutarHabilidadAdicional(String nombre, Personaje objetivo) {
		if ("Escudo Arcano".equals(nombre)) {
			escudoActivo = true;
			return "🛡 ¡Escudo Arcano! " + getNombre()
					+ " se envuelve en una barrera mágica. El siguiente ataque enemigo será absorbido.";
		}
		return null;
	}

	@Override
	public int getCostePmHabilidad(String nombre) {
		return "Escudo Arcano".equals(nombre) ? COSTE_ESCUDO : 0;
	}

	@Override
	public boolean isHabilidadAdicionalActiva(String nombre) {
		return "Escudo Arcano".equals(nombre) && escudoActivo;
	}

	// ── Habilidad especial ────────────────────────────────────────────────────

	/**
	 * Bola de Fuego: inflige poder × 2 directamente al objetivo, ignorando su
	 * defensa.
	 */
	@Override
	protected String aplicarHabilidad(Personaje objetivo) {
		int danio = getPoder() * 2;
		objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
		return String.format("🔥 ¡Bola de Fuego! %s recibe %d puntos de daño mágico directo.", objetivo.getNombre(),
				danio);
	}
}
