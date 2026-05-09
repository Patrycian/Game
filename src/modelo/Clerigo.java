package modelo;

/**
 * Clérigo – soporte y aguante. Defensa media, poder bajo.
 * Habilidad especial: Curación Divina (recupera el 40 % de su vida máxima).
 */
public class Clerigo extends Heroe {

    public Clerigo(String nombre) {
        super(nombre,
              /*hp*/    100,
              /*def*/    12,
              /*poder*/  14,
              "Curación Divina",
              "Invoca la gracia divina para recuperar el 40 % de tu vida máxima.");
    }

    @Override
    public String getTipo()  { return "CLERIGO"; }

    @Override
    public String getIcono() { return "✝️"; }

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
