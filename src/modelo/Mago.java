package modelo;

/**
 * Mago – alto poder mágico, defensa baja. Habilidad especial: Bola de Fuego
 * (daño doble al enemigo, una vez por combate).
 * <p>
 * Extiende {@link Magico}: tiene 30 PM y conoce dos habilidades mágicas.
 */
public class Mago extends Magico {

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
