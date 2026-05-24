package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    //Parámetros de conexión 
	
    private static final String URL      = "jdbc:postgresql://localhost:5432/game_db";
    private static final String USER     = "patri_game"; 
    private static final String PASSWORD = "1";

    private static Connection instancia = null;

    /**
     * Devuelve la conexión, creándola o reconectando si es necesario.
     *
     * Además de comprobar isClosed(), valida la conexión con
     * isValid(2) para detectar conexiones zombie que el servidor
     * PostgreSQL cerró silenciosamente (ejemplo: por timeout de inactividad)
     * pero que Java aún considera abiertas. Sin esta comprobación los
     * guardados fallan silenciosamente si la app lleva tiempo en reposo.
     */
    public static Connection getConexion() throws SQLException {
        if (instancia == null || instancia.isClosed() || !instancia.isValid(2)) { //2: tiempo en segundos que espera para comprobar
            try {
                Class.forName("org.postgresql.Driver");//carga el driver de Postgre
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL no encontrado.", e);
            }
            
            /*Creamos la conexión real con la base de datos usando las variables definidas arriba 
             * y la guardamos en instancia*/
            instancia = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instancia;
    }

    /** Cierra la conexión de forma segura al cerrar la aplicación*/
    public static void cerrar() {
        if (instancia != null) {
            try {
                if (!instancia.isClosed()) { instancia.close(); }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally { //garantiza que instancia quede como null aunque el close() falle
                instancia = null;
            }
        }
    }
    
    /**Constructor privado que impide hacer new ConexionDB() desde fuera.*/
    private ConexionDB() { 
    	
    }
}
