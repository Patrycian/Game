package dao;

import modelo.EnemigoDatos;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Acceso a datos para la tabla {@code enemigos}.
 *
 * Carga el catálogo completo de enemigos una sola vez y lo guarda en un
 * {@code Map} estático (caché). Las llamadas posteriores a
 * {@link #getCatalogo()} devuelven la caché sin tocar la BD.
 *
 * Si la BD no está disponible, el catálogo queda vacío y las clases de
 * enemigo caen al constructor con valores hardcodeados.
 */
public class EnemigoDAO {

    /** Caché: tipo → datos. Se inicializa la primera vez que se llama a getCatalogo(). */
    private static Map<String, EnemigoDatos> catalogo = null;

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Devuelve el catálogo completo de enemigos.
     * La primera llamada consulta la BD; las siguientes usan la caché.
     *
     * @return mapa inmutable tipo → {@link EnemigoDatos}
     */
    public static Map<String, EnemigoDatos> getCatalogo() {
        if (catalogo == null) {
            cargar();
        }
        return catalogo;
    }

    /**
     * Devuelve los datos de un tipo de enemigo concreto, o {@code null}
     * si el tipo no se encuentra en la caché (p. ej. BD no disponible).
     *
     * @param tipo  clave exacta: "OGRO", "GOBLIN", "SAGA" o "DRAGON"
     */
    public static EnemigoDatos getPorTipo(String tipo) {
        return getCatalogo().get(tipo);
    }

    /**
     * Fuerza una recarga desde la BD en la próxima llamada a
     * {@link #getCatalogo()}. Útil si los stats se modifican en caliente.
     */
    public static void invalidarCache() {
        catalogo = null;
    }

    // ── Carga interna ─────────────────────────────────────────────────────────

    private static void cargar() {
        Map<String, EnemigoDatos> mapa = new HashMap<>();
        String sql = "SELECT tipo, nombre, puntos_golpe, defensa, poder, icono, es_jefe "
                   + "FROM enemigos";
        try (Statement st  = ConexionDB.getConexion().createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                EnemigoDatos datos = new EnemigoDatos(
                    rs.getString("tipo"),
                    rs.getString("nombre"),
                    rs.getInt("puntos_golpe"),
                    rs.getInt("defensa"),
                    rs.getInt("poder"),
                    rs.getString("icono"),
                    rs.getBoolean("es_jefe")
                );
                mapa.put(datos.getTipo(), datos);
            }
            System.out.println("[EnemigoDAO] Catálogo cargado: " + mapa.size() + " enemigos.");
        } catch (SQLException e) {
            System.err.println("[EnemigoDAO] No se pudo cargar el catálogo desde BD: " + e.getMessage());
            // La caché queda vacía; las clases de enemigo usarán sus valores por defecto
        }
        catalogo = Collections.unmodifiableMap(mapa);
    }
}
