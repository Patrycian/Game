package modelo;

import java.util.Random;

public abstract class Enemigo extends Personaje {

    private int pm;
    private int pmMax;
    protected static final Random RNG = new Random(); //generador cálculo probabilidad de drops

    // ── Constructores ─────────────────────────────────────────────────────────

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

    // ── Getters/Setters ─────────────────────────────────────────────────────

    public int getPm()    { return pm; }

    public int getPmMax() { return pmMax; }

    public void setPm(int pm) {
        this.pm = Math.max(0, Math.min(pmMax, pm));
    }

    public boolean tienePmMax() {
        return pmMax > 0;
    }

    public double getPorcentajePm() {
        return pmMax == 0 ? 0.0 : (double) pm / pmMax;
    }

    public String getRutaImagen() {
        return "/recursos/imagen/" + getClass().getSimpleName().toLowerCase() + ".png";
    }

    // ── Métodos ───────────────────────────────────────────────────────────────

    public boolean gastarPm(int coste) {
        if (pm < coste) { return false; }
        pm -= coste;
        return true;
    }

    /**
     * Genera el objeto que suelta el enemigo al morir.
     */
    public TipoDrop generarDrop() {
        return null;
    }

    /**
     * El enemigo realiza su turno de ataque contra el héroe.
     * Por defecto es un ataque básico.
     */
    public String realizarAtaque(Heroe objetivo) { //recibe al héroe como parámetro
        int danio = objetivo.recibirAtaque(this); //this pasa el propio enemigo como argumento
        return String.format("%s %s  →  -%d HP  [%s: %d/%d HP]",
                getIcono(), getNombre(), danio,
                objetivo.getNombre(), objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax());
    }
}
