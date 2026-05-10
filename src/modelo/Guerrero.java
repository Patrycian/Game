package modelo;

/**
 * Guerrero – equilibrado entre ataque y defensa.
 * Habilidad especial: Golpe Devastador (ataque con poder × 1.5, redondeado).
 */
public class Guerrero extends Heroe {

    public Guerrero(String nombre) {
        super(nombre,
              /*hp*/    120,
              /*def*/    15,
              /*poder*/  18,
              "Golpe Devastador",
              "Un ataque brutal que inflige 1,5 veces tu poder menos la defensa del enemigo.");
    }

    @Override
    public String getTipo()  { return "GUERRERO"; }

    @Override
    public String getIcono() { return "⚔️"; }

    @Override
    public String getRutaImagen() { return "/recursos/imagen/guerrero.png"; }

    /**
     * Golpe Devastador: calcula daño con poder × 1.5, descontando defensa del objetivo.
     */
    @Override
    protected String aplicarHabilidad(Personaje objetivo) {
        int poderAmpliado = (int) Math.round(getPoder() * 1.5);
        int danio = Math.max(1, poderAmpliado - objetivo.getDefensa());
        objetivo.setPuntosGolpe(Math.max(0, objetivo.getPuntosGolpe() - danio));
        return String.format("⚔️  ¡Golpe Devastador! %s recibe %d puntos de daño brutal.",
                objetivo.getNombre(), danio);
    }
}
