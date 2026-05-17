package modelo;

import java.util.Random;

/**
 * Superclase abstracta de los enemigos de la mazmorra. Extiende {@link Personaje} y añade:
 * <ul>
 *   <li>Comportamiento de ataque automático (IA básica).</li>
 *   <li>Sistema de Puntos de Magia (PM) para enemigos que usan hechizos.
 *       Los enemigos sin magia se construyen con pm = 0 y el sistema queda inactivo.</li>
 *   <li>Sistema de drops: al morir, puede soltar un objeto para el héroe.</li>
 * </ul>
 */
public abstract class Enemigo extends Personaje {

    // ── Variables ─────────────────────────────────────────────────────────────

    private int pm;
    private int pmMax;

    /** Generador compartido para el cálculo de probabilidad de drops. */
    protected static final Random RNG = new Random();

    // ── Constructores ─────────────────────────────────────────────────────────

    /**
     * Constructor sin magia (pm = 0). Para Ogro y Goblin.
     */
    protected Enemigo(String nombre, int puntosGolpe, int defensa, int poder) {
        this(nombre, puntosGolpe, defensa, poder, 0);
    }

    /**
     * Constructor con PM para enemigos mágicos (Saga, Dragón).
     *
     * @param pm PM máximos del enemigo (0 si no usa magia)
     */
    protected Enemigo(String nombre, int puntosGolpe, int defensa, int poder, int pm) {
        super(nombre, puntosGolpe, defensa, poder);
        this.pm    = pm;
        this.pmMax = pm;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /** @return PM actuales del enemigo */
    public int getPm()    { return pm; }

    /** @return PM máximos del enemigo (0 si no usa magia) */
    public int getPmMax() { return pmMax; }

    /**
     * Fija los PM actuales del enemigo, garantizando que estén entre 0 y pmMax.
     * Se usa al reanudar una partida guardada para restaurar el estado del enemigo.
     *
     * @param pm PM a restaurar (se clampea a [0, pmMax])
     */
    public void setPm(int pm) {
        this.pm = Math.max(0, Math.min(pmMax, pm));
    }

    /** @return {@code true} si este enemigo tiene sistema de magia activo (pmMax > 0) */
    public boolean tienePmMax() {
        return pmMax > 0;
    }

    /** @return porcentaje de PM actual respecto al máximo (0.0 – 1.0) */
    public double getPorcentajePm() {
        return pmMax == 0 ? 0.0 : (double) pm / pmMax;
    }

    /**
     * Ruta del recurso de imagen del enemigo.
     * El nombre del archivo coincide con el nombre de la clase en minúsculas
     * (p. ej. Goblin → /recursos/imagen/goblin.png).
     *
     * @return ruta del recurso de imagen
     */
    public String getRutaImagen() {
        return "/recursos/imagen/" + getClass().getSimpleName().toLowerCase() + ".png";
    }

    // ── Métodos ───────────────────────────────────────────────────────────────

    /**
     * Intenta gastar {@code coste} PM.
     *
     * @param coste puntos de magia a consumir
     * @return {@code true} si había suficientes PM y se han consumido;
     *         {@code false} si no había suficientes (sin cambios)
     */
    public boolean gastarPm(int coste) {
        if (pm < coste) { return false; }
        pm -= coste;
        return true;
    }

    /**
     * Genera el objeto que suelta este enemigo al morir.
     *
     * <p>La implementación por defecto devuelve {@code null} (sin drop).
     * Las subclases sobreescriben este método para definir sus propias
     * probabilidades y tipos de objeto.</p>
     *
     * @return el {@link TipoDrop} obtenido, o {@code null} si no cae nada
     */
    public TipoDrop generarDrop() {
        return null;
    }

    /**
     * El enemigo realiza su turno de ataque contra el héroe.
     * Por defecto es un ataque básico; las subclases pueden sobrescribir
     * para añadir comportamiento especial (p. ej. la Saga o el Dragón).
     *
     * @param objetivo el héroe que recibe el ataque
     * @return descripción del ataque realizado
     */
    public String realizarAtaque(Heroe objetivo) {
        int danio = objetivo.recibirAtaque(this);
        return String.format("%s %s  →  -%d HP  [%s: %d/%d HP]",
                getIcono(), getNombre(), danio,
                objetivo.getNombre(), objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax());
    }
}
