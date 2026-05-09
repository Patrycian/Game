package modelo;

/**
 * Superclase abstracta de los enemigos de la mazmorra.
 * Extiende Personaje y añade el comportamiento de ataque automático (IA básica).
 */
public abstract class Enemigo extends Personaje {

    protected Enemigo(String nombre, int puntosGolpe, int defensa, int poder) {
        super(nombre, puntosGolpe, defensa, poder);
    }

    /**
     * El enemigo realiza su turno de ataque contra el héroe.
     * Por defecto es un ataque básico; las subclases pueden sobrescribir
     * para añadir comportamiento especial (p. ej. el Dragón).
     *
     * @param objetivo el héroe que recibe el ataque
     * @return descripción del ataque realizado
     */
    public String realizarAtaque(Heroe objetivo) {
        int danio = objetivo.recibirAtaque(this);
        return String.format("%s %s ataca a %s y causa %d puntos de daño. (HP restante: %d)",
                getIcono(), getNombre(), objetivo.getNombre(), danio, objetivo.getPuntosGolpe());
    }
}
