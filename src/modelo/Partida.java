package modelo;

public class Partida {

	/**
	 * Estados posibles de una partida.
	 */
	public enum Estado {
		EN_CURSO, COMPLETADA, DERROTA
	}

	private int id; // id auto-generado por BD (0 si es nueva)
	private int idJugador; // FK → jugadores.id
	private int idPersonaje; // FK → personajes.id
	private int faseActual; // Fase en la que se guardó (1-4)
	private int hpActual; // HP del héroe en el momento del guardado
	private int pmActual; // PM del héroe mágico al guardar (0 si no es mágico)
	private Estado estado; // Estado actual de la partida
	private String tipoEnemigo; // Tipo del enemigo activo al guardar (null si no aplica)
	private int hpEnemigo; // HP del enemigo al guardar (0 si fue derrotado o nueva fase)
	private int pmEnemigo; // PM del enemigo al guardar (0 si no usa magia o fue derrotado)

	// ── Constructores ─────────────────────────────────────────────────────────

	/* Constructor para crear una nueva partida antes de persistirla en BD */

	public Partida(int idJugador, int idPersonaje, int faseActual, int hpActual) {
		this.id = 0;
		this.idJugador = idJugador;
		this.idPersonaje = idPersonaje;
		this.faseActual = faseActual;
		this.hpActual = hpActual;
		this.pmActual = 0;
		this.estado = Estado.EN_CURSO;
		this.tipoEnemigo = null;
		this.hpEnemigo = 0;
		this.pmEnemigo = 0;
	}

	/* Constructor para reconstruir una partida existente cargada desde la BD */

	public Partida(int id, int idJugador, int idPersonaje, int faseActual, int hpActual, int pmActual, Estado estado,
			String tipoEnemigo, int hpEnemigo, int pmEnemigo) {
		this.id = id;
		this.idJugador = idJugador;
		this.idPersonaje = idPersonaje;
		this.faseActual = faseActual;
		this.hpActual = hpActual;
		this.pmActual = pmActual;
		this.estado = estado;
		this.tipoEnemigo = tipoEnemigo;
		this.hpEnemigo = hpEnemigo;
		this.pmEnemigo = pmEnemigo;
	}

	// ── Getters y setters ─────────────────────────────────────────────────────

	//id en BD, o 0 si la partida aún no fue persistida
	public int getId() {
		return id;
	}

	// id asignado por BD tras el INSERT 
	public void setId(int id) {
		this.id = id;
	}

	// id del jugador propietario (FK → jugadores)
	public int getIdJugador() {
		return idJugador;
	}

	public int getIdPersonaje() {
		return idPersonaje;
	}

	// Fase activa cuando se guardó la partida
	public int getFaseActual() {
		return faseActual;
	}

	public void setFaseActual(int f) {
		this.faseActual = f;
	}

	// HP del héroe en el momento del último guardado
	public int getHpActual() {
		return hpActual;
	}

	// HP actualizados antes de persistir 
	public void setHpActual(int hp) {
		this.hpActual = hp;
	}

	public int getPmActual() {
		return pmActual;
	}

	// PM actualizados antes de persistir
	public void setPmActual(int pm) {
		this.pmActual = Math.max(0, pm);
	}

	//estado actual de la partida 
	public Estado getEstado() {
		return estado;
	}

	// nuevo estado a persistir 
	public void setEstado(Estado e) {
		this.estado = e;
	}

	public String getTipoEnemigo() {
		return tipoEnemigo;
	}

	public void setTipoEnemigo(String t) {
		this.tipoEnemigo = t;
	}

	public int getHpEnemigo() {
		return hpEnemigo;
	}

	//HP actuales del enemigo antes de persistir 
	public void setHpEnemigo(int hp) {
		this.hpEnemigo = Math.max(0, hp);
	}

	public int getPmEnemigo() {
		return pmEnemigo;
	}

	// PM actuales del enemigo antes de persistir
	public void setPmEnemigo(int pm) {
		this.pmEnemigo = Math.max(0, pm);
	}
	
	// ── toString ─────────────────────────────────────────────────────

	@Override
	public String toString() {
		return String.format("Partida[%d] fase:%d hp:%d estado:%s enemigo:%s(hp:%d)", id, faseActual, hpActual, estado,
				tipoEnemigo, hpEnemigo);
	}
}
