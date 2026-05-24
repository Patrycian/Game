package dao;

import modelo.Partida;
import modelo.Partida.Estado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartidaDAO {

	/** Crea una partida nueva en la BD. */

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
			ps.setString(7, partida.getTipoEnemigo());
			ps.setInt(8, partida.getHpEnemigo());
			ps.setInt(9, partida.getPmEnemigo());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					partida.setId(rs.getInt("id"));
				}
			}
		}
	}

	/** Actualiza todos los campos de una partida existente */

	public static void actualizar(Partida partida) throws SQLException {
		String sql = "UPDATE partidas SET fase_actual = ?, hp_actual = ?, pm_actual = ?, estado = ?, "
				+ "tipo_enemigo = ?, hp_enemigo = ?, pm_enemigo = ?, fecha_guardado = NOW() WHERE id = ?";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
			ps.setInt(1, partida.getFaseActual());
			ps.setInt(2, Math.max(0, partida.getHpActual())); // El HP nunca se guarda como negativo en la BD.
			ps.setInt(3, partida.getPmActual());
			ps.setString(4, partida.getEstado().name());
			ps.setString(5, partida.getTipoEnemigo()); // puede ser null → SQL NULL
			ps.setInt(6, partida.getHpEnemigo());
			ps.setInt(7, partida.getPmEnemigo());
			ps.setInt(8, partida.getId());
			ps.executeUpdate();
		}
	}

	/** Busca la partida más reciente del jugador que siga en curso. */

	public static Partida buscarPartidaActiva(int idJugador) throws SQLException {
		String sql = "SELECT * FROM partidas WHERE id_jugador = ? AND estado = 'EN_CURSO' "
				+ "ORDER BY fecha_guardado DESC LIMIT 1";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
			ps.setInt(1, idJugador);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapear(rs);
				}
			}
		}
		return null;
	}

	/** Devuelve todas las partidas en curso de todos los jugadores. */

	public static List<Partida> listarPartidasActivas() throws SQLException {
		List<Partida> lista = new ArrayList<>();
		// JOIN con jugadores para poder mostrar el nick en la UI si fuera necesario
		String sql = "SELECT p.*, j.nick FROM partidas p " + "JOIN jugadores j ON j.id = p.id_jugador "
				+ "WHERE p.estado = 'EN_CURSO' " + "ORDER BY p.fecha_guardado DESC";
		try (Statement st = ConexionDB.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
			while (rs.next())
				lista.add(mapear(rs));
		}
		return lista;
	}

	/** Método privado que convierte una fila del ResultSet en un objeto Partida. */

	private static Partida mapear(ResultSet rs) throws SQLException {
		return new Partida(rs.getInt("id"), rs.getInt("id_jugador"), rs.getInt("id_personaje"),
				rs.getInt("fase_actual"), rs.getInt("hp_actual"), rs.getInt("pm_actual"),
				Estado.valueOf(rs.getString("estado")), rs.getString("tipo_enemigo"), // puede devolver null si la
																						// columna es NULL
				rs.getInt("hp_enemigo"), rs.getInt("pm_enemigo"));
	}
}
