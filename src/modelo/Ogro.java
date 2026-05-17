package modelo;

/**
 * Ogro – enemigo tanque de la mazmorra.
 *
 * <p>El Ogro es el enemigo más resistente de las fases normales (1-3).
 * Su punto fuerte es la combinación de HP alto y defensa elevada, aunque
 * su poder de ataque es moderado. No dispone de habilidades especiales ni
 * de sistema de magia: todos sus turnos son ataques físicos básicos
 * (heredados de {@link Enemigo#realizarAtaque}).</p>
 *
 * <p>Usa los stats de {@link EnemigoDatos} si se proporcionan desde BD;
 * en caso contrario cae a los valores hardcodeados del constructor por defecto.</p>
 *
 * <h3>Stats por defecto</h3>
 * <ul>
 *   <li>HP: 100</li>
 *   <li>DEF: 12</li>
 *   <li>POD: 20</li>
 * </ul>
 */
public class Ogro extends Enemigo {

    /** Constructor con stats cargados desde BD.*/
    public Ogro(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
    }

    /**
     * Constructor por defecto con valores hardcodeados.
     * Se usa como fallback si la BD no está disponible.
     */
    public Ogro() {
        super("Ogro Brutal", /*hp*/ 100, /*def*/ 12, /*poder*/ 20);
    }

    /** @return "OGRO" */
    @Override public String getTipo()  { return "OGRO"; }

    /** @return emoji 👹 */
    @Override public String getIcono() { return "👹"; }

    // El Ogro usa el ataque físico básico heredado de Enemigo:
    //   daño = max(1, poder − defensa_del_héroe)
    // No sobreescribe realizarAtaque().

    /**
     * El Ogro tiene un 30 % de probabilidad de soltar una Poción de Curación.
     * Su corpulencia a veces esconde provisiones entre sus pieles.
     */
    @Override
    public TipoDrop generarDrop() {
        return RNG.nextInt(100) < 30 ? TipoDrop.POCION_VIDA : null;
    }
}
