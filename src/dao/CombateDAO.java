package dao;

import java.sql.*;

public class CombateDAO {

	/**
	 * Método que registra en BD el resultado de un combate.
	 */
	public static void registrar(int idPartida, int fase, String tipoEnemigo, boolean victoria, int turnos)
			throws SQLException {
		String sql = "INSERT INTO combates (id_partida, fase, tipo_enemigo, resultado, turnos) " // consulta SQL
				+ "VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) { // abrimos conexión con BD
			ps.setInt(1, idPartida); // Sustituye el primer ? con el valor de idPartid
			ps.setInt(2, fase);// Sustituye el segundo ?...y así con los siguientes
			ps.setString(3, tipoEnemigo.toUpperCase());
			ps.setString(4, victoria ? "VICTORIA" : "DERROTA"); //usamos operador ternario
			ps.setInt(5, turnos);
			ps.executeUpdate(); 
			
			/*Usamos executeUpdate en lugar de executeQuery porque es una operación de
			 * escritura (INSERT), no de lectura */
		}
	}
}
