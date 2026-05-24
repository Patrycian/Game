package dao;

import modelo.EnemigoDatos;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnemigoDAO {

	private static Map<String, EnemigoDatos> catalogo = null;

	/**
	 * Devuelve el catálogo completo de enemigos. La primera llamada consulta la BD;
	 * solo consulta la base de datos la primera vez que alguien pide el catálogo.
	 * Las llamadas siguientes devuelven directamente lo que ya está en memoria sin
	 * tocar la BD.
	 */

	public static Map<String, EnemigoDatos> getCatalogo() {
		if (catalogo == null) {
			cargar();
		}
		return catalogo;
	}

	/**
	 * Atajo para obtener un enemigo concreto sin tener que obtener el mapa
	 * completo.
	 */
	public static EnemigoDatos getPorTipo(String tipo) {
		return getCatalogo().get(tipo);
	}

	/** Fuerza que la próxima llamada a getCatalogo() vuelva a consultar la BD. */

	public static void invalidarCache() {
		catalogo = null;
	}

	// Carga interna

	private static void cargar() {
		Map<String, EnemigoDatos> mapa = new HashMap<>();
		String sql = "SELECT tipo, nombre, puntos_golpe, defensa, poder, icono, es_jefe " + "FROM enemigos";
		try (Statement st = ConexionDB.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) { // Itera fila a fila por los resultados.
				EnemigoDatos datos = new EnemigoDatos(rs.getString("tipo"), rs.getString("nombre"),
						rs.getInt("puntos_golpe"), rs.getInt("defensa"), rs.getInt("poder"), rs.getString("icono"),
						rs.getBoolean("es_jefe"));
				mapa.put(datos.getTipo(), datos); // Añade el enemigo al mapa usando su tipo como clave
			}
			System.out.println("[EnemigoDAO] Catálogo cargado: " + mapa.size() + " enemigos.");
		} catch (SQLException e) {
			/*
			 * Si la consulta falla, en lugar de lanzar la excepción hacia arriba
			 * simplemente la registra en consola. El mapa queda vacío pero la aplicación no
			 * se rompe, los enemigos usan sus valores por defecto definidos en sus propias
			 * clases.
			 */
			System.err.println("[EnemigoDAO] No se pudo cargar el catálogo desde BD: " + e.getMessage());
		}
		/*
		 * Convierte el mapa en inmutable antes de asignarlo a catalogo. Nadie fuera de
		 * esta clase puede modificarlo.
		 */
		catalogo = Collections.unmodifiableMap(mapa);
	}
}
