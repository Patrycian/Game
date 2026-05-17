package modelo;

/**
 * Dragón – jefe final de la mazmorra (fase 4).
 * Estadísticas muy superiores al resto de enemigos.
 * <p>
 * Dispone de <b>40 PM</b> para su ataque especial:
 * <ul>
 *   <li>Cada 3 turnos, si tiene PM suficientes, usa
 *       <b>Aliento de Fuego</b>: daño = poder × 2, ignorando la defensa del héroe
 *       (coste: {@value #COSTE_ALIENTO} PM).</li>
 *   <li>Si no tiene PM en ese turno, o en los turnos intermedios,
 *       realiza un golpe físico normal con su cola.</li>
 * </ul>
 *
 * Usa los stats de {@link EnemigoDatos} si se proporcionan;
 * en caso contrario cae a los valores hardcodeados originales.
 */
public class Dragon extends Enemigo {

    private static final int PM_DRAGON     = 40;
    private static final int COSTE_ALIENTO = 15;

    private int turnoActual;

    // ── Constructor ─────────────────────────────────────────────────────────

    /** Constructor con stats cargados desde BD. */
    public Dragon(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder(),
              PM_DRAGON);
        this.turnoActual = 0;
    }

    /** Constructor por defecto (fallback si la BD no está disponible). */
    public Dragon() {
        super("Ignaroth, el Dragón Eterno", /*hp*/ 200, /*def*/ 18, /*poder*/ 28, PM_DRAGON);
        this.turnoActual = 0;
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    @Override public String getTipo()  { return "DRAGON"; }
    @Override public String getIcono() { return "🐉"; }

    // ── Métodos ─────────────────────────────────────────────────────────

    /**
     * Cada 3 turnos intenta usar <b>Aliento de Fuego</b> (coste {@value #COSTE_ALIENTO} PM):
     * daño = poder × 2, ignorando la defensa del héroe.
     * Si no tiene PM suficientes en ese turno, o en los turnos intermedios,
     * realiza un golpe físico con su cola (daño descontado por defensa).
     */
    @Override
    public String realizarAtaque(Heroe objetivo) {
        turnoActual++;

        if (turnoActual % 3 == 0 && gastarPm(COSTE_ALIENTO)) {
            // ── Aliento de Fuego: daño directo, ignora defensa
            int danio = getPoder() * 2;
            objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
            return String.format(
                    "🔥 ¡ALIENTO DE FUEGO!  →  -%d HP  [%s: %d/%d HP]  |  PM Dragón: %d/%d",
                    danio, objetivo.getNombre(),
                    objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax(),
                    getPm(), getPmMax());
        } else {
            // ── Golpe de cola: ataque físico normal
            int danio = objetivo.recibirAtaque(this);
            return String.format(
                    "🐉 %s golpea con su cola  →  -%d HP  [%s: %d/%d HP]",
                    getNombre(), danio,
                    objetivo.getNombre(), objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax());
        }
    }
}
