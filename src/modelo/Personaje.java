package modelo;

/**
 * Superclase abstracta común a héroes y enemigos.
 * Almacena los atributos base del personaje y la lógica
 * de combate (recibir daño, comprobar si está vivo).
 */
public abstract class Personaje {

    private int    id;          // id en BD (0 si aún no persistido)
    private String nombre;
    private int    puntosGolpeMax;   // vida máxima
    private int    puntosGolpe;      // vida actual
    private int    defensa;
    private int    poder;            // ataque / hechizo / fuerza bruta

    protected Personaje(String nombre, int puntosGolpe, int defensa, int poder) {
        this.nombre          = nombre;
        this.puntosGolpeMax  = puntosGolpe;
        this.puntosGolpe     = puntosGolpe;
        this.defensa         = defensa;
        this.poder           = poder;
        this.id              = 0;
    }

    // ── Lógica de combate ─────────────────────────────────────────────────────

    /**
     * Aplica el daño recibido de un ataque.
     * Daño efectivo = poder del atacante − defensa propia (mínimo 1).
     * Los puntos de golpe no bajan de 0.
     */
    public int recibirAtaque(Personaje atacante) {
        int danio = Math.max(1, atacante.getPoder() - this.defensa);
        this.puntosGolpe = Math.max(0, this.puntosGolpe - danio);
        return danio;
    }

    /** @return true si el personaje sigue con vida. */
    public boolean estaVivo() {
        return puntosGolpe > 0;
    }

    /** Restaura los puntos de golpe actuales al máximo. */
    public void restaurarVida() {
        this.puntosGolpe = this.puntosGolpeMax;
    }

    /**
     * Cura una cantidad fija de HP sin superar el máximo.
     * @param cantidad puntos a recuperar
     */
    public void curar(int cantidad) {
        this.puntosGolpe = Math.min(puntosGolpeMax, puntosGolpe + cantidad);
    }

    /** Descripción corta del tipo de personaje para mostrar en pantalla. */
    public abstract String getTipo();

    /** Icono emoji representativo del personaje. */
    public abstract String getIcono();

    // ── Getters y setters ─────────────────────────────────────────────────────

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public String getNombre()          { return nombre; }
    public void   setNombre(String n)  { this.nombre = n; }

    public int    getPuntosGolpe()     { return puntosGolpe; }
    public void   setPuntosGolpe(int v){ this.puntosGolpe = Math.max(0, v); }

    public int    getPuntosGolpeMax()  { return puntosGolpeMax; }

    public int    getDefensa()         { return defensa; }
    public void   setDefensa(int d)    { this.defensa = d; }

    public int    getPoder()           { return poder; }
    public void   setPoder(int p)      { this.poder = p; }

    /** Porcentaje de vida actual (0.0 – 1.0). */
    public double getPorcentajeVida() {
        if (puntosGolpeMax == 0) return 0;
        return (double) puntosGolpe / puntosGolpeMax;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] HP:%d/%d DEF:%d POD:%d",
                nombre, getTipo(), puntosGolpe, puntosGolpeMax, defensa, poder);
    }
}
