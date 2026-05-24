package modelo;

public class Goblin extends Enemigo {

	// ── Constructor ───────────────────────────────────────────────────────────────
	
	//constructor con datos de la BD
	
    public Goblin(EnemigoDatos datos) {
        super(datos.getNombre(), datos.getPuntosGolpe(), datos.getDefensa(), datos.getPoder());
    }

    // constructor por defecto con valores hardcoreados. Fallback si BD no disponible
    
    public Goblin() {
        super("Goblin Astuto", /*hp*/ 60, /*def*/ 5, /*poder*/ 15);
    }
    
    // ── Getters ───────────────────────────────────────────────────────────────

    /** @return "GOBLIN" */
    @Override public String getTipo()  { return "GOBLIN"; }

    /** @return emoji 👺 */
    @Override public String getIcono() { return "👺"; }
    
    // ── Método ───────────────────────────────────────────────────────────────

    // El Goblin usa el ataque físico básico heredado de Enemigo. No sobreescribe realizarAtaque().

    /**
     * El Goblin tiene un 40 % de probabilidad de soltar una Poción de Curación.
     */
    @Override
    public TipoDrop generarDrop() {
        return RNG.nextInt(100) < 40 ? TipoDrop.POCION_VIDA : null;
    }
}
