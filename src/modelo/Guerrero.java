package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Guerrero – equilibrado entre ataque y defensa.
 *
 * <p>Dispone de una <b>barra de Energía</b> (EN) que representa la resistencia
 * física del combatiente. Cada habilidad del submenú tiene un coste en EN;
 * al final de cada turno el guerrero recupera automáticamente una parte.</p>
 *
 * <h3>Habilidades disponibles en combate:</h3>
 * <ul>
 *   <li><b>Golpe Devastador</b> (habilidad especial): ataque con poder × 1,5.
 *       Coste: {@value #COSTE_GOLPE_DEVASTADOR} EN.</li>
 *   <li><b>Postura de Hierro</b>: aumenta la defensa en
 *       {@value #BONUS_DEFENSA_POSTURA} puntos durante el resto del combate.
 *       Usable una vez por fase. Coste: {@value #COSTE_POSTURA_HIERRO} EN.</li>
 * </ul>
 *
 * <h3>Sistema de Energía:</h3>
 * <ul>
 *   <li>Máximo: {@value #ENERGIA_MAX} EN.</li>
 *   <li>Se inicia a tope al comenzar cada fase ({@link #reiniciarHabilidad()}).</li>
 *   <li>Regenera {@value #REGEN_POR_TURNO} EN al final de cada turno (incluyendo
 *       turnos en los que el héroe usa pociones o buffs).</li>
 * </ul>
 */
public class Guerrero extends Heroe {

    // ── Postura de Hierro ─────────────────────────────────────────────────────
    private static final int BONUS_DEFENSA_POSTURA = 8;
    private boolean posturaDeHierroActiva = false;

    // ── Sistema de Energía ────────────────────────────────────────────────────
    /** Energía máxima del guerrero. */
    public static final int ENERGIA_MAX = 50;

    /** Coste en EN del Golpe Devastador. */
    public static final int COSTE_GOLPE_DEVASTADOR = 20;

    /** Coste en EN de la Postura de Hierro. */
    public static final int COSTE_POSTURA_HIERRO = 15;

    /** Energía recuperada automáticamente al final de cada turno. */
    public static final int REGEN_POR_TURNO = 8;

    private int energia = ENERGIA_MAX;

    // ── Catálogo de habilidades ───────────────────────────────────────────────
    private static final List<String[]> HABILIDADES = Collections.unmodifiableList(Arrays.asList(
            new String[] { "Golpe Devastador",
                    "Ataque brutal que inflige 1,5× tu poder menos la defensa enemiga.  Coste: "
                    + COSTE_GOLPE_DEVASTADOR + " EN" },
            new String[] { "Postura de Hierro",
                    "Adoptas una postura defensiva: +8 DEF durante el resto del combate. Usable una vez por fase.  Coste: "
                    + COSTE_POSTURA_HIERRO + " EN" }));

    // ── Constructor ───────────────────────────────────────────────────────────

    public Guerrero(String nombre) {
        super(nombre, /* hp */ 120, /* def */ 15, /* poder */ 18, "Golpe Devastador",
                "Ataque brutal que inflige 1,5× tu poder menos la defensa enemiga.  Coste: "
                + COSTE_GOLPE_DEVASTADOR + " EN");
    }

    // ── Identificación ────────────────────────────────────────────────────────

    @Override public String getTipo()       { return "GUERRERO"; }
    @Override public String getIcono()      { return "⚔️"; }
    @Override public String getRutaImagen() { return "/recursos/imagen/guerrero.png"; }

    // ── Habilidad especial (Golpe Devastador) ─────────────────────────────────

    /**
     * Golpe Devastador: calcula daño con poder × 1,5, descontando defensa del objetivo.
     * El gasto de Energía lo realiza el controlador ANTES de llamar a este método.
     */
    @Override
    protected String aplicarHabilidad(Personaje objetivo) {
        int poderAmpliado = (int) Math.round(getPoder() * 1.5);
        int danio = Math.max(1, poderAmpliado - objetivo.getDefensa());
        objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
        return String.format("⚔️  ¡GOLPE DEVASTADOR! %s recibe %d de daño brutal.  [%s: %d/%d HP]",
                objetivo.getNombre(), danio,
                objetivo.getNombre(), objetivo.getPuntosGolpe(), objetivo.getPuntosGolpeMax());
    }

    // ── Habilidad adicional: Postura de Hierro ────────────────────────────────

    /** @return {@code true} si la Postura de Hierro ya está activa este combate. */
    public boolean isPosturaDeHierroActiva() { return posturaDeHierroActiva; }

    /**
     * Activa la Postura de Hierro: incrementa la defensa del guerrero en
     * {@value #BONUS_DEFENSA_POSTURA} puntos permanentemente durante la fase.
     * El gasto de Energía lo realiza el controlador ANTES de llamar a este método.
     *
     * @return mensaje descriptivo del efecto, o {@code null} si ya estaba activa
     */
    public String usarPosturaDeHierro() {
        if (posturaDeHierroActiva) return null;
        posturaDeHierroActiva = true;
        setDefensa(getDefensa() + BONUS_DEFENSA_POSTURA);
        return String.format("🛡️  ¡%s adopta la Postura de Hierro!  +%d DEF  [DEF total: %d]",
                getNombre(), BONUS_DEFENSA_POSTURA, getDefensa());
    }

    // ── Sistema de Energía ────────────────────────────────────────────────────

    /** @return energía actual del guerrero (0..{@value #ENERGIA_MAX}). */
    public int getEnergia() { return energia; }

    /** @return energía máxima ({@value #ENERGIA_MAX}). */
    public int getEnergiaMax() { return ENERGIA_MAX; }

    /**
     * Intenta gastar {@code coste} puntos de Energía.
     *
     * @param coste puntos de EN a consumir
     * @return {@code true} si había suficiente EN y se descontó; {@code false} si no
     */
    public boolean gastarEnergia(int coste) {
        if (energia < coste) return false;
        energia = Math.max(0, energia - coste);
        return true;
    }

    /**
     * Regenera {@value #REGEN_POR_TURNO} puntos de Energía al final de cada turno,
     * sin sobrepasar {@value #ENERGIA_MAX}.
     */
    public void regenerarEnergia() {
        energia = Math.min(ENERGIA_MAX, energia + REGEN_POR_TURNO);
    }

    /**
     * Fracción de Energía restante (0.0 – 1.0), para alimentar la barra de progreso.
     *
     * @return {@code energia / ENERGIA_MAX}
     */
    public double getPorcentajeEnergia() {
        return (double) energia / ENERGIA_MAX;
    }

    // ── Reinicio entre fases ──────────────────────────────────────────────────

    /**
     * Al inicio de cada nueva fase revierte el bonus de defensa y restaura
     * la Energía al máximo para que el guerrero empiece fresco.
     */
    @Override
    public void reiniciarHabilidad() {
        if (posturaDeHierroActiva) {
            setDefensa(getDefensa() - BONUS_DEFENSA_POSTURA);
            posturaDeHierroActiva = false;
        }
        energia = ENERGIA_MAX;
    }

    // ── Catálogo ──────────────────────────────────────────────────────────────

    /**
     * @return lista inmutable de habilidades del guerrero; cada elemento es un
     *         array {@code String[2]} donde [0] = nombre y [1] = descripción.
     */
    public List<String[]> getHabilidadesGuerrero() { return HABILIDADES; }
}
