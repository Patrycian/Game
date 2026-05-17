package modelo;

/**
 * Superclase abstracta de los héroes con sistema de Puntos de Magia (PM).
 *
 * <p>Extiende {@link Heroe} añadiendo una reserva de PM que alimenta las
 * habilidades mágicas definidas por las subclases. La lógica concreta de cada
 * habilidad vive en los objetos {@link Habilidad} que devuelve
 * {@link #getHabilidades()}, implementado en Mago y Clérigo.</p>
 */
public abstract class Magico extends Heroe {

    // ── Variables ─────────────────────────────────────────────────────────────

    private int pm;     // PM actuales
    private int pmMax;  // PM máximos

    // ── Constructor ───────────────────────────────────────────────────────────

    protected Magico(String nombre, int puntosGolpe, int defensa, int poder, int pm) {
        super(nombre, puntosGolpe, defensa, poder);
        this.pm    = pm;
        this.pmMax = pm;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /** @return PM actuales del héroe */
    public int getPm()    { return pm; }

    /** @return PM máximos del héroe */
    public int getPmMax() { return pmMax; }

    /**
     * Establece los PM actuales, siempre entre 0 y {@code pmMax}.
     *
     * @param pm nuevo valor de PM (se clampea al rango [0, pmMax])
     */
    public void setPm(int pm) {
        this.pm = Math.max(0, Math.min(pmMax, pm));
    }

    /** @return porcentaje de PM actual respecto al máximo (0.0 – 1.0) */
    public double getPorcentajePm() {
        return pmMax == 0 ? 0.0 : (double) pm / pmMax;
    }

    // ── Métodos ───────────────────────────────────────────────────────────────

    /**
     * Intenta gastar {@code coste} PM.
     *
     * @param coste PM a consumir
     * @return {@code true} si había suficientes PM y se han consumido; {@code false} si no
     */
    public boolean gastarPm(int coste) {
        if (pm < coste) { return false; }
        pm -= coste;
        return true;
    }

    /** Restaura todos los PM al máximo. */
    public void restaurarPm() {
        this.pm = pmMax;
    }

    /**
     * Restaura parcialmente los PM sin sobrepasar el máximo.
     *
     * @param cantidad PM a recuperar
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
