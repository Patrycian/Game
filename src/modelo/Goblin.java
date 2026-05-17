package modelo;

/**
 * Goblin – enemigo ágil y frágil de la mazmorra.
 *
 * <p>El Goblin es el enemigo más débil de las fases normales (1-3).
 * Tiene poca vida y defensa baja, pero su poder de ataque es relevante
 * para ser un enemigo de inicio. No dispone de habilidades especiales ni
 * de sistema de magia: todos sus turnos son ataques físicos básicos
 * (heredados de {@link Enemigo#realizarAtaque}).</p>
 *
 * <p>Usa los stats de {@link EnemigoDatos} si se proporcionan desde BD;
 * en caso contrario cae a los valores hardcodeados del constructor por defecto.</p>
 *
 * <h3>Stats por defecto</h3>
 * <ul>
 *   <li>HP: 60</li>
 *   <li>DEF: 5</li>
 *   <li>POD: 15</li>
 * </ul>
 */
public class Goblin extends Enemigo {

    /**
     * Constructor con stats cargados desde BD.
     *
     * @param datos datos del catálogo de enemigos obtenidos de {@link dao.EnemigoDAO}
     */
    public Goblin(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
    }

    /**
     * Constructor por defecto con valores hardcodeados.
     * Se usa como fallback si la BD no está disponible.
     */
    public Goblin() {
        super("Goblin Astuto", /*hp*/ 60, /*def*/ 5, /*poder*/ 15);
    }

    /** @return "GOBLIN" */
    @Override public String getTipo()  { return "GOBLIN"; }

    /** @return emoji 👺 */
    @Override public String getIcono() { return "👺"; }

    // El Goblin usa el ataque físico básico heredado de Enemigo:
    //   daño = max(1, poder − defensa_del_héroe)
    // No sobreescribe realizarAtaque().

    /**
     * El Goblin tiene un 40 % de probabilidad de soltar una Poción de Curación.
     * Los goblins rapiñan suministros de los aventureros caídos, de ahí sus botines.
     */
    @Override
    public TipoDrop generarDrop() {
        return RNG.nextInt(100) < 40 ? TipoDrop.POCION_VIDA : null;
    }
}
