package modelo;

public class Dragon extends Enemigo {

	private static final int PM_DRAGON = 40;
	private static final int COSTE_ALIENTO = 15;

	private int turnoActual;

	// ── Constructor ─────────────────────────────────────────────────────────

	// Constructor con stats cargados desde BD
	public Dragon(EnemigoDatos datos) {
		super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder(), PM_DRAGON);
		this.turnoActual = 0;
	}

	// Constructor por defecto (fallback si la BD no está disponible)
	public Dragon() {
		super("Ignaroth, el Dragón Eterno", /* hp */ 200, /* def */ 18, /* poder */ 28, PM_DRAGON);
		this.turnoActual = 0;
	}

	// ── Getters ─────────────────────────────────────────────────────

	@Override
	public String getTipo() {
		return "DRAGON";
	}

	@Override
	public String getIcono() {
		return "🐉";
	}

	// ── Métodos ─────────────────────────────────────────────────────────

	/**
	 * Cada 3 turnos intenta usar Aliento de Fuego. daño = poder × 2, ignorando la
	 * defensa del héroe. Si no tiene PM suficientes en ese turno, o en los turnos
	 * intermedios, realiza un ataque básico.
	 */
	@Override
	public String realizarAtaque(Heroe objetivo) {
		turnoActual++;

		if (turnoActual % 3 == 0 && gastarPm(COSTE_ALIENTO)) {
			// Aliento de Fuego (ignora defensa)
			int danio = getPoder() * 2;
			objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
			return String.format("🔥 ¡ALIENTO DE FUEGO!  →  -%d HP  [%s: %d/%d HP]  |  PM Dragón: %d/%d", danio,
					objetivo.getNombre(), objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax(), getPm(), getPmMax());
		} else {
			// Ataque básico
			int danio = objetivo.recibirAtaque(this);
			return String.format("🐉 %s golpea con su cola  →  -%d HP  [%s: %d/%d HP]", getNombre(), danio,
					objetivo.getNombre(), objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax());
		}
	}
}
