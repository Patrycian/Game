package modelo;

public abstract class Personaje {

	private int id;
	private String nombre;
	private int puntosGolpeMax;
	private int puntosGolpe;
	private int defensa;
	private int poder;

	// ── Constructor ───────────────────────────────────────────────────────────

	protected Personaje(String nombre, int puntosGolpe, int defensa, int poder) {
		this.nombre = nombre;
		this.puntosGolpeMax = puntosGolpe;
		this.puntosGolpe = puntosGolpe;
		this.defensa = defensa;
		this.poder = poder;
		this.id = 0;
	}

	// ── Getters y setters ─────────────────────────────────────────────────────

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getPuntosGolpe() {
		return puntosGolpe;
	}

	public void setPuntosGolpe(int v) {
		this.puntosGolpe = Math.max(0, v);
	}

	public int getPuntosGolpeMax() {
		return puntosGolpeMax;
	}

	public int getDefensa() {
		return defensa;
	}

	public void setDefensa(int d) {
		this.defensa = d;
	}

	public int getPoder() {
		return poder;
	}

	public void setPoder(int p) {
		this.poder = p;
	}

	public abstract String getTipo();

	public abstract String getIcono();

	// ── Métodos ───────────────────────────────────────────────────────────────

	/**
	 * Aplica el daño de un ataque básico recibido de otro personaje.
	 */
	public int recibirAtaque(Personaje atacante) {
		int danio = Math.max(1, atacante.getPoder() - this.defensa);
		this.puntosGolpe = Math.max(0, this.puntosGolpe - danio);
		return danio;
	}

	/**
	 * Aplica daño directo al personaje, ignorando su defensa.
	 *
	 * Se usa para efectos que no son ataques convencionales, como la penalización
	 * por huida fallida.
	 */
	public int recibirDanio(int danio) {
		int efectivo = Math.max(0, danio);
		this.puntosGolpe = Math.max(0, this.puntosGolpe - efectivo);
		return efectivo;
	}

	/**
	 * Comprueba si el personaje sigue con vida.
	 */
	public boolean estaVivo() {
		return puntosGolpe > 0;
	}

	/**
	 * Restaura los puntos de golpe actuales a su valor máximo. Útil para reiniciar
	 * el estado entre combates o en pruebas.
	 */
	public void restaurarVida() {
		this.puntosGolpe = this.puntosGolpeMax;
	}

	/**
	 * Cura al personaje restaurando una cantidad fija de HP, sin sobrepasar el
	 * máximo.
	 */
	public void curar(int cantidad) {
		this.puntosGolpe = Math.min(puntosGolpeMax, puntosGolpe + cantidad);
	}

	/**
	 * Calcula el porcentaje de vida restante como valor entre 0.0 y 1.0. Se usa
	 * para actualizar las barras de progreso en la UI.
	 */
	public double getPorcentajeVida() {
		if (puntosGolpeMax == 0) {
			return 0;
		}
		return (double) puntosGolpe / puntosGolpeMax;
	}

	// ── ToString ──────────────────────────────────────────────────────────────

	@Override
	public String toString() {
		return String.format("%s [%s] HP:%d/%d DEF:%d POD:%d", nombre, getTipo(), puntosGolpe, puntosGolpeMax, defensa,
				poder);
	}
}
