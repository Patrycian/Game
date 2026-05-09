package modelo;

/**
 * POJO inmutable con los datos base de un tipo de enemigo,
 * tal como se cargan desde la tabla {@code enemigos} de la BD.
 *
 * Se usa como puente entre {@code dao.EnemigoDAO} y las clases
 * concretas de enemigo ({@code Ogro}, {@code Goblin}, etc.).
 */
public class EnemigoDatos {

    private final String  tipo;
    private final String  nombre;
    private final int     puntosGolpe;
    private final int     defensa;
    private final int     poder;
    private final String  icono;
    private final boolean esJefe;

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

    public String  getTipo()        { return tipo; }
    public String  getNombre()      { return nombre; }
    public int     getPuntosGolpe() { return puntosGolpe; }
    public int     getDefensa()     { return defensa; }
    public int     getPoder()       { return poder; }
    public String  getIcono()       { return icono; }
    public boolean isEsJefe()       { return esJefe; }

    @Override
    public String toString() {
        return String.format("EnemigoDatos[%s] hp:%d def:%d pod:%d %s",
                tipo, puntosGolpe, defensa, poder, icono);
    }
}
