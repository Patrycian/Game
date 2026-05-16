package controlador;

import dao.JugadorDAO;
import modelo.Jugador;

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
 * Controlador de la pantalla de ranking (Top 10 jugadores).
 *
 * <p>Recupera de la BD los 10 jugadores con mayor puntuación mediante
 * {@link JugadorDAO#obtenerRanking()} y los muestra en una lista vertical
 * ordenada de mayor a menor. Los tres primeros reciben una medalla (🥇🥈🥉)
 * y el primero tiene un fondo y borde dorado diferenciado.</p>
 *
 * <h3>Aspectos visuales:</h3>
 * <ul>
 *   <li>Fondo con 60 partículas doradas animadas (semilla fija {@code 22}).</li>
 *   <li>Cada fila se construye dinámicamente con {@link #crearFilaRanking}.</li>
 *   <li>Si no hay jugadores o hay error de BD, se muestra un label informativo.</li>
 * </ul>
 */
public class RankingController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    /** Panel de fondo con las partículas doradas animadas. */
    @FXML private javafx.scene.layout.Pane panelParticulas;

    /**
     * Contenedor vertical donde se añaden dinámicamente las filas del ranking.
     * Se limpia y rellena en cada llamada a {@link #cargarRanking()}.
     */
    @FXML private VBox contenedorRanking;

    /**
     * Llamado automáticamente por JavaFX al cargar el FXML.
     * Genera las partículas de fondo y carga el ranking desde la BD.
     *
     * @param url URL del FXML (no se usa directamente)
     * @param rb  ResourceBundle de localización (no se usa directamente)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
        cargarRanking();
    }

    /**
     * Recupera el Top 10 de jugadores desde la BD y construye una fila visual
     * por cada uno, con medalla y puntuación.
     *
     * <p>Los tres primeros puestos muestran medalla emoji (🥇🥈🥉);
     * el resto muestran su número de posición (4., 5., …).</p>
     *
     * <p>Si la BD no tiene jugadores registrados, muestra el mensaje
     * "Aún no hay partidas registradas." en el contenedor. Si ocurre un error
     * de conexión, muestra un aviso de error en rojo.</p>
     */
    private void cargarRanking() {
        contenedorRanking.getChildren().clear();
        try {
            List<Jugador> ranking = JugadorDAO.obtenerRanking();
            if (ranking.isEmpty()) {
                Label lbl = new Label("Aún no hay partidas registradas.");
                lbl.setStyle("-fx-text-fill: #8a7a60; -fx-font-family: Georgia; -fx-font-size: 14px;");
                contenedorRanking.getChildren().add(lbl);
                return;
            }

            String[] medallas = { "🥇", "🥈", "🥉" };
            for (int i = 0; i < ranking.size(); i++) {
                Jugador j = ranking.get(i);
                // Posición: medalla para Top 3, número para el resto
                String medalla = i < 3 ? medallas[i] : (i + 1) + ".";
                HBox fila = crearFilaRanking(medalla, j.getNick(), j.getPuntuacion(), i == 0);
                contenedorRanking.getChildren().add(fila);
            }
        } catch (Exception e) {
            Label lbl = new Label("⚠ Error al conectar con la base de datos.");
            lbl.setStyle("-fx-text-fill: #e05555; -fx-font-family: Georgia;");
            contenedorRanking.getChildren().add(lbl);
            e.printStackTrace();
        }
    }

    /**
     * Crea una fila del ranking con posición, nick y puntuación del jugador.
     *
     * <p>La fila del primer puesto tiene fondo {@code #1c1830} y borde dorado
     * para destacarla visualmente. El resto usa fondo {@code #14141f} y borde
     * más oscuro.</p>
     *
     * <p>Estructura de la fila (izquierda a derecha):</p>
     * <pre>
     *   [posición]   [nick]   [───────spacer───────]   [pts]
     * </pre>
     *
     * @param posicion  medalla emoji o número de posición (p. ej. "🥇" o "4.")
     * @param nick      nombre del jugador
     * @param puntos    puntuación acumulada del jugador
     * @param esPrimero {@code true} si es el jugador en primer lugar (estilo diferenciado)
     * @return HBox con la fila completa lista para añadir al contenedor
     */
    private HBox crearFilaRanking(String posicion, String nick, int puntos, boolean esPrimero) {
        HBox fila = new HBox(20);
        fila.setStyle("-fx-background-color: " + (esPrimero ? "#1c1830" : "#14141f") + "; "
                + "-fx-border-color: " + (esPrimero ? "#c8a84b" : "#2e2840") + "; "
                + "-fx-border-width: 1; -fx-border-radius: 2; -fx-background-radius: 2; "
                + "-fx-padding: 10 20;");
        fila.setPrefWidth(500);

        // Label de posición (medalla o número)
        Label lblPos  = new Label(posicion);
        lblPos.setStyle("-fx-font-family: Georgia; -fx-font-size: 18px; "
                + "-fx-text-fill: #c8a84b; -fx-min-width: 40;");

        // Label de nick del jugador
        Label lblNick = new Label(nick);
        lblNick.setStyle("-fx-font-family: Georgia; -fx-font-size: 16px; "
                + "-fx-text-fill: #e8e0d0; -fx-min-width: 200;");

        // Spacer para empujar la puntuación a la derecha
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Label de puntuación en dorado brillante
        Label lblPts = new Label(puntos + " pts");
        lblPts.setStyle("-fx-font-family: Georgia; -fx-font-size: 16px; "
                + "-fx-font-weight: bold; -fx-text-fill: #f0d070;");

        fila.getChildren().addAll(lblPos, lblNick, spacer, lblPts);
        return fila;
    }

    /**
     * Vuelve al menú principal.
     * Carga {@code MenuPrincipal.fxml} y lo establece como escena activa.
     */
    @FXML
    private void handleVolver() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
            Stage stage = (Stage) contenedorRanking.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Genera 60 partículas doradas en posiciones aleatorias del fondo y les
     * aplica una animación de parpadeo continuo.
     *
     * <p>Se usa la semilla fija {@code 22} para reproducibilidad visual.
     * Duración de parpadeo aleatoria entre 2 y 5 s, con desfase de 0 a 4 s.</p>
     */
    private void generarParticulas() {
        Random rnd = new Random(22);
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
