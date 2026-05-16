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
 * Flujo de un turno:
 *  1. El héroe ataca al enemigo  (o usa su habilidad especial).
 *  2. Si el enemigo sigue vivo, el enemigo ataca al héroe.
 *  3. Se comprueba si alguno ha llegado a 0 HP → fin del combate.
 *
 * El controlador llama a {@link #ejecutarTurnoHeroe(boolean)} para el turno
 * del jugador y recibe la lista de mensajes ocurridos en ese turno.
 */
public class MotorCombate {

    public enum ResultadoCombate { EN_CURSO, VICTORIA, DERROTA }

    private final Heroe   heroe;
    private final Enemigo enemigo;
    private int           turno;
    private ResultadoCombate resultado;

    public MotorCombate(Heroe heroe, Enemigo enemigo) {
        this.heroe    = heroe;
        this.enemigo  = enemigo;
        this.turno    = 0;
        this.resultado = ResultadoCombate.EN_CURSO;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Incrementa el contador de turnos y devuelve la línea separadora que debe
     * mostrarse al inicio de cada turno en el log de combate.
     *
     * <p>Se llama desde el controlador antes de mostrar cualquier acción del turno,
     * de modo que el separador aparece justo antes del bloque de mensajes.</p>
     *
     * @return cadena con el separador visual del turno (p. ej. {@code "\n── TURNO 1 ─────────────"})
     */
    public String iniciarTurno() {
        turno++;
        String n = String.valueOf(turno);
        return "\n── TURNO " + n + " " + "─".repeat(Math.max(0, 28 - n.length()));
    }

    /**
     * Ejecuta únicamente la <b>acción del héroe</b> dentro del turno actual.
     *
     * <p>A diferencia de {@link #ejecutarTurnoHeroe}, este método no realiza el
     * contraataque enemigo. Está pensado para que el controlador pueda insertar
     * una pausa visual entre la acción del héroe y la reacción del enemigo.</p>
     *
     * <p>Precondición: el controlador debe haber llamado antes a {@link #iniciarTurno()}.</p>
     *
     * @param usarHabilidad {@code true} para habilidad especial; {@code false} para ataque básico
     * @return mensajes de la acción del héroe; si el enemigo cae, incluye los mensajes de victoria
     */
    public List<String> ejecutarAccionHeroe(boolean usarHabilidad) {
        List<String> log = new ArrayList<>();
        if (resultado != ResultadoCombate.EN_CURSO) return log;

        if (usarHabilidad) {
            Personaje objetivo = (heroe instanceof Clerigo) ? heroe : enemigo;
            log.add("▸ " + heroe.usarHabilidad(objetivo));
        } else {
            int danio = enemigo.recibirAtaque(heroe);
            log.add(String.format("▸ %s %s  →  -%d HP  [%s: %d/%d HP]",
                    heroe.getIcono(), heroe.getNombre(), danio,
                    enemigo.getNombre(), enemigo.getPuntosGolpe(), enemigo.getPuntosGolpeMax()));
        }

        if (!enemigo.estaVivo()) {
            resultado = ResultadoCombate.VICTORIA;
            log.add("  💀 ¡" + enemigo.getNombre() + " derrotado!");
            log.add("  🏆 ¡VICTORIA!");
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
        if (resultado != ResultadoCombate.EN_CURSO || !enemigo.estaVivo()) return log;

        String ataque         = enemigo.realizarAtaque(heroe);
        String mensajeDefensa = heroe.consumirMensajeDefensa();
        log.add("◀ " + (mensajeDefensa != null ? mensajeDefensa : ataque));

        if (!heroe.estaVivo()) {
            resultado = ResultadoCombate.DERROTA;
            log.add("  💀 " + heroe.getNombre() + " ha caído en combate...");
            log.add("  ☠  Derrota. Fin de la aventura.");
        }
        return log;
    }

    /**
     * Ejecuta un turno completo (héroe + enemigo) de forma atómica.
     * Mantiene compatibilidad con código legado; el controlador principal
     * usa {@link #iniciarTurno()}, {@link #ejecutarAccionHeroe} y
     * {@link #ejecutarReaccionEnemigo()} para poder insertar pausas visuales.
     *
     * @param usarHabilidad true si el jugador quiere usar la habilidad especial
     * @return lista completa de mensajes del turno
     */
    public List<String> ejecutarTurnoHeroe(boolean usarHabilidad) {
        List<String> log = new ArrayList<>();
        if (resultado != ResultadoCombate.EN_CURSO) {
            log.add("El combate ya ha terminado.");
            return log;
        }
        log.add(iniciarTurno());
        log.addAll(ejecutarAccionHeroe(usarHabilidad));
        if (resultado == ResultadoCombate.EN_CURSO)
            log.addAll(ejecutarReaccionEnemigo());
        return log;
    }

    /**
     * Inicia un nuevo turno y ejecuta únicamente el contraataque del enemigo.
     * Se usa cuando el héroe realiza una acción no-ataque (buff, poción) que
     * ocupa su turno pero no es gestionada por {@link #ejecutarAccionHeroe}.
     *
     * @return lista de mensajes del contraataque (vacía si el combate ya terminó
     *         o el enemigo está derrotado)
     * @deprecated Usar {@link #iniciarTurno()} + {@link #ejecutarReaccionEnemigo()} directamente.
     */
    @Deprecated
    public List<String> ejecutarContraataqueEnemigo() {
        List<String> log = new ArrayList<>();
        if (resultado != ResultadoCombate.EN_CURSO || !enemigo.estaVivo()) return log;
        log.add(iniciarTurno());
        log.addAll(ejecutarReaccionEnemigo());
        return log;
    }

    // ── Fábrica de enemigos aleatorios ────────────────────────────────────────

    /**
     * Crea un enemigo del tipo concreto indicado, usando los datos del catálogo de BD
     * si están disponibles. Se usa al reanudar una partida guardada para restaurar
     * el mismo tipo de enemigo que estaba activo cuando el jugador huyó.
     *
     * <p>Si el tipo no coincide con ningún enemigo conocido, se genera un enemigo
     * aleatorio de fase 1 como fallback seguro.</p>
     *
     * @param tipo nombre del tipo en mayúsculas ("GOBLIN", "OGRO", "SAGA", "DRAGON")
     * @return enemigo del tipo solicitado con HP al máximo (el HP guardado se aplica
     *         externamente tras la llamada con {@code enemigo.setPuntosGolpe(hp)})
     */
    public static Enemigo generarEnemigoDeTipo(String tipo) {
        Map<String, EnemigoDatos> cat = EnemigoDAO.getCatalogo();
        switch (tipo.toUpperCase()) {
            case "DRAGON": { EnemigoDatos d = cat.get("DRAGON"); return d != null ? new Dragon(d)  : new Dragon();  }
            case "OGRO":   { EnemigoDatos d = cat.get("OGRO");   return d != null ? new Ogro(d)    : new Ogro();    }
            case "GOBLIN": { EnemigoDatos d = cat.get("GOBLIN"); return d != null ? new Goblin(d)  : new Goblin();  }
            case "SAGA":   { EnemigoDatos d = cat.get("SAGA");   return d != null ? new Saga(d)    : new Saga();    }
            default: {     // tipo desconocido (BD corrupta) → Goblin como fallback más seguro
                EnemigoDatos d = cat.get("GOBLIN");
                return d != null ? new Goblin(d) : new Goblin();
            }
        }
    }

    /**
     * Genera el enemigo correspondiente a la fase indicada.
     * Intenta cargar los stats desde la BD mediante {@link EnemigoDAO}.
     * Si la BD no está disponible, usa los constructores por defecto (valores hardcodeados).
     *
     * Fases 1-3 → enemigo aleatorio entre Ogro, Goblin, Saga.
     * Fase 4    → Dragón (jefe final, siempre).
     */
    public static Enemigo generarEnemigo(int fase) {
        Map<String, EnemigoDatos> cat = EnemigoDAO.getCatalogo();

        if (fase == 4) {
            EnemigoDatos d = cat.get("DRAGON");
            return (d != null) ? new Dragon(d) : new Dragon();
        }

        // Pool de fases 1-3: construir con datos de BD si están disponibles
        EnemigoDatos dOgro   = cat.get("OGRO");
        EnemigoDatos dGoblin = cat.get("GOBLIN");
        EnemigoDatos dSaga   = cat.get("SAGA");

        Enemigo[] pool = {
            (dOgro   != null) ? new Ogro(dOgro)     : new Ogro(),
            (dGoblin != null) ? new Goblin(dGoblin)  : new Goblin(),
            (dSaga   != null) ? new Saga(dSaga)      : new Saga()
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
