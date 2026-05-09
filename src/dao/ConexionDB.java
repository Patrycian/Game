package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la conexión JDBC a PostgreSQL.
 * Patrón Singleton: una única conexión por sesión de aplicación.
 *
 * ── Configuración ────────────────────────────────────────────
 *  Edita las constantes URL, USER y PASSWORD con tus datos.
 * ────────────────────────────────────────────────────────────
 */
public class ConexionDB {

    // ── Parámetros de conexión ────────────────────────────────────────────────
    //  Ajusta estos valores si cambias el servidor, usuario o contraseña.
    private static final String URL      = "jdbc:postgresql://localhost:5432/game_db";
    private static final String USER     = "patri_game";
    private static final String PASSWORD = "1";

    private static Connection instancia = null;

    /** Devuelve la conexión, creándola si no existe o está cerrada. */
    public static Connection getConexion() throws SQLException {
        if (instancia == null || instancia.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL no encontrado. "
                        + "Añade postgresql-42.7.11.jar a la carpeta lib/", e);
            }
            instancia = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instancia;
    }

    /** Cierra la conexión si está abierta. Llamar al cerrar la aplicación. */
    public static void cerrar() {
        if (instancia != null) {
            try {
                if (!instancia.isClosed()) instancia.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                instancia = null;
            }
        }
    }

    private ConexionDB() { /* no instanciar */ }
}
