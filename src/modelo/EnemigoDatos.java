package modelo;

/**
 * Actúa como puente entre EnemigoDAO y las clases concretas de los enmigos.
 * Permite que los constructores de estas clases acepten datos dinámicos de la
 * BD en lugar de depender exclusivamente de valores hardcodeados.
 *
 * Si la BD no está disponible, EnemigoDAO devuelve un catálogo vacío y los
 * enemigos se instancian con sus constructores por defecto.
 */
public class EnemigoDatos {

	private final String tipo; // Clave del catálogo: "OGRO", "GOBLIN", "SAGA", "DRAGON"
	private final String nombre; 
	private final int puntosGolpe; // HP máximos del enemigo
	private final int defensa; // Defensa base
	private final int poder; // Poder de ataque base
	private final String icono; 
	private final boolean esJefe; // true si es el jefe final

	// ── Constructor ─────────────────────────────────────────────────────────────

	public EnemigoDatos(String tipo, String nombre, int puntosGolpe, int defensa, int poder, String icono,
			boolean esJefe) {
		this.tipo = tipo;
		this.nombre = nombre;
		this.puntosGolpe = puntosGolpe;
		this.defensa = defensa;
		this.poder = poder;
		this.icono = icono;
		this.esJefe = esJefe;
	}

	// ── Getters ───────────────────────────────────────────────────────────────

	public String getTipo() {
		return tipo;
	}

	public String getNombre() {
		return nombre;
	}

	public int getPuntosGolpe() {
		return puntosGolpe;
	}

	public int getDefensa() {
		return defensa;
	}

	public int getPoder() {
		return poder;
	}

	public String getIcono() {
		return icono;
	}

	public boolean isEsJefe() {
		return esJefe;
	}

	// ── toString ───────────────────────────────────────────────────────────────

	@Override
	public String toString() {
		return String.format("EnemigoDatos[%s] hp:%d def:%d pod:%d %s", tipo, puntosGolpe, defensa, poder, icono);
	}
}
