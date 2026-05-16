package modelo;

/**
 * POJO inmutable con los datos base de un tipo de enemigo,
 * tal como se cargan desde la tabla {@code enemigos} de la BD.
 *
 * <p>Actúa como puente entre {@link dao.EnemigoDAO} y las clases concretas
 * de enemigo ({@link Ogro}, {@link Goblin}, {@link Saga}, {@link Dragon}).
 * Permite que los constructores de estas clases acepten datos dinámicos
 * de la BD en lugar de depender exclusivamente de valores hardcodeados.</p>
 *
 * <p>Si la BD no está disponible, {@code EnemigoDAO} devuelve un catálogo vacío
 * y los enemigos se instancian con sus constructores por defecto.</p>
 */
public class EnemigoDatos {

    private final String  tipo;         // Clave del catálogo: "OGRO", "GOBLIN", "SAGA", "DRAGON"
    private final String  nombre;       // Nombre visible en pantalla
    private final int     puntosGolpe;  // HP máximos del enemigo
    private final int     defensa;      // Defensa base
    private final int     poder;        // Poder de ataque base
    private final String  icono;        // Emoji identificativo (puede usarse como sprite)
    private final boolean esJefe;       // true si es el jefe final (Dragón)

    /**
     * Constructor completo. Todos los campos son inmutables tras la construcción.
     *
     * @param tipo        clave de tipo en mayúsculas (p. ej. "DRAGON")
     * @param nombre      nombre visible del enemigo
     * @param puntosGolpe HP máximos
     * @param defensa     defensa base
     * @param poder       poder de ataque base
     * @param icono       emoji representativo
     * @param esJefe      {@code true} si es el jefe final de la mazmorra
     */
    public EnemigoDatos(String tipo, String nombre,
                        int puntosGolpe, int defensa, int poder,
                        String icono, boolean esJefe) {
        this.tipo        = tipo;
        this.nombre      = nombre;
        this.puntosGolpe = puntosGolpe;
        this.defensa     = defensa;
        this.poder       = poder;
        this.icono       = icono;
        this.esJefe      = esJefe;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /** @return clave de tipo en mayúsculas (p. ej. "SAGA") */
    public String  getTipo()        { return tipo; }

    /** @return nombre visible del enemigo (p. ej. "Saga Oscura") */
    public String  getNombre()      { return nombre; }

    /** @return HP máximos cargados desde BD */
    public int     getPuntosGolpe() { return puntosGolpe; }

    /** @return defensa base cargada desde BD */
    public int     getDefensa()     { return defensa; }

    /** @return poder de ataque base cargado desde BD */
    public int     getPoder()       { return poder; }

    /** @return emoji identificativo del enemigo */
    public String  getIcono()       { return icono; }

    /** @return {@code true} si este enemigo es el jefe final de la mazmorra */
    public boolean isEsJefe()       { return esJefe; }

    @Override
    public String toString() {
        return String.format("EnemigoDatos[%s] hp:%d def:%d pod:%d %s",
                tipo, puntosGolpe, defensa, poder, icono);
    }
}
