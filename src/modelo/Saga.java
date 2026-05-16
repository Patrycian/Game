package modelo;

/**
 * Saga Oscura – hechicera anciana.
 * <p>
 * Bajo HP y defensa, pero poder mágico alto.
 * Dispone de <b>20 PM</b> que gasta en su hechizo especial:
 * <ul>
 *   <li>Cada 2 turnos, si tiene PM suficientes, lanza
 *       <b>Maldición de Oscuridad</b>: daño = poder × 1,5 ignorando la defensa del héroe
 *       (coste: {@value #COSTE_MALDICION} PM).</li>
 *   <li>El resto de turnos (o cuando no tiene PM) realiza un ataque mágico básico
 *       que sí está reducido por la defensa del héroe.</li>
 * </ul>
 *
 * Usa los stats de {@link EnemigoDatos} si se proporcionan;
 * en caso contrario cae a los valores hardcodeados originales.
 */
public class Saga extends Enemigo {

    private static final int PM_SAGA         = 20;
    private static final int COSTE_MALDICION = 8;

    private int turnoActual = 0;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** Constructor con stats cargados desde BD. */
    public Saga(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder(),
              PM_SAGA);
    }

    /** Constructor por defecto (fallback si la BD no está disponible). */
    public Saga() {
        super("Saga Oscura", /*hp*/ 70, /*def*/ 6, /*poder*/ 22, PM_SAGA);
    }

    // ── Identificación ────────────────────────────────────────────────────────

    @Override public String getTipo()  { return "SAGA"; }
    @Override public String getIcono() { return "🧟"; }

    // ── IA de combate ─────────────────────────────────────────────────────────

    /**
     * Cada 2 turnos usa <b>Maldición de Oscuridad</b> (si tiene PM):
     * inflige poder × 1,5 de daño mágico puro, ignorando la defensa del héroe.
     * En el resto de turnos (o sin PM) realiza un ataque mágico básico
     * descontado por la defensa normal.
     */
    @Override
    public String realizarAtaque(Heroe objetivo) {
        turnoActual++;

        if (turnoActual % 2 == 0 && gastarPm(COSTE_MALDICION)) {
            // ── Maldición de Oscuridad: daño mágico puro (ignora defensa)
            int danio = (int) Math.round(getPoder() * 1.5);
            objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
            return String.format(
                    "🌑 ¡MALDICIÓN DE OSCURIDAD!  →  -%d HP  [%s: %d/%d HP]  |  PM Saga: %d/%d",
                    danio, objetivo.getNombre(),
                    objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax(),
                    getPm(), getPmMax());
        } else {
            // ── Ataque mágico básico (reducido por defensa)
            int danio = objetivo.recibirAtaque(this);
            return String.format(
                    "🧟 Saga lanza un hechizo  →  -%d HP  [%s: %d/%d HP]",
                    danio, objetivo.getNombre(),
                    objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax());
        }
    }
}
