package modelo;

public abstract class Magico extends Heroe {

	// ── Variables ─────────────────────────────────────────────────────────────

	private int pm; // PM actuales
	private int pmMax; // PM máximos

	// ── Constructor ───────────────────────────────────────────────────────────

	protected Magico(String nombre, int puntosGolpe, int defensa, int poder, int pm) {
		super(nombre, puntosGolpe, defensa, poder);
		this.pm = pm;
		this.pmMax = pm;
	}

	// ── Getters y setters ─────────────────────────────────────────────────────

	public int getPm() {
		return pm;
	}

	public int getPmMax() {
		return pmMax;
	}

	public void setPm(int pm) {
		this.pm = Math.max(0, Math.min(pmMax, pm));
	}

	/** Porcentaje de PM actual respecto al máximo (0.0 – 1.0) */
	public double getPorcentajePm() {
		return pmMax == 0 ? 0.0 : (double) pm / pmMax;
	}

	// ── Métodos ───────────────────────────────────────────────────────────────

	/**
	 * Permite el gasto de PM.
	 */
	public boolean gastarPm(int coste) {
		if (pm < coste) {
			return false;
		}
		pm -= coste;
		return true;
	}

	/** Restaura todos los PM al máximo. */
	public void restaurarPm() {
		this.pm = pmMax;
	}

	/**
	 * Restaura parcialmente los PM sin sobrepasar el máximo.
	 */
	public void restaurarPmParcial(int cantidad) {
		this.pm = Math.min(pmMax, pm + cantidad);
	}

	// ── ToString ──────────────────────────────────────────────────────────────

	@Override
	public String toString() {
		return super.toString() + String.format(" PM:%d/%d", pm, pmMax);
	}
}
