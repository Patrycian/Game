package dao;

import modelo.Partida;
import modelo.Partida.Estado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code partidas}.
 */
public class PartidaDAO {

    // ── Insertar ──────────────────────────────────────────────────────────────

    /** Crea una nueva fila en partidas y asigna el id al objeto. */
    public static void insertar(Partida partida) throws SQLException {
        String sql = "INSERT INTO partidas (id_jugador, id_personaje, fase_actual, hp_actual, estado) "
                   + "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, partida.getIdJugador());
            ps.setInt(2, partida.getIdPersonaje());
            ps.setInt(3, partida.getFaseActual());
            ps.setInt(4, partida.getHpActual());
            ps.setString(5, partida.getEstado().name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) partida.setId(rs.getInt("id"));
        }
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    /** Actualiza fase, HP y estado de una partida existente. */
    public static void actualizar(Partida partida) throws SQLException {
        String sql = "UPDATE partidas SET fase_actual = ?, hp_actual = ?, estado = ?, "
                   + "fecha_guardado = NOW() WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, partida.getFaseActual());
            ps.setInt(2, Math.max(0, partida.getHpActual()));
            ps.setString(3, partida.getEstado().name());
            ps.setInt(4, partida.getId());
            ps.executeUpdate();
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Devuelve la partida EN_CURSO más reciente de un jugador, o null si no hay. */
    public static Partida buscarPartidaActiva(int idJugador) throws SQLException {
        String sql = "SELECT * FROM partidas WHERE id_jugador = ? AND estado = 'EN_CURSO' "
                   + "ORDER BY fecha_guardado DESC LIMIT 1";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    /** Devuelve todas las partidas EN_CURSO (para el menú de carga). */
    public static List<Partida> listarPartidasActivas() throws SQLException {
        List<Partida> lista = new ArrayList<>();
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

    private static Partida mapear(ResultSet rs) throws SQLException {
        return new Partida(
            rs.getInt("id"),
            rs.getInt("id_jugador"),
            rs.getInt("id_personaje"),
            rs.getInt("fase_actual"),
            rs.getInt("hp_actual"),
            Estado.valueOf(rs.getString("estado"))
        );
    }
}
