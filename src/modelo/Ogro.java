package modelo;

/**
 * Ogro – fuerza bruta, mucha vida, defensa alta.
 * Sin habilidades especiales: es lento pero resistente.
 *
 * Usa los stats de {@link EnemigoDatos} si se proporcionan;
 * en caso contrario cae a los valores hardcodeados originales.
 */
public class Ogro extends Enemigo {

    /** Constructor con stats cargados desde BD. */
    public Ogro(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
    }

    /** Constructor por defecto (fallback si la BD no está disponible). */
    public Ogro() {
        super("Ogro Brutal", /*hp*/ 100, /*def*/ 12, /*poder*/ 20);
    }

    @Override public String getTipo()  { return "OGRO"; }
    @Override public String getIcono() { return "👹"; }
}
