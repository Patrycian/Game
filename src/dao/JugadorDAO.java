package dao;

import modelo.Jugador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code jugadores}.
 *
 * <p>Todas las operaciones son estáticas (sin estado de instancia) y gestionan
 * su propia transacción a través de la conexión singleton de {@link ConexionDB}.</p>
 *
 * <p>Esquema de la tabla:</p>
 * <pre>
 *   jugadores (
 *     id          SERIAL PRIMARY KEY,
 *     nick        VARCHAR(50) UNIQUE NOT NULL,
 *     puntuacion  INT DEFAULT 0
 *   )
 * </pre>
 */
public class JugadorDAO {

    // ── Insertar ──────────────────────────────────────────────────────────────

    /**
     * Inserta un nuevo jugador en la BD y asigna el id generado al objeto.
     *
     * <p>Si el nick ya existe (constraint UNIQUE), no inserta un duplicado:
     * devuelve directamente el jugador ya registrado con sus datos actuales.</p>
     *
     * @param jugador objeto con el nick a registrar (id puede ser 0)
     * @return el mismo objeto {@code jugador} con el id asignado, o el jugador
     *         existente si el nick ya estaba registrado
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static Jugador insertar(Jugador jugador) throws SQLException {
        // Comprobar si el nick ya existe para evitar duplicados
        Jugador existente = buscarPorNick(jugador.getNick());
        if (existente != null) return existente;

        String sql = "INSERT INTO jugadores (nick, puntuacion) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, jugador.getNick());
            ps.setInt(2, jugador.getPuntuacion());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) jugador.setId(rs.getInt("id"));
            }
        }
        return jugador;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Busca un jugador por su nick único.
     *
     * @param nick nick exacto a buscar (sensible a mayúsculas según collation de BD)
     * @return el {@link Jugador} encontrado, o {@code null} si no existe
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static Jugador buscarPorNick(String nick) throws SQLException {
        String sql = "SELECT id, nick, puntuacion FROM jugadores WHERE nick = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, nick);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Jugador(rs.getInt("id"), rs.getString("nick"), rs.getInt("puntuacion"));
                }
            }
        }
        return null;
    }

    /**
     * Busca un jugador por su id de BD.
     * Se usa al recargar una partida guardada para obtener el jugador propietario.
     *
     * @param id id de la tabla {@code jugadores}
     * @return el {@link Jugador} encontrado, o {@code null} si el id no existe
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static Jugador buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nick, puntuacion FROM jugadores WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Jugador(rs.getInt("id"), rs.getString("nick"), rs.getInt("puntuacion"));
                }
            }
        }
        return null;
    }

    /**
     * Devuelve los 10 jugadores con mayor puntuación, ordenados de mayor a menor.
     * Se usa para construir la pantalla de ranking.
     *
     * @return lista de hasta 10 {@link Jugador} ordenados por puntuación descendente
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static List<Jugador> obtenerRanking() throws SQLException {
        List<Jugador> lista = new ArrayList<>();
        String sql = "SELECT id, nick, puntuacion FROM jugadores ORDER BY puntuacion DESC LIMIT 10";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Jugador(rs.getInt("id"), rs.getString("nick"), rs.getInt("puntuacion")));
            }
        }
        return lista;
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    /**
     * Actualiza la puntuación del jugador en la BD.
     * Se llama desde {@code MazmorraController} cada vez que el héroe gana un combate.
     *
     * @param jugador jugador con la puntuación ya actualizada en memoria
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static void actualizarPuntuacion(Jugador jugador) throws SQLException {
        String sql = "UPDATE jugadores SET puntuacion = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, jugador.getPuntuacion());
            ps.setInt(2, jugador.getId());
            ps.executeUpdate();
        }
    }
}
