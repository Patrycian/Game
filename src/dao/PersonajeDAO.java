package dao;

import modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code personajes}.
 */
public class PersonajeDAO {

    // ── Insertar ──────────────────────────────────────────────────────────────

    /**
     * Persiste un héroe en la BD y le asigna el id generado.
     * @param heroe héroe a guardar
     * @param idJugador id del jugador propietario
     */
    public static void insertar(Heroe heroe, int idJugador) throws SQLException {
        String sql = "INSERT INTO personajes (nombre, tipo, puntos_golpe, defensa, poder, id_jugador) "
                   + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, heroe.getNombre());
            ps.setString(2, heroe.getTipo());
            ps.setInt(3, heroe.getPuntosGolpeMax());
            ps.setInt(4, heroe.getDefensa());
            ps.setInt(5, heroe.getPoder());
            ps.setInt(6, idJugador);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) heroe.setId(rs.getInt("id"));
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public static Heroe buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM personajes WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearHeroe(rs);
        }
        return null;
    }

    public static List<Heroe> buscarPorJugador(int idJugador) throws SQLException {
        List<Heroe> lista = new ArrayList<>();
        String sql = "SELECT * FROM personajes WHERE id_jugador = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearHeroe(rs));
        }
        return lista;
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    /** Actualiza los puntos de golpe actuales del héroe (para guardar mid-run). */
    public static void actualizarHp(int idPersonaje, int hpActual) throws SQLException {
        String sql = "UPDATE personajes SET puntos_golpe = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, hpActual));
            ps.setInt(2, idPersonaje);
            ps.executeUpdate();
        }
    }

    // ── Mapeo ResultSet → Heroe ───────────────────────────────────────────────

    private static Heroe mapearHeroe(ResultSet rs) throws SQLException {
        int    id     = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String tipo   = rs.getString("tipo");
        int    hp     = rs.getInt("puntos_golpe");

        Heroe h = switch (tipo) {
            case "MAGO"     -> new Mago(nombre);
            case "GUERRERO" -> new Guerrero(nombre);
            case "CLERIGO"  -> new Clerigo(nombre);
            default -> throw new SQLException("Tipo de héroe desconocido: " + tipo);
        };
        h.setId(id);
        h.setPuntosGolpe(hp);   // restaurar HP guardado (puede ser parcial)
        return h;
    }
}
