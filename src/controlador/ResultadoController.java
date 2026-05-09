package controlador;

import modelo.GameSession;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de resultado final (victoria o derrota).
 */
public class ResultadoController implements Initializable {

    @FXML private Pane  panelParticulas;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblJugador;
    @FXML private Label lblHeroe;
    @FXML private Label lblPuntos;
    @FXML private Label lblFase;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
    }

    /** Llamado por MazmorraController para inyectar los datos antes de mostrar la pantalla. */
    public void mostrarResultado(GameSession sesion, boolean victoria) {
        if (victoria) {
            lblTitulo.setText("🏆  ¡VICTORIA TOTAL!");
            lblTitulo.setStyle("-fx-text-fill: #f0d070; -fx-font-size: 40px; -fx-font-family: Georgia; " +
                    "-fx-effect: dropshadow(gaussian, rgba(240,208,112,0.8), 30, 0, 0, 0);");
            lblSubtitulo.setText("Has completado la mazmorra y derrotado al Dragón.");
        } else {
            lblTitulo.setText("☠  DERROTA");
            lblTitulo.setStyle("-fx-text-fill: #e05555; -fx-font-size: 40px; -fx-font-family: Georgia; " +
                    "-fx-effect: dropshadow(gaussian, rgba(224,85,85,0.8), 30, 0, 0, 0);");
            lblSubtitulo.setText("Tu héroe ha caído en la oscuridad de la mazmorra.");
        }

        lblJugador.setText("Jugador:  " + sesion.getJugador().getNick());
        lblHeroe.setText("Héroe:    " + sesion.getHeroe().getIcono() + "  " +
                sesion.getHeroe().getNombre() + " (" + sesion.getHeroe().getTipo() + ")");
        lblPuntos.setText("Puntuación:  " + sesion.getJugador().getPuntuacion() + " pts");
        lblFase.setText("Fase alcanzada:  " + sesion.getFaseActual() + " / 4");
    }

    @FXML
    private void handleVolver() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
            Stage stage = (Stage) lblTitulo.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void generarParticulas() {
        Random rnd = new Random(33);
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
