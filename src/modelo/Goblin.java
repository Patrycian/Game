package modelo;

/**
 * Goblin – rápido y astuto, pero frágil.
 * Vida baja, defensa baja, poder moderado.
 *
 * Usa los stats de {@link EnemigoDatos} si se proporcionan;
 * en caso contrario cae a los valores hardcodeados originales.
 */
public class Goblin extends Enemigo {

    /** Constructor con stats cargados desde BD. */
    public Goblin(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
    }

    /** Constructor por defecto (fallback si la BD no está disponible). */
    public Goblin() {
        super("Goblin Astuto", /*hp*/ 60, /*def*/ 5, /*poder*/ 15);
    }

    @Override public String getTipo()  { return "GOBLIN"; }
    @Override public String getIcono() { return "👺"; }
}
