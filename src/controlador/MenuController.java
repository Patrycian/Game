package controlador;

import javafx.animation.*;
import javafx.application.Platform;
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

public class MenuController implements Initializable {

    @FXML private Pane   panelParticulas;
    @FXML private Button btnNuevaPartida;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnRanking;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
        animarEntrada();
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    @FXML
    private void handleNuevaPartida() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/Nombre.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) btnNuevaPartida.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCargarPartida() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/CargarPartida.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) btnCargarPartida.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRanking() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/Ranking.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) btnRanking.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSalir() {
        Platform.exit();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void mostrarMensaje(String boton) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acción");
        alert.setHeaderText(null);
        alert.setContentText("Ha dado click en: " + boton);

        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(
            getClass().getResource("/application/vistas/estilos.css").toExternalForm()
        );
        dp.getStyleClass().add("dialog-oscuro");
        alert.showAndWait();
    }

    private void generarParticulas() {
        Random rnd = new Random(42);
        for (int i = 0; i < 80; i++) {
            double x        = rnd.nextDouble() * 900;
            double y        = rnd.nextDouble() * 650;
            double radio    = 0.5 + rnd.nextDouble() * 1.2;
            double opacidad = 0.2 + rnd.nextDouble() * 0.5;
            Circle estrella = new Circle(x, y, radio, Color.web("#c8a84b", opacidad));

            FadeTransition ft = new FadeTransition(
                Duration.seconds(1.5 + rnd.nextDouble() * 3), estrella
            );
            ft.setFromValue(opacidad * 0.3);
            ft.setToValue(opacidad);
            ft.setAutoReverse(true);
            ft.setCycleCount(Animation.INDEFINITE);
            ft.setDelay(Duration.seconds(rnd.nextDouble() * 4));
            ft.play();

            panelParticulas.getChildren().add(estrella);
        }
    }

    private void animarEntrada() {
        Platform.runLater(() -> {
            var panel = btnNuevaPartida.getParent();
            if (panel == null) return;
            panel.setOpacity(0);
            panel.setTranslateY(20);
            FadeTransition fade = new FadeTransition(Duration.millis(800), panel);
            fade.setToValue(1);
            TranslateTransition slide = new TranslateTransition(Duration.millis(800), panel);
            slide.setToY(0);
            new ParallelTransition(fade, slide).play();
        });
    }
}
