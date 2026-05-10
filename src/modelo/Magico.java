package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Subclase abstracta de Héroe para personajes con poder mágico (Mago, Clérigo).
 * <p>
 * Añade dos elementos propios de los personajes mágicos:
 * <ul>
 *   <li><b>PM (Puntos de Magia)</b>: recurso que se consume al lanzar hechizos.
 *       Cada personaje mágico tiene un máximo definido en su constructor y
 *       métodos para gastarlo y restaurarlo.</li>
 *   <li><b>Habilidades mágicas</b>: catálogo de hechizos o capacidades especiales
 *       que el personaje conoce. Las subclases registran sus habilidades llamando
 *       a {@link #agregarHabilidadMagica(String, String)} desde su propio constructor.</li>
 * </ul>
 */
public abstract class Magico extends Heroe {

    // ── Puntos de Magia ──────────────────────────────────────────────────────

    private int pm;     // PM actuales
    private int pmMax;  // PM máximos

    // ── Habilidades mágicas ──────────────────────────────────────────────────

    /** Cada entrada: [0] = nombre, [1] = descripción. */
    private final List<String[]> habilidadesMagicas = new ArrayList<>();

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * @param nombre          nombre del personaje
     * @param puntosGolpe     HP máximos
     * @param defensa         defensa base
     * @param poder           poder de ataque base
     * @param pm              PM máximos (puntos de magia)
     * @param nombreHabilidad nombre de la habilidad especial (hereda de Héroe)
     * @param descHabilidad   descripción de la habilidad especial
     */
    protected Magico(String nombre, int puntosGolpe, int defensa, int poder, int pm,
                     String nombreHabilidad, String descHabilidad) {
        super(nombre, puntosGolpe, defensa, poder, nombreHabilidad, descHabilidad);
        this.pm    = pm;
        this.pmMax = pm;
    }

    // ── Gestión de PM ────────────────────────────────────────────────────────

    /** @return PM actuales del personaje. */
    public int getPm() { return pm; }

    /** @return PM máximos del personaje. */
    public int getPmMax() { return pmMax; }

    /**
     * Establece los PM actuales, siempre entre 0 y {@code pmMax}.
     */
    public void setPm(int pm) {
        this.pm = Math.max(0, Math.min(pmMax, pm));
    }

    /**
     * Intenta gastar {@code coste} PM.
     *
     * @param coste puntos de magia a consumir
     * @return {@code true} si había suficientes PM y se han consumido;
     *         {@code false} si no había suficientes (sin cambios).
     */
    public boolean gastarPm(int coste) {
        if (pm < coste) return false;
        pm -= coste;
        return true;
    }

    /** Restaura todos los PM al máximo. */
    public void restaurarPm() {
        this.pm = pmMax;
    }

    /**
     * Restaura parcialmente los PM.
     *
     * @param cantidad puntos de magia a recuperar (sin sobrepasar el máximo)
     */
    public void restaurarPmParcial(int cantidad) {
        this.pm = Math.min(pmMax, pm + cantidad);
    }

    /** @return porcentaje de PM actual respecto al máximo (0.0 – 1.0). */
    public double getPorcentajePm() {
        return pmMax == 0 ? 0.0 : (double) pm / pmMax;
    }

    // ── Habilidades mágicas ──────────────────────────────────────────────────

    /**
     * Registra una habilidad mágica del personaje.
     * Debe llamarse desde el constructor de cada subclase.
     *
     * @param nombre      nombre del hechizo o habilidad
     * @param descripcion descripción de lo que hace
     */
    protected void agregarHabilidadMagica(String nombre, String descripcion) {
        habilidadesMagicas.add(new String[]{nombre, descripcion});
    }

    /**
     * @return lista inmutable de habilidades mágicas; cada elemento es un
     *         array {@code String[2]} donde [0] es el nombre y [1] la descripción.
     */
    public List<String[]> getHabilidadesMagicas() {
        return Collections.unmodifiableList(habilidadesMagicas);
    }

    /**
     * Genera un resumen legible de todas las habilidades mágicas del personaje,
     * listo para mostrar en pantalla o en un log de combate.
     *
     * @return texto multi-línea con cada habilidad en formato "✨ Nombre: Descripción"
     */
    public String getResumenHabilidadesMagicas() {
        if (habilidadesMagicas.isEmpty()) return "(Sin habilidades mágicas)";
        StringBuilder sb = new StringBuilder();
        for (String[] h : habilidadesMagicas) {
            sb.append("✨ ").append(h[0]).append(": ").append(h[1]).append("\n");
        }
        return sb.toString().trim();
    }

    // ── toString extendido ───────────────────────────────────────────────────

    @Override
    public String toString() {
        return super.toString() + String.format(" PM:%d/%d", pm, pmMax);
    }
}
