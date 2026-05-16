package controlador;

import dao.JugadorDAO;
import dao.PartidaDAO;
import dao.PersonajeDAO;
import modelo.*;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla "Cargar Partida".
 *
 * <p>Recupera de la BD todas las partidas con estado {@code EN_CURSO} y las muestra
 * en una lista vertical. El jugador puede seleccionar cualquiera de ellas para
 * reanudarla.</p>
 *
 * <h3>Flujo al reanudar una partida:</h3>
 * <ol>
 *   <li>Se recuperan los datos del jugador ({@link JugadorDAO#buscarPorId(int)})
 *       y del héroe ({@link PersonajeDAO#buscarPorId(int)}) de la BD.</li>
 *   <li>Se restaura el HP guardado al héroe.</li>
 *   <li>Se crea un {@link GameSession} con la fase y la partida persistida ya cargadas.</li>
 *   <li>Se navega a {@code Mazmorra.fxml} con la sesión inyectada.</li>
 * </ol>
 *
 * <h3>Casos especiales:</h3>
 * <ul>
 *   <li>Si no hay partidas guardadas, se muestra un mensaje informativo.</li>
 *   <li>Si la BD no está disponible, se muestra un aviso de error.</li>
 *   <li>Las entradas con jugador o héroe no encontrado en BD se descartan silenciosamente.</li>
 * </ul>
 */
public class CargarPartidaController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    /** Panel de fondo con las partículas doradas animadas. */
    @FXML private Pane panelParticulas;

    /**
     * Contenedor vertical donde se añaden dinámicamente las filas de partida.
     * Se limpia y rellena en cada llamada a {@link #cargarPartidas()}.
     */
    @FXML private VBox contenedorPartidas;

    /**
     * Label de estado que muestra mensajes cuando no hay partidas o hay un error.
     * Oculto si hay partidas para mostrar.
     */
    @FXML private Label lblEstado;

    /**
     * Llamado automáticamente por JavaFX al cargar el FXML.
     * Genera las partículas de fondo y carga las partidas desde la BD.
     *
     * @param url URL del FXML (no se usa directamente)
     * @param rb  ResourceBundle de localización (no se usa directamente)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
        cargarPartidas();
    }

    /**
     * Recupera todas las partidas {@code EN_CURSO} de la BD y construye una fila
     * visual por cada una.
     *
     * <p>Para cada partida se realizan dos consultas adicionales: una para obtener
     * el jugador y otra para obtener el héroe. Si alguno de los dos no existe en BD,
     * la fila se descarta para evitar mostrar datos incoherentes.</p>
     *
     * <p>Si la lista está vacía, se muestra el mensaje "No hay partidas guardadas."
     * en {@code lblEstado}. Si ocurre un error de conexión, se muestra un aviso.</p>
     */
    private void cargarPartidas() {
        contenedorPartidas.getChildren().clear();
        try {
            List<Partida> partidas = PartidaDAO.listarPartidasActivas();
            if (partidas.isEmpty()) {
                lblEstado.setText("No hay partidas guardadas.");
                lblEstado.setVisible(true);
                return;
            }
            lblEstado.setVisible(false);

            for (Partida p : partidas) {
                Jugador jugador = JugadorDAO.buscarPorId(p.getIdJugador());
                Heroe   heroe   = PersonajeDAO.buscarPorId(p.getIdPersonaje());
                // Descartar entradas huérfanas (datos inconsistentes en BD)
                if (jugador == null || heroe == null) continue;

                HBox fila = crearFilaPartida(p, jugador, heroe);
                contenedorPartidas.getChildren().add(fila);
            }
        } catch (Exception e) {
            lblEstado.setText("⚠ Error al conectar con la base de datos.");
            lblEstado.setVisible(true);
            e.printStackTrace();
        }
    }

    /**
     * Crea un panel horizontal (HBox) con la información de una partida guardada
     * y un botón "▶ Reanudar" para cargarla.
     *
     * <p>El formato del texto informativo es:</p>
     * <pre>Nick  —  Icono Tipo  —  Fase N  —  HP N</pre>
     *
     * <p>El botón llama a {@link #reanudarPartida(Partida, Jugador, Heroe)} al pulsarse.</p>
     *
     * @param p       datos de la partida (fase, HP, estado)
     * @param jugador jugador propietario de la partida
     * @param heroe   héroe guardado en la partida
     * @return HBox listo para añadir al contenedor
     */
    private HBox crearFilaPartida(Partida p, Jugador jugador, Heroe heroe) {
        HBox fila = new HBox(16);
        fila.setStyle("-fx-background-color: #14141f; -fx-border-color: #2e2840; "
                + "-fx-border-width: 1; -fx-border-radius: 2; -fx-background-radius: 2; "
                + "-fx-padding: 12 20; -fx-cursor: hand;");

        Label lblInfo = new Label(String.format("%s  —  %s %s  —  Fase %d  —  HP %d",
                jugador.getNick(), heroe.getIcono(), heroe.getTipo(), p.getFaseActual(), p.getHpActual()));
        lblInfo.setStyle("-fx-font-family: Georgia; -fx-font-size: 14px; -fx-text-fill: #e8e0d0;");

        // Spacer que empuja el botón a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnReanudar = new Button("▶  Reanudar");
        btnReanudar.setStyle("-fx-background-color: #1a1a26; -fx-border-color: #c8a84b; "
                + "-fx-border-width: 1; -fx-text-fill: #f0d070; -fx-font-family: Georgia; "
                + "-fx-cursor: hand; -fx-border-radius: 2; -fx-background-radius: 2;");
        btnReanudar.setOnAction(e -> reanudarPartida(p, jugador, heroe));

        fila.getChildren().addAll(lblInfo, spacer, btnReanudar);
        return fila;
    }

    /**
     * Restaura la sesión de una partida guardada y navega a la pantalla de mazmorra.
     *
     * <p>Pasos:</p>
     * <ol>
     *   <li>Restaura el HP guardado al héroe (el valor en memoria puede diferir del
     *       persistido si el objeto se reutilizó).</li>
     *   <li>Crea un {@link GameSession} y le asigna la fase actual y la partida.</li>
     *   <li>Carga {@code Mazmorra.fxml} e inyecta la sesión en
     *       {@link MazmorraController#iniciarSesion(GameSession)}.</li>
     * </ol>
     *
     * @param partida partida a reanudar (contiene fase, HP y estado)
     * @param jugador jugador propietario
     * @param heroe   héroe con sus stats originales (HP se sobreescribe con el guardado)
     */
    private void reanudarPartida(Partida partida, Jugador jugador, Heroe heroe) {
        // Restaurar el HP guardado al héroe (puede diferir del HP máximo)
        heroe.setPuntosGolpe(partida.getHpActual());

        GameSession sesion = new GameSession(jugador, heroe);
        sesion.setFaseActual(partida.getFaseActual());
        sesion.setPartida(partida);

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/Mazmorra.fxml"));
            Parent root = loader.load();
            MazmorraController siguiente = loader.getController();
            siguiente.iniciarSesion(sesion);
            Stage stage = (Stage) contenedorPartidas.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vuelve al menú principal sin reanudar ninguna partida.
     * Carga {@code MenuPrincipal.fxml} y lo establece como escena activa.
     */
    @FXML
    private void handleVolver() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
            Stage stage = (Stage) contenedorPartidas.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Genera 60 partículas doradas en posiciones aleatorias del fondo y les
     * aplica una animación de parpadeo continuo.
     *
     * <p>Se usa la semilla fija {@code 11} para reproducibilidad visual.
     * Duración de parpadeo aleatoria entre 2 y 5 s, con desfase de 0 a 4 s.</p>
     */
    private void generarParticulas() {
        Random rnd = new Random(11);
        for (int i = 0; i < 60; i++) {
            double x = rnd.nextDouble() * 900, y = rnd.nextDouble() * 650;
            double r = 0.5 + rnd.nextDouble() * 1.2, o = 0.2 + rnd.nextDouble() * 0.5;
            Circle c = new Circle(x, y, r, Color.web("#c8a84b", o));
            FadeTransition ft = new FadeTransition(Duration.seconds(2 + rnd.nextDouble() * 3), c);
            ft.setFromValue(o * 0.3); ft.setToValue(o);
            ft.setAutoReverse(true); ft.setCycleCount(Animation.INDEFINITE);
            ft.setDelay(Duration.seconds(rnd.nextDouble() * 4)); ft.play();
            panelParticulas.getChildren().add(c);
        }
    }
}
