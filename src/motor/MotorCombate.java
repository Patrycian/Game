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
     * Ejecuta un turno completo del combate.
     *
     * @param usarHabilidad true si el jugador quiere usar la habilidad especial
     *                      en vez del ataque básico
     * @return lista de mensajes que describen lo ocurrido en el turno
     */
    public List<String> ejecutarTurnoHeroe(boolean usarHabilidad) {
        List<String> log = new ArrayList<>();

        if (resultado != ResultadoCombate.EN_CURSO) {
            log.add("El combate ya ha terminado.");
            return log;
        }

        turno++;
        log.add("── Turno " + turno + " ──────────────────────────");

        // 1. Acción del héroe
        if (usarHabilidad) {
            // Para el Clérigo la habilidad se aplica sobre sí mismo; para el resto, sobre el enemigo
            Personaje objetivo = (heroe instanceof Clerigo) ? heroe : enemigo;
            log.add(heroe.usarHabilidad(objetivo));
        } else {
            int danio = enemigo.recibirAtaque(heroe);
            log.add(String.format("%s %s ataca a %s causando %d de daño. (HP: %d/%d)",
                    heroe.getIcono(), heroe.getNombre(), enemigo.getNombre(),
                    danio, enemigo.getPuntosGolpe(), enemigo.getPuntosGolpeMax()));
        }

        // 2. Comprobar si el enemigo ha caído
        if (!enemigo.estaVivo()) {
            resultado = ResultadoCombate.VICTORIA;
            log.add("💀 " + enemigo.getNombre() + " ha sido derrotado.");
            log.add("🏆 ¡" + heroe.getNombre() + " GANA el combate!");
            return log;
        }

        // 3. Contraataque del enemigo
        String ataqueEnemigo = enemigo.realizarAtaque(heroe);
        // Si el héroe tiene un mensaje de defensa pendiente (p. ej. Escudo Arcano),
        // lo usamos en lugar del mensaje del ataque enemigo.
        String mensajeDefensa = heroe.consumirMensajeDefensa();
        log.add(mensajeDefensa != null ? mensajeDefensa : ataqueEnemigo);

        // 4. Comprobar si el héroe ha caído
        if (!heroe.estaVivo()) {
            resultado = ResultadoCombate.DERROTA;
            log.add("💀 " + heroe.getNombre() + " ha caído en combate...");
            log.add("☠ Derrota. Fin de la aventura.");
        }

        return log;
    }

    /**
     * Ejecuta únicamente el contraataque del enemigo, sin acción previa del héroe.
     * Se usa cuando el héroe realiza una habilidad mágica adicional (p. ej. Escudo
     * Arcano, Bendición Sagrada) que ocupa su turno pero no es un ataque directo
     * gestionado por {@link #ejecutarTurnoHeroe}.
     *
     * <p>Si el Escudo Arcano está activo cuando el enemigo ataca, el override de
     * {@link Heroe#recibirAtaque} absorbe el golpe y registra el mensaje descriptivo,
     * que este método recupera con {@link Heroe#consumirMensajeDefensa()}.</p>
     *
     * @return lista de mensajes del contraataque (vacía si el combate ya terminó
     *         o el enemigo está derrotado)
     */
    public List<String> ejecutarContraataqueEnemigo() {
        List<String> log = new ArrayList<>();
        if (resultado != ResultadoCombate.EN_CURSO || !enemigo.estaVivo()) return log;

        turno++;
        log.add("── Turno " + turno + " ──────────────────────────");

        String ataque       = enemigo.realizarAtaque(heroe);
        String mensajeDefensa = heroe.consumirMensajeDefensa();
        log.add(mensajeDefensa != null ? mensajeDefensa : ataque);

        if (!heroe.estaVivo()) {
            resultado = ResultadoCombate.DERROTA;
            log.add("💀 " + heroe.getNombre() + " ha caído en combate...");
            log.add("☠ Derrota. Fin de la aventura.");
        }

        return log;
    }

    // ── Fábrica de enemigos aleatorios ────────────────────────────────────────

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
