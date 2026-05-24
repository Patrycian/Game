package dao;

import modelo.Jugador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JugadorDAO {

	/**
	 * Recibe un objeto Jugador y lo guarda en la BD. Devuelve el jugador, ya sea el
	 * recién insertado o uno existente.
	 */

	public static Jugador insertar(Jugador jugador) throws SQLException {
		// Comprobar si el nick ya existe para evitar duplicados
		Jugador existente = buscarPorNick(jugador.getNick());
		if (existente != null) {
			return existente;
		}
		/*
		 * Inserta el nuevo jugador y con RETURNING id le pide a PostgreSQL que devuelva
		 * el id que generó automáticamente. Sintaxis propia de PostgreSQL.
		 */
		String sql = "INSERT INTO jugadores (nick, puntuacion) VALUES (?, ?) RETURNING id";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
			ps.setString(1, jugador.getNick());
			ps.setInt(2, jugador.getPuntuacion());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					jugador.setId(rs.getInt("id"));
				}
			}
		}
		return jugador;
	}

	/**
	 * Busca un jugador por su nick exacto. Si lo encuentra construye y devuelve un
	 * objeto Jugador con sus datos.
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
	 * Busca un jugador por su ID.
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

	/** Devuelve los 10 mejores jugadores ordenados de mayor a menor puntuación. */
	public static List<Jugador> obtenerRanking() throws SQLException {
		List<Jugador> lista = new ArrayList<>();
		String sql = "SELECT id, nick, puntuacion FROM jugadores ORDER BY puntuacion DESC LIMIT 10";
		try (Statement st = ConexionDB.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) { // múltiples filas
				lista.add(new Jugador(rs.getInt("id"), rs.getString("nick"), rs.getInt("puntuacion")));
			}
		}
		return lista;
	}

	/**
	 * Actualiza la puntuación en la BD usando el id como criterio. Se llama cada vez que el héroe gana un combate.
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
