package modelo;

/**
 * Dragón – jefe final de la mazmorra (fase 4).
 * Estadísticas muy superiores al resto de enemigos.
 * Ataque especial: Aliento de Fuego (se activa cada 3 turnos, daño doble).
 *
 * Usa los stats de {@link EnemigoDatos} si se proporcionan;
 * en caso contrario cae a los valores hardcodeados originales.
 */
public class Dragon extends Enemigo {

    private int turnoActual;

    /** Constructor con stats cargados desde BD. */
    public Dragon(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
        this.turnoActual = 0;
    }

    /** Constructor por defecto (fallback si la BD no está disponible). */
    public Dragon() {
        super("Ignaroth, el Dragón Eterno", /*hp*/ 200, /*def*/ 18, /*poder*/ 28);
        this.turnoActual = 0;
    }

    @Override public String getTipo()  { return "DRAGON"; }
    @Override public String getIcono() { return "🐉"; }

    /**
     * Cada 3 turnos usa Aliento de Fuego (daño × 2, ignora defensa).
     * El resto de turnos realiza un ataque físico normal.
     */
    @Override
    public String realizarAtaque(Heroe objetivo) {
        turnoActual++;
        if (turnoActual % 3 == 0) {
            // Aliento de Fuego: daño directo sin contar defensa del héroe
            int danio = getPoder() * 2;
            objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
            return String.format(
                "🔥 ¡ALIENTO DE FUEGO! %s abrasa a %s causando %d puntos de daño devastador. (HP restante: %d)",
                getNombre(), objetivo.getNombre(), danio, objetivo.getPuntosGolpe());
        } else {
            int danio = objetivo.recibirAtaque(this);
            return String.format(
                "🐉 %s golpea a %s con su cola causando %d puntos de daño. (HP restante: %d)",
                getNombre(), objetivo.getNombre(), danio, objetivo.getPuntosGolpe());
        }
    }
}
