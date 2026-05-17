package modelo;

/**
 * Superclase abstracta común a héroes y enemigos.
 *
 * <p>Almacena los cuatro atributos base de cualquier combatiente
 * (nombre, vida, defensa, poder) y contiene la lógica de combate
 * elemental: recibir daño, curarse y calcular el porcentaje de vida.</p>
 *
 * <p>Las subclases concretas ({@link Heroe}, {@link Enemigo}) añaden
 * comportamientos específicos como habilidades especiales o IA de ataque.</p>
 *
 * <h3>Fórmula de daño básica</h3>
 * <pre>
 *   daño = max(1, poder_del_atacante − defensa_del_receptor)
 * </pre>
 * El mínimo garantizado de 1 asegura que ningún ataque sea completamente
 * absorbido por la defensa.
 */
public abstract class Personaje {

    // ── Variables ─────────────────────────────────────────────────────────────

    private int    id;             // id en BD (0 si aún no persistido)
    private String nombre;         // nombre visible en pantalla
    private int    puntosGolpeMax; // vida máxima (no varía en combate)
    private int    puntosGolpe;    // vida actual (decrece al recibir daño)
    private int    defensa;        // reduce el daño de ataques físicos entrantes
    private int    poder;          // determina el daño de los ataques de este personaje

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Construye un personaje con sus atributos base.
     * La vida actual se inicializa igual a la vida máxima (personaje a plena salud).
     *
     * @param nombre      nombre del personaje
     * @param puntosGolpe vida máxima (y actual inicial)
     * @param defensa     valor de defensa base
     * @param poder       valor de ataque base
     */
    protected Personaje(String nombre, int puntosGolpe, int defensa, int poder) {
        this.nombre         = nombre;
        this.puntosGolpeMax = puntosGolpe;
        this.puntosGolpe    = puntosGolpe;
        this.defensa        = defensa;
        this.poder          = poder;
        this.id             = 0;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /** @return id en BD, o 0 si aún no ha sido persistido */
    public int    getId()               { return id; }
    /** @param id id asignado por la BD tras el INSERT */
    public void   setId(int id)         { this.id = id; }

    /** @return nombre del personaje */
    public String getNombre()           { return nombre; }
    /** @param nombre nuevo nombre */
    public void   setNombre(String nombre) { this.nombre = nombre; }

    /** @return puntos de golpe actuales (vida restante) */
    public int    getPuntosGolpe()      { return puntosGolpe; }
    /**
     * Fija los puntos de golpe actuales, garantizando que no sean negativos.
     *
     * @param v nuevo valor de HP (se clampea a 0 como mínimo)
     */
    public void   setPuntosGolpe(int v) { this.puntosGolpe = Math.max(0, v); }

    /** @return puntos de golpe máximos (vida total) */
    public int    getPuntosGolpeMax()   { return puntosGolpeMax; }

    /** @return valor de defensa actual */
    public int    getDefensa()          { return defensa; }
    /**
     * Actualiza la defensa del personaje.
     * Lo usan habilidades como Postura de Hierro (Guerrero) o Bendición Sagrada (Clérigo)
     * para aplicar o revertir bonificaciones temporales.
     *
     * @param d nuevo valor de defensa
     */
    public void   setDefensa(int d)     { this.defensa = d; }

    /** @return valor de poder (ataque) actual */
    public int    getPoder()            { return poder; }
    /** @param p nuevo valor de poder */
    public void   setPoder(int p)       { this.poder = p; }

    /**
     * Devuelve una descripción corta del tipo de personaje (p. ej. "MAGO", "DRAGON").
     * Se muestra en la UI junto al nombre.
     *
     * @return cadena con el tipo en mayúsculas
     */
    public abstract String getTipo();

    /**
     * Devuelve el icono emoji que representa visualmente al personaje en el log de combate.
     *
     * @return emoji representativo (p. ej. "🧙", "🐉")
     */
    public abstract String getIcono();

    // ── Métodos ───────────────────────────────────────────────────────────────

    /**
     * Aplica el daño de un ataque básico recibido de otro personaje.
     *
     * <p>El daño se calcula como {@code max(1, atacante.poder − this.defensa)},
     * garantizando al menos 1 punto de daño. Los puntos de golpe no bajan de 0.</p>
     *
     * @param atacante el personaje que realiza el ataque
     * @return daño efectivo infligido (≥ 1)
     */
    public int recibirAtaque(Personaje atacante) {
        int danio = Math.max(1, atacante.getPoder() - this.defensa);
        this.puntosGolpe = Math.max(0, this.puntosGolpe - danio);
        return danio;
    }

    /**
     * Aplica daño directo al personaje, ignorando su defensa.
     *
     * <p>A diferencia de {@link #recibirAtaque(Personaje)}, este método no
     * descuenta la defensa del receptor: el valor {@code danio} se resta
     * íntegramente de los puntos de golpe (con un mínimo de 0).</p>
     *
     * <p>Se usa para efectos que no son ataques convencionales, como la
     * penalización por huida fallida (golpe por la espalda).</p>
     *
     * @param danio puntos de daño a aplicar (valores ≤ 0 no tienen efecto)
     * @return daño efectivo infligido (≥ 0)
     */
    public int recibirDanio(int danio) {
        int efectivo = Math.max(0, danio);
        this.puntosGolpe = Math.max(0, this.puntosGolpe - efectivo);
        return efectivo;
    }

    /**
     * Comprueba si el personaje sigue con vida.
     *
     * @return {@code true} si los puntos de golpe actuales son mayores que 0
     */
    public boolean estaVivo() {
        return puntosGolpe > 0;
    }

    /**
     * Restaura los puntos de golpe actuales a su valor máximo.
     * Útil para reiniciar el estado entre combates o en pruebas.
     */
    public void restaurarVida() {
        this.puntosGolpe = this.puntosGolpeMax;
    }

    /**
     * Cura al personaje restaurando una cantidad fija de HP,
     * sin sobrepasar el máximo.
     *
     * <p>Ejemplo: si el héroe tiene 50/100 HP y se cura 30, queda en 80/100.</p>
     *
     * @param cantidad puntos de vida a recuperar (valores negativos no tienen efecto)
     */
    public void curar(int cantidad) {
        this.puntosGolpe = Math.min(puntosGolpeMax, puntosGolpe + cantidad);
    }

    /**
     * Calcula el porcentaje de vida restante como valor entre 0.0 y 1.0.
     * Se usa para actualizar las barras de progreso en la UI.
     *
     * @return fracción {@code puntosGolpe / puntosGolpeMax}, o 0 si el máximo es 0
     */
    public double getPorcentajeVida() {
        if (puntosGolpeMax == 0) { return 0; }
        return (double) puntosGolpe / puntosGolpeMax;
    }

    // ── ToString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("%s [%s] HP:%d/%d DEF:%d POD:%d",
                nombre, getTipo(), puntosGolpe, puntosGolpeMax, defensa, poder);
    }
}
