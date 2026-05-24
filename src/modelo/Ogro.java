package modelo;

public class Ogro extends Enemigo {

	// ── Constructor ───────────────────────────────────────────────────────────────

	// constructor con datos de la BD

	public Ogro(EnemigoDatos datos) {
		super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
	}

	// constructor por defecto con valores hardcoreados. Fallback si BD no disponible

	public Ogro() {
		super("Ogro Brutal", /* hp */ 100, /* def */ 12, /* poder */ 20);
	}

	// ── Getters ───────────────────────────────────────────────────────────────

	@Override
	public String getTipo() {
		return "OGRO";
	}

	@Override
	public String getIcono() {
		return "👹";
	}

	// ── Métodos ───────────────────────────────────────────────────────────────

	// El Ogro usa el ataque físico básico heredado de Enemigo así que no
	// sobreescribe realizarAtaque().

	/**
	 * El Ogro tiene un 30 % de probabilidad de soltar una Poción de Curación.
	 */
	@Override
	public TipoDrop generarDrop() {
		return RNG.nextInt(100) < 30 ? TipoDrop.POCION_VIDA : null;
	}
}
