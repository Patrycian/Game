package dao;

import modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code personajes}.
 *
 * <p>Gestiona la persistencia de los héroes del jugador. Cada héroe tiene
 * una fila en esta tabla con sus stats base y su HP actual (que se actualiza
 * al guardar la partida).</p>
 *
 * <p>Esquema de la tabla:</p>
 * <pre>
 *   personajes (
 *     id           SERIAL PRIMARY KEY,
 *     nombre       VARCHAR(50),
 *     tipo         VARCHAR(20),   -- 'MAGO' | 'GUERRERO' | 'CLERIGO'
 *     puntos_golpe INT,           -- HP actual (se actualiza mid-run)
 *     defensa      INT,
 *     poder        INT,
 *     id_jugador   INT REFERENCES jugadores(id)
 *   )
 * </pre>
 */
public class PersonajeDAO {

    // ── Insertar ──────────────────────────────────────────────────────────────

    /**
     * Persiste un héroe nuevo en la BD y le asigna el id generado.
     *
     * <p>Se llama desde {@code SeleccionHeroeController} justo antes de iniciar
     * la primera fase de combate.</p>
     *
     * @param heroe     héroe a guardar (su id se actualizará a lo que devuelva BD)
     * @param idJugador id del jugador propietario (FK → jugadores.id)
     * @throws SQLException si ocurre un error de acceso a la BD
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
                if (rs.next()) heroe.setId(rs.getInt("id"));
            }
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Recupera un héroe por su id de BD y lo reconstruye como objeto Java.
     * Se usa al reanudar una partida guardada.
     *
     * @param id id de la tabla {@code personajes}
     * @return el {@link Heroe} correspondiente con HP restaurado al valor guardado,
     *         o {@code null} si el id no existe
     * @throws SQLException si ocurre un error de acceso a la BD, o si el tipo
     *                      del personaje guardado no es reconocido
     */
    public static Heroe buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM personajes WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearHeroe(rs);
            }
        }
        return null;
    }

    /**
     * Devuelve todos los héroes asociados a un jugador concreto.
     * Útil para mostrar el historial de personajes de un jugador.
     *
     * @param idJugador id del jugador (FK → jugadores.id)
     * @return lista de {@link Heroe} del jugador (puede estar vacía)
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static List<Heroe> buscarPorJugador(int idJugador) throws SQLException {
        List<Heroe> lista = new ArrayList<>();
        String sql = "SELECT * FROM personajes WHERE id_jugador = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearHeroe(rs));
            }
        }
        return lista;
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    /**
     * Actualiza los puntos de golpe actuales del héroe en BD.
     * Se llama al guardar la partida (entre fases o al huir) para que el HP
     * persista y pueda restaurarse al reanudar.
     *
     * @param idPersonaje id del personaje en la tabla {@code personajes}
     * @param hpActual    HP a guardar (se clampea a 0 como mínimo)
     * @throws SQLException si ocurre un error de acceso a la BD
     */
    public static void actualizarHp(int idPersonaje, int hpActual) throws SQLException {
        String sql = "UPDATE personajes SET puntos_golpe = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, hpActual));
            ps.setInt(2, idPersonaje);
            ps.executeUpdate();
        }
    }

    // ── Mapeo ResultSet → Heroe ───────────────────────────────────────────────

    /**
     * Convierte una fila del {@link ResultSet} en el objeto {@link Heroe} concreto.
     *
     * <p>La clase concreta (Mago, Guerrero, Clérigo) se determina según la columna
     * {@code tipo}. El HP se inicializa al valor guardado en BD mediante
     * {@link Personaje#setPuntosGolpe}, que puede ser menor que el máximo si el
     * héroe tomó daño antes de guardar.</p>
     *
     * @param rs fila activa del ResultSet, ya posicionada con {@code rs.next()}
     * @return objeto {@link Heroe} listo para usar en combate
     * @throws SQLException si el tipo del personaje no coincide con ninguna clase conocida,
     *                      o si la BD devuelve datos inválidos
     */
    private static Heroe mapearHeroe(ResultSet rs) throws SQLException {
        int    id     = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String tipo   = rs.getString("tipo");
        int    hp     = rs.getInt("puntos_golpe"); // HP guardado (puede ser parcial)

        // Crear la subclase correcta según el tipo almacenado en BD
        Heroe h = switch (tipo) {
            case "MAGO"     -> new Mago(nombre);
            case "GUERRERO" -> new Guerrero(nombre);
            case "CLERIGO"  -> new Clerigo(nombre);
            default -> throw new SQLException("Tipo de héroe desconocido: " + tipo);
        };

        h.setId(id);
        h.setPuntosGolpe(hp); // restaurar HP guardado (puede ser parcial si se guardó mid-run)
        return h;
    }
}
