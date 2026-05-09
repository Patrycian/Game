package modelo;

/**
 * Saga – hechicera anciana. Bajo HP y defensa, pero ataque mágico alto.
 * Contrapartida enemiga del Mago héroe.
 *
 * Usa los stats de {@link EnemigoDatos} si se proporcionan;
 * en caso contrario cae a los valores hardcodeados originales.
 */
public class Saga extends Enemigo {

    /** Constructor con stats cargados desde BD. */
    public Saga(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
    }

    /** Constructor por defecto (fallback si la BD no está disponible). */
    public Saga() {
        super("Saga Oscura", /*hp*/ 70, /*def*/ 6, /*poder*/ 22);
    }

    @Override public String getTipo()  { return "SAGA"; }
    @Override public String getIcono() { return "🧟"; }

    /** La Saga lanza un hechizo: el texto de ataque refleja la magia. */
    @Override
    public String realizarAtaque(Heroe objetivo) {
        int danio = objetivo.recibirAtaque(this);
        return String.format("🧟 La Saga Oscura lanza un hechizo sobre %s causando %d puntos de daño mágico. (HP restante: %d)",
                objetivo.getNombre(), danio, objetivo.getPuntosGolpe());
    }
}
