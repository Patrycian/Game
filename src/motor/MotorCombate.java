package motor;

import dao.EnemigoDAO;
import modelo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Motor de combate por turnos (1 héroe vs 1 enemigo).
 *
 * <p>Flujo de un turno:</p>
 * <ol>
 *   <li>El héroe ataca al enemigo o ejecuta una {@link Habilidad} directamente.</li>
 *   <li>Si el enemigo sigue vivo, el enemigo ataca al héroe.</li>
 *   <li>Se comprueba si alguno ha llegado a 0 HP → fin del combate.</li>
 * </ol>
 *
 * <p>El controlador llama a {@link #iniciarTurno()}, luego a
 * {@link #ejecutarAtaqueBasico()} (o ejecuta una {@link Habilidad} directamente
 * y llama a {@link #verificarResultado()}), y por último a
 * {@link #ejecutarReaccionEnemigo()} con la pausa visual entre medias.</p>
 */
public class MotorCombate {

    public enum ResultadoCombate { EN_CURSO, VICTORIA, DERROTA }

    private final Heroe   heroe;
    private final Enemigo enemigo;
    private int           turno;
    private ResultadoCombate resultado;

    public MotorCombate(Heroe heroe, Enemigo enemigo) {
        this.heroe     = heroe;
        this.enemigo   = enemigo;
        this.turno     = 0;
        this.resultado = ResultadoCombate.EN_CURSO;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Incrementa el contador de turnos y devuelve la línea separadora que debe
     * mostrarse al inicio de cada turno en el log de combate.
     *
     * @return cadena con el separador visual del turno (p. ej. {@code "\n── TURNO 1 ────"})
     */
    public String iniciarTurno() {
        turno++;
        String turnoStr = String.valueOf(turno);
        return "\n── TURNO " + turnoStr + " " + "─".repeat(Math.max(0, 28 - turnoStr.length()));
    }

    /**
     * Ejecuta el ataque básico del héroe contra el enemigo y devuelve los mensajes del resultado.
     *
     * <p>Si el enemigo cae, actualiza el resultado a VICTORIA y añade los mensajes de victoria.</p>
     *
     * <p>Para habilidades especiales, el controlador llama a {@link Habilidad#ejecutar}
     * directamente y luego invoca {@link #verificarResultado()} para sincronizar el estado.</p>
     *
     * @return mensajes de la acción del héroe
     */
    public List<String> ejecutarAtaqueBasico() {
        List<String> log = new ArrayList<>();
        if (resultado != ResultadoCombate.EN_CURSO) { return log; }

        int danio = enemigo.recibirAtaque(heroe);
        log.add(String.format("▸ %s %s  →  -%d HP  [%s: %d/%d HP]",
                heroe.getIcono(), heroe.getNombre(), danio,
                enemigo.getNombre(), enemigo.getPuntosGolpe(), enemigo.getPuntosGolpeMax()));

        log.addAll(verificarResultado());
        return log;
    }

    /**
     * Comprueba si alguno de los combatientes ha llegado a 0 HP y actualiza
     * el resultado del combate en consecuencia.
     *
     * <p>Se llama internamente tras {@link #ejecutarAtaqueBasico()} y externamente
     * por el controlador tras ejecutar una {@link Habilidad} directamente sobre
     * el modelo.</p>
     *
     * @return lista de mensajes de fin de combate (vacía si el combate sigue en curso)
     */
    public List<String> verificarResultado() {
        List<String> log = new ArrayList<>();
        if (resultado != ResultadoCombate.EN_CURSO) { return log; }

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
     * Ejecuta únicamente la <b>reacción del enemigo</b> dentro del turno actual.
     *
     * <p>No incrementa el contador de turnos (el turno ya fue iniciado con
     * {@link #iniciarTurno()}). Gestiona el Escudo Arcano del Mago si está activo.</p>
     *
     * @return mensajes del contraataque enemigo; vacío si el combate ya terminó o el enemigo cayó
     */
    public List<String> ejecutarReaccionEnemigo() {
        List<String> log = new ArrayList<>();
        if (resultado != ResultadoCombate.EN_CURSO || !enemigo.estaVivo()) { return log; }

        String ataque         = enemigo.realizarAtaque(heroe);
        String mensajeDefensa = heroe.consumirMensajeDefensa();
        log.add("◀ " + (mensajeDefensa != null ? mensajeDefensa : ataque));

        log.addAll(verificarResultado());
        return log;
    }

    /**
     * Ejecuta un turno completo (héroe ataca + enemigo contraataca) de forma atómica.
     *
     * <p>Mantenido por compatibilidad con código de pruebas; el controlador principal
     * usa {@link #iniciarTurno()}, {@link #ejecutarAtaqueBasico()} y
     * {@link #ejecutarReaccionEnemigo()} por separado para poder insertar pausas visuales.</p>
     *
     * @return lista completa de mensajes del turno
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

    // ── Fábrica de enemigos aleatorios ────────────────────────────────────────

    /**
     * Crea un enemigo del tipo concreto indicado, usando los datos del catálogo de BD
     * si están disponibles. Se usa al reanudar una partida guardada para restaurar
     * el mismo tipo de enemigo que estaba activo cuando el jugador huyó.
     *
     * <p>Si el tipo no coincide con ningún enemigo conocido, se genera un Goblin
     * como fallback seguro.</p>
     *
     * @param tipo nombre del tipo en mayúsculas ("GOBLIN", "OGRO", "SAGA", "DRAGON")
     * @return enemigo del tipo solicitado con HP al máximo
     */
    public static Enemigo generarEnemigoDeTipo(String tipo) {
        Map<String, EnemigoDatos> catalogo = EnemigoDAO.getCatalogo();
        switch (tipo.toUpperCase()) {
            case "DRAGON": return crearEnemigoDesdeCatalogo("DRAGON", catalogo);
            case "OGRO":   return crearEnemigoDesdeCatalogo("OGRO",   catalogo);
            case "GOBLIN": return crearEnemigoDesdeCatalogo("GOBLIN", catalogo);
            case "SAGA":   return crearEnemigoDesdeCatalogo("SAGA",   catalogo);
            default:       return crearEnemigoDesdeCatalogo("GOBLIN", catalogo); // fallback seguro
        }
    }

    /**
     * Construye un enemigo del tipo indicado usando los datos del catálogo de BD
     * cuando están disponibles, o los valores por defecto hardcodeados si no lo están.
     *
     * @param tipo     clave del enemigo en el catálogo ("GOBLIN", "OGRO", "SAGA", "DRAGON")
     * @param catalogo mapa de datos cargado desde BD por {@link EnemigoDAO}
     * @return enemigo listo para combatir
     */
    private static Enemigo crearEnemigoDesdeCatalogo(String tipo, Map<String, EnemigoDatos> catalogo) {
        EnemigoDatos datos = catalogo.get(tipo);
        switch (tipo) {
            case "DRAGON": return datos != null ? new Dragon(datos) : new Dragon();
            case "OGRO":   return datos != null ? new Ogro(datos)   : new Ogro();
            case "SAGA":   return datos != null ? new Saga(datos)   : new Saga();
            default:       return datos != null ? new Goblin(datos) : new Goblin();
        }
    }

    /**
     * Genera el enemigo correspondiente a la fase indicada.
     * Intenta cargar los stats desde la BD mediante {@link EnemigoDAO}.
     * Si la BD no está disponible, usa los constructores por defecto (valores hardcodeados).
     *
     * <p>Fases 1–3: enemigo aleatorio entre Ogro, Goblin, Saga.
     * Fase 4: Dragón (jefe final, siempre).</p>
     *
     * @param fase número de fase (1–4)
     * @return enemigo generado para esa fase
     */
    public static Enemigo generarEnemigo(int fase) {
        Map<String, EnemigoDatos> catalogo = EnemigoDAO.getCatalogo();

        if (fase == 4) {
            return crearEnemigoDesdeCatalogo("DRAGON", catalogo);
        }

        // Fases 1-3: elegir al azar entre los tres enemigos normales
        Enemigo[] pool = {
            crearEnemigoDesdeCatalogo("OGRO",   catalogo),
            crearEnemigoDesdeCatalogo("GOBLIN", catalogo),
            crearEnemigoDesdeCatalogo("SAGA",   catalogo)
        };
        return pool[new Random().nextInt(pool.length)];
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public ResultadoCombate getResultado() { return resultado; }
    public int              getTurno()     { return turno; }
    public Heroe            getHeroe()     { return heroe; }
    public Enemigo          getEnemigo()   { return enemigo; }
    public boolean          haTerminado()  { return resultado != ResultadoCombate.EN_CURSO; }
}
