package motor;

import dao.EnemigoDAO;
import modelo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MotorCombate {

	public enum ResultadoCombate {
		EN_CURSO, VICTORIA, DERROTA
	}

	private final Heroe heroe;
	private final Enemigo enemigo;
	private int turno;
	private ResultadoCombate resultado;

	// ── Constructor ───────────────────────────────────────────────────────────

	public MotorCombate(Heroe heroe, Enemigo enemigo) {
		this.heroe = heroe;
		this.enemigo = enemigo;
		this.turno = 0;
		this.resultado = ResultadoCombate.EN_CURSO;
	}

	// ── Getters ───────────────────────────────────────────────────────────────

	public ResultadoCombate getResultado() {
		return resultado;
	}

	public int getTurno() {
		return turno;
	}

	public Heroe getHeroe() {
		return heroe;
	}

	public Enemigo getEnemigo() {
		return enemigo;
	}

	public boolean haTerminado() {
		return resultado != ResultadoCombate.EN_CURSO;
	}

	// ── Métodos ───────────────────────────────────────────────────────────

	/**
	 * Incrementa el contador de turnos y devuelve la línea separadora que debe
	 * mostrarse al inicio de cada turno en el log de combate.
	 */
	public String iniciarTurno() {
		turno++;
		String turnoStr = String.valueOf(turno);
		return "\n── TURNO " + turnoStr + " " + "─".repeat(Math.max(0, 28 - turnoStr.length()));
	}

	/**
	 * Ejecuta el ataque básico del héroe contra el enemigo y devuelve los mensajes
	 * del resultado. Si el enemigo cae, actualiza el resultado a VICTORIA y añade
	 * los mensajes de victoria.
	 */
	public List<String> ejecutarAtaqueBasico() {
		List<String> log = new ArrayList<>();
		if (resultado != ResultadoCombate.EN_CURSO) {
			return log;
		}

		int danio = enemigo.recibirAtaque(heroe);
		log.add(String.format("▸ %s %s  →  -%d HP  [%s: %d/%d HP]", heroe.getIcono(), heroe.getNombre(), danio,
				enemigo.getNombre(), enemigo.getPuntosGolpe(), enemigo.getPuntosGolpeMax()));

		log.addAll(verificarResultado());
		return log;
	}

	/**
	 * Comprueba si alguno de los combatientes ha llegado a 0 HP y actualiza el
	 * resultado del combate en consecuencia.
	 *
	 * @return lista de mensajes de fin de combate (vacía si el combate sigue en
	 *         curso)
	 */
	public List<String> verificarResultado() {
		List<String> log = new ArrayList<>();
		if (resultado != ResultadoCombate.EN_CURSO) {
			return log;
		}

		if (!enemigo.estaVivo()) {
			resultado = ResultadoCombate.VICTORIA;
			log.add("  💀 ¡" + enemigo.getNombre() + " derrotado!");
			log.add("  🏆 ¡VICTORIA!");
		} else if (!heroe.estaVivo()) {
			resultado = ResultadoCombate.DERROTA;
			log.add("  💀 " + heroe.getNombre() + " ha caído en combate...");
			log.add("  ☠  Derrota. Fin de la aventura.");
		}
		return log;
	}

	/**
	 * Ejecuta únicamente la reacción del enemigo dentro del turno actual.
	 */
	public List<String> ejecutarReaccionEnemigo() {
		List<String> log = new ArrayList<>();
		if (resultado != ResultadoCombate.EN_CURSO || !enemigo.estaVivo()) {
			return log;
		}

		String ataque = enemigo.realizarAtaque(heroe);
		String mensajeDefensa = heroe.consumirMensajeDefensa();
		log.add("◀ " + (mensajeDefensa != null ? mensajeDefensa : ataque));

		log.addAll(verificarResultado());
		return log;
	}

	/**
	 * Ejecuta un turno completo (héroe ataca + enemigo contraataca).
	 */
	public List<String> ejecutarTurnoHeroe() {
		List<String> log = new ArrayList<>();
		if (resultado != ResultadoCombate.EN_CURSO) {
			log.add("El combate ya ha terminado.");
			return log;
		}
		log.add(iniciarTurno());
		log.addAll(ejecutarAtaqueBasico());
		if (resultado == ResultadoCombate.EN_CURSO) {
			log.addAll(ejecutarReaccionEnemigo());
		}
		return log;
	}

	/**
	 * Crea un enemigo del tipo concreto indicado, usando los datos del catálogo de
	 * BD si están disponibles. Se usa al reanudar una partida guardada para
	 * restaurar el mismo tipo de enemigo que estaba activo cuando el jugador huyó.
	 *
	 * Si el tipo no coincide con ningún enemigo conocido, se genera un Goblin como
	 * fallback seguro.
	 */
	public static Enemigo generarEnemigoDeTipo(String tipo) {
		Map<String, EnemigoDatos> catalogo = EnemigoDAO.getCatalogo();
		switch (tipo.toUpperCase()) {
		case "DRAGON":
			return crearEnemigoDesdeCatalogo("DRAGON", catalogo);
		case "OGRO":
			return crearEnemigoDesdeCatalogo("OGRO", catalogo);
		case "GOBLIN":
			return crearEnemigoDesdeCatalogo("GOBLIN", catalogo);
		case "SAGA":
			return crearEnemigoDesdeCatalogo("SAGA", catalogo);
		default:
			return crearEnemigoDesdeCatalogo("GOBLIN", catalogo); // fallback seguro
		}
	}

	/**
	 * Construye un enemigo del tipo indicado usando los datos del catálogo de BD
	 * cuando están disponibles, o los valores por defecto hardcodeados si no lo
	 * están.
	 */
	private static Enemigo crearEnemigoDesdeCatalogo(String tipo, Map<String, EnemigoDatos> catalogo) {
		EnemigoDatos datos = catalogo.get(tipo);
		switch (tipo) {
		case "DRAGON":
			return datos != null ? new Dragon(datos) : new Dragon();
		case "OGRO":
			return datos != null ? new Ogro(datos) : new Ogro();
		case "SAGA":
			return datos != null ? new Saga(datos) : new Saga();
		default:
			return datos != null ? new Goblin(datos) : new Goblin();
		}
	}

	/**
	 * Genera el enemigo correspondiente a la fase indicada. Intenta cargar los
	 * stats desde la BD mediante EnemigoDAO.
	 */
	public static Enemigo generarEnemigo(int fase) {
		Map<String, EnemigoDatos> catalogo = EnemigoDAO.getCatalogo();

		if (fase == 4) {
			return crearEnemigoDesdeCatalogo("DRAGON", catalogo);
		}

		// Fases 1-3: enemigo al azar
		Enemigo[] pool = { crearEnemigoDesdeCatalogo("OGRO", catalogo), crearEnemigoDesdeCatalogo("GOBLIN", catalogo),
				crearEnemigoDesdeCatalogo("SAGA", catalogo) };
		return pool[new Random().nextInt(pool.length)];
	}

}
