package dao;

import modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonajeDAO {

	/**
	 * Guarda un héroe nuevo en la BD vinculado a un jugador concreto mediante
	 * id_jugador.
	 */

	public static void insertar(Heroe heroe, int idJugador) throws SQLException {
		String sql = "INSERT INTO personajes (nombre, tipo, puntos_golpe, defensa, poder, id_jugador) "
				+ "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
			ps.setString(1, heroe.getNombre());
			ps.setString(2, heroe.getTipo());
			ps.setInt(3, heroe.getPuntosGolpeMax()); // se guarda el HP máximo al inicio
			ps.setInt(4, heroe.getDefensa());
			ps.setInt(5, heroe.getPoder());
			ps.setInt(6, idJugador);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					heroe.setId(rs.getInt("id"));
				}
			}
		}
	}

	/** Busca un héroe concreto por su id. Se usa al cargar una partida guardada. */

	public static Heroe buscarPorId(int id) throws SQLException {
		String sql = "SELECT * FROM personajes WHERE id = ?";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapearHeroe(rs);
				}
			}
		}
		return null;
	}

	/** Devuelve todos los héroes que pertenecen a un jugador. */

	public static List<Heroe> buscarPorJugador(int idJugador) throws SQLException {
		List<Heroe> lista = new ArrayList<>();
		String sql = "SELECT * FROM personajes WHERE id_jugador = ?";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
			ps.setInt(1, idJugador);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					lista.add(mapearHeroe(rs));
			}
		}
		return lista;
	}

	/**
	 * Actualiza solo el HP del personaje, sin tocar el resto de campos. Se llama
	 * durante la partida cada vez que el héroe recibe daño o se cura.
	 */

	public static void actualizarHp(int idPersonaje, int hpActual) throws SQLException {
		String sql = "UPDATE personajes SET puntos_golpe = ? WHERE id = ?";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
			ps.setInt(1, Math.max(0, hpActual));
			ps.setInt(2, idPersonaje);
			ps.executeUpdate();
		}
	}
	
	/** Puente entre el modelo relacional y el modelo orientado a objetos*/

	private static Heroe mapearHeroe(ResultSet rs) throws SQLException {
		int id = rs.getInt("id");
		String nombre = rs.getString("nombre");
		String tipo = rs.getString("tipo");
		int hp = rs.getInt("puntos_golpe"); // HP guardado (puede ser parcial)

		// Creamos la subclase correcta según el tipo almacenado en BD
		Heroe h = switch (tipo) {
		case "MAGO" -> new Mago(nombre);
		case "GUERRERO" -> new Guerrero(nombre);
		case "CLERIGO" -> new Clerigo(nombre);
		default -> throw new SQLException("Tipo de héroe desconocido: " + tipo);
		};

		/*
		 * Una vez creada la subclase correcta se le asigna el id de BD y el HP
		 * guardado, restaurando el estado exacto en que estaba cuando se guardó la
		 * partida.
		 */

		h.setId(id);
		h.setPuntosGolpe(hp);
		return h;
	}
}
