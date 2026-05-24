package modelo;

public class Saga extends Enemigo {

    private static final int PM_SAGA         = 20;
    private static final int COSTE_MALDICION = 8;

    private int turnoActual = 0;

    // ── Constructores ─────────────────────────────────────────────────────────

    //Constructor con stats cargados desde BD. 
    public Saga(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder(),
              PM_SAGA);
    }

    //Constructor por defecto (fallback si la BD no está disponible). 
    public Saga() {
        super("Saga Oscura", /*hp*/ 70, /*def*/ 6, /*poder*/ 22, PM_SAGA);
    }

    // ── Getters ────────────────────────────────────────────────────────

    @Override public String getTipo()  { return "SAGA"; }
    @Override public String getIcono() { return "🧟"; }

    // ── Métodos ─────────────────────────────────────────────────────────────────

    /**
     * La Saga tiene un 35 % de probabilidad de soltar una Poción Mágica.
     */
    @Override
    public TipoDrop generarDrop() {
        return RNG.nextInt(100) < 35 ? TipoDrop.POCION_MAGICA : null;
    }

    /**
     * Cada 2 turnos usa Maldición de Oscuridad (si tiene PM):
     * inflige poder × 1,5 de daño mágico puro, ignorando la defensa del héroe.
     * En el resto de turnos (o sin PM) realiza un ataque mágico básico
     * descontado por la defensa normal.
     */
    @Override
    public String realizarAtaque(Heroe objetivo) {
        turnoActual++; //contador para patrón de ataque

        if (turnoActual % 2 == 0 && gastarPm(COSTE_MALDICION)) {
            // ── Maldición de Oscuridad (ignora defensa)
            int danio = (int) Math.round(getPoder() * 1.5); // poder de la Saga multiplicado por 1.5, sin restar la defensa del objetivo
            objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
            return String.format(
                    "🌑 ¡MALDICIÓN DE OSCURIDAD!  →  -%d HP  [%s: %d/%d HP]  |  PM Saga: %d/%d",
                    danio, objetivo.getNombre(),
                    objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax(),
                    getPm(), getPmMax());
        } else {
            // ── Ataque básico (reducido por defensa)
            int danio = objetivo.recibirAtaque(this);
            return String.format(
                    "🧙🏻‍♀️ Saga lanza un hechizo  →  -%d HP  [%s: %d/%d HP]",
                    danio, objetivo.getNombre(),
                    objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax());
        }
    }
}
