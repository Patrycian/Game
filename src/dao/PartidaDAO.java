package dao;

import modelo.Partida;
import modelo.Partida.Estado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code partidas}.
 *
 * <p>Gestiona el ciclo de vida completo de una partida: creación al iniciar,
 * actualización al avanzar de fase o guardar, y consulta al reanudar.</p>
 *
 * <p>Esquema de la tabla:</p>
 * <pre>
 *   partidas (
 *     id              SERIAL PRIMARY KEY,
 *     id_jugador      INT REFERENCES jugadores(id),
 *     id_personaje    INT REFERENCES personajes(id),
 *     fase_actual     INT,              -- 1-4
 *     hp_actual       INT,              -- HP del héroe al guardar
 *     pm_actual       INT DEFAULT 0,   -- PM del héroe mágico al guardar (0 si no es mágico)
 *     estado          VARCHAR(20),      -- 'EN_CURSO' | 'COMPLETADA' | 'DERROTA'
 *     tipo_enemigo    VARCHAR(20),      -- tipo del enemigo activo al guardar (NULL si nueva fase)
 *     hp_enemigo      INT DEFAULT 0,   -- HP del enemigo al guardar (0 si derrotado o nueva fase)
 *     pm_enemigo      INT DEFAULT 0,   -- PM del enemigo al guardar (0 si no usa magia)
 *     fecha_guardado  TIMESTAMP DEFAULT NOW()
 *   )
 * </pre>
 */
public class PartidaDAO {

    // ── Insertar ──────────────────────────────────────────────────────────────

    /**
     * Crea una nueva fila en la tabla {@code partidas} y asigna el id generado
     * al objeto recibido.
     *
     * <p>Se llama la primera vez que hay que guardar la partida (normalmente al
     * terminar el primer combate). Las llamadas posteriores usan {@link #actualizar}.</p>
     *
     * @param partida objeto con los datos a persistir (id debe ser 0)
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static void insertar(Partida partida) throws SQLException {
        String sql = "INSERT INTO partidas (id_jugador, id_personaje, fase_actual, hp_actual, pm_actual, estado, tipo_enemigo, hp_enemigo, pm_enemigo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, partida.getIdJugador());
            ps.setInt(2, partida.getIdPersonaje());
            ps.setInt(3, partida.getFaseActual());
            ps.setInt(4, partida.getHpActual());
            ps.setInt(5, partida.getPmActual());
            ps.setString(6, partida.getEstado().name());
            ps.setString(7, partida.getTipoEnemigo());   // puede ser null → SQL NULL
            ps.setInt(8, partida.getHpEnemigo());
            ps.setInt(9, partida.getPmEnemigo());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { partida.setId(rs.getInt("id")); }
            }
        }
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    /**
     * Actualiza la fase, el HP actual y el estado de una partida ya persistida.
     * También actualiza el timestamp {@code fecha_guardado} a la hora actual.
     *
     * <p>Se llama al avanzar de fase (checkpoint de progreso), al marcar una
     * partida como {@code DERROTA} o {@code COMPLETADA}, y al registrar
     * el primer combate cuando la partida aún no tenía id.</p>
     *
     * @param partida objeto con los datos actualizados (debe tener un id válido)
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static void actualizar(Partida partida) throws SQLException {
        String sql = "UPDATE partidas SET fase_actual = ?, hp_actual = ?, pm_actual = ?, estado = ?, "
                   + "tipo_enemigo = ?, hp_enemigo = ?, pm_enemigo = ?, fecha_guardado = NOW() WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, partida.getFaseActual());
            ps.setInt(2, Math.max(0, partida.getHpActual()));
            ps.setInt(3, partida.getPmActual());
            ps.setString(4, partida.getEstado().name());
            ps.setString(5, partida.getTipoEnemigo());   // puede ser null → SQL NULL
            ps.setInt(6, partida.getHpEnemigo());
            ps.setInt(7, partida.getPmEnemigo());
            ps.setInt(8, partida.getId());
            ps.executeUpdate();
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Devuelve la partida {@code EN_CURSO} más reciente de un jugador concreto,
     * o {@code null} si el jugador no tiene ninguna activa.
     *
     * @param idJugador id del jugador a buscar
     * @return la {@link Partida} activa más reciente, o {@code null}
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static Partida buscarPartidaActiva(int idJugador) throws SQLException {
        String sql = "SELECT * FROM partidas WHERE id_jugador = ? AND estado = 'EN_CURSO' "
                   + "ORDER BY fecha_guardado DESC LIMIT 1";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { return mapear(rs); }
            }
        }
        return null;
    }

    /**
     * Devuelve todas las partidas {@code EN_CURSO} de todos los jugadores,
     * ordenadas por fecha de guardado descendente (la más reciente primero).
     * Se usa para poblar la pantalla "Cargar Partida".
     *
     * @return lista de {@link Partida} activas (puede estar vacía si no hay ninguna)
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static List<Partida> listarPartidasActivas() throws SQLException {
        List<Partida> lista = new ArrayList<>();
        // JOIN con jugadores para poder mostrar el nick en la UI si fuera necesario
        String sql = "SELECT p.*, j.nick FROM partidas p "
                   + "JOIN jugadores j ON j.id = p.id_jugador "
                   + "WHERE p.estado = 'EN_CURSO' "
                   + "ORDER BY p.fecha_guardado DESC";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Convierte una fila del {@link ResultSet} en un objeto {@link Partida}.
     * Requiere que el cursor ya esté posicionado (llamar a {@code rs.next()} antes).
     *
     * @param rs ResultSet posicionado en una fila válida
     * @return objeto {@link Partida} mapeado
     * @throws SQLException si alguna columna no existe o su tipo no coincide
     */
    private static Partida mapear(ResultSet rs) throws SQLException {
        return new Partida(
            rs.getInt("id"),
            rs.getInt("id_jugador"),
            rs.getInt("id_personaje"),
            rs.getInt("fase_actual"),
            rs.getInt("hp_actual"),
            rs.getInt("pm_actual"),
            Estado.valueOf(rs.getString("estado")),
            rs.getString("tipo_enemigo"),   // puede devolver null si la columna es NULL
            rs.getInt("hp_enemigo"),
            rs.getInt("pm_enemigo")
        );
    }
}
