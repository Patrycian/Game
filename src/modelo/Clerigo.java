package modelo;

/**
 * Clérigo – soporte y aguante. Defensa media, poder bajo.
 * Habilidad especial: Curación Divina (recupera el 40 % de su vida máxima).
 * <p>
 * Extiende {@link Magico}: tiene 20 PM y conoce dos habilidades mágicas.
 */
public class Clerigo extends Magico {

    // ── Bendición Sagrada ────────────────────────────────────────────────────
    private static final int BONUS_DEF    = 8;
    private static final int COSTE_BENDICION = 7;
    private boolean bendicionActiva = false;

    public Clerigo(String nombre) {
        super(nombre,
              /*hp*/    100,
              /*def*/    12,
              /*poder*/  14,
              /*pm*/     20,
              "Curación Divina",
              "Invoca la gracia divina para recuperar el 40 % de tu vida máxima.");

        agregarHabilidadMagica("Curación Divina",
                "Recupera el 40 % de la vida máxima del clérigo mediante la gracia divina.");
        agregarHabilidadMagica("Bendición Sagrada",
                "Invoca un aura divina que aumenta temporalmente la defensa del clérigo.");
    }

    // ── Reinicio de habilidades (inicio de cada fase) ────────────────────────

    /**
     * Además de reiniciar la habilidad especial, revierte el bonus de defensa
     * de la Bendición Sagrada si estaba activa, para no acumular bonuses entre fases.
     */
    @Override
    public void reiniciarHabilidad() {
        super.reiniciarHabilidad();
        desactivarBendicion();
    }

    private void desactivarBendicion() {
        if (bendicionActiva) {
            bendicionActiva = false;
            setDefensa(getDefensa() - BONUS_DEF);
        }
    }

    // ── Habilidades mágicas adicionales ──────────────────────────────────────

    @Override
    public String ejecutarHabilidadAdicional(String nombre, Personaje objetivo) {
        if ("Bendición Sagrada".equals(nombre)) {
            if (bendicionActiva) {
                return "⚠ La Bendición Sagrada ya está activa. (DEF: " + getDefensa() + ")";
            }
            bendicionActiva = true;
            setDefensa(getDefensa() + BONUS_DEF);
            return String.format("✝️ ¡Bendición Sagrada! %s invoca un aura divina."
                    + " Defensa aumentada en %d. (DEF: %d)", getNombre(), BONUS_DEF, getDefensa());
        }
        return null;
    }

    @Override
    public int getCostePmHabilidad(String nombre) {
        return "Bendición Sagrada".equals(nombre) ? COSTE_BENDICION : 0;
    }

    @Override
    public boolean isHabilidadAdicionalActiva(String nombre) {
        return "Bendición Sagrada".equals(nombre) && bendicionActiva;
    }

    // ── Tipo, icono e imagen ──────────────────────────────────────────────────

    @Override
    public String getTipo()  { return "CLERIGO"; }

    @Override
    public String getIcono() { return "✝️"; }

    @Override
    public String getRutaImagen() { return "/recursos/imagen/clerigo.png"; }

    // ── Habilidad especial ────────────────────────────────────────────────────

    /**
     * Curación Divina: el Clérigo se cura a sí mismo (el objetivo en este caso
     * siempre es él mismo; se pasa desde el controlador).
     */
    @Override
    protected String aplicarHabilidad(Personaje objetivo) {
        int curacion = (int) Math.round(getPuntosGolpeMax() * 0.4);
        curar(curacion);   // se cura a sí mismo
        return String.format("✨ ¡Curación Divina! %s recupera %d puntos de vida. (HP: %d/%d)",
                getNombre(), curacion, getPuntosGolpe(), getPuntosGolpeMax());
    }
}
