package modelo;

/**
 * Clérigo – soporte y aguante. Defensa media, poder bajo.
 * Habilidad especial: Curación Divina (recupera el 40 % de su vida máxima).
 * <p>
 * Extiende {@link Magico}: tiene 20 PM y conoce dos habilidades mágicas.
 */
public class Clerigo extends Magico {

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

    @Override
    public String getTipo()  { return "CLERIGO"; }

    @Override
    public String getIcono() { return "✝️"; }

    @Override
    public String getRutaImagen() { return "/recursos/imagen/clerigo.png"; }

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
