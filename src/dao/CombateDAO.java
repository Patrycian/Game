package dao;

import java.sql.*;

/**
 * Acceso a datos para la tabla {@code combates}.
 * Registra el historial de cada enfrentamiento de una partida.
 */
public class CombateDAO {

    /**
     * Registra el resultado de un combate.
     *
     * @param idPartida    id de la partida en curso
     * @param fase         número de fase (1-4)
     * @param tipoEnemigo  "OGRO" | "GOBLIN" | "SAGA" | "DRAGON"
     * @param victoria     true si el héroe venció
     * @param turnos       número de turnos que duró el combate
     */
    public static void registrar(int idPartida, int fase,
                                  String tipoEnemigo, boolean victoria,
                                  int turnos) throws SQLException {
        String sql = "INSERT INTO combates (id_partida, fase, tipo_enemigo, resultado, turnos) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPartida);
            ps.setInt(2, fase);
            ps.setString(3, tipoEnemigo.toUpperCase());
            ps.setString(4, victoria ? "VICTORIA" : "DERROTA");
            ps.setInt(5, turnos);
            ps.executeUpdate();
        }
    }
}
