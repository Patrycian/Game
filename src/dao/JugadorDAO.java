package dao;

import modelo.Jugador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code jugadores}.
 */
public class JugadorDAO {

    // ── Insertar ──────────────────────────────────────────────────────────────

    /**
     * Inserta un nuevo jugador en la BD y asigna el id generado.
     * Si el nick ya existe, devuelve el jugador existente en su lugar.
     */
    public static Jugador insertar(Jugador jugador) throws SQLException {
        // Comprobar si el nick ya existe
        Jugador existente = buscarPorNick(jugador.getNick());
        if (existente != null) return existente;

        String sql = "INSERT INTO jugadores (nick, puntuacion) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, jugador.getNick());
            ps.setInt(2, jugador.getPuntuacion());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) jugador.setId(rs.getInt("id"));
        }
        return jugador;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public static Jugador buscarPorNick(String nick) throws SQLException {
        String sql = "SELECT id, nick, puntuacion FROM jugadores WHERE nick = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Jugador(rs.getInt("id"), rs.getString("nick"), rs.getInt("puntuacion"));
            }
        }
        return null;
    }

    public static Jugador buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nick, puntuacion FROM jugadores WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Jugador(rs.getInt("id"), rs.getString("nick"), rs.getInt("puntuacion"));
            }
        }
        return null;
    }

    /** Devuelve todos los jugadores ordenados por puntuación descendente (ranking). */
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

    /** Actualiza la puntuación del jugador en la BD. */
    public static void actualizarPuntuacion(Jugador jugador) throws SQLException {
        String sql = "UPDATE jugadores SET puntuacion = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, jugador.getPuntuacion());
            ps.setInt(2, jugador.getId());
            ps.executeUpdate();
        }
    }
}
