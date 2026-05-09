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

public class NombreController implements Initializable {

    @FXML private Pane      panelParticulas;
    @FXML private TextField txtNombre;
    @FXML private Label     lblError;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
        animarEntrada();
        txtNombre.setOnAction(e -> handleContinuar());
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    @FXML
    private void handleContinuar() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("⚠  El nombre no puede estar vacío.");
            sacudirCampo();
            return;
        }
        if (nombre.length() < 2) {
            mostrarError("⚠  El nombre debe tener al menos 2 caracteres.");
            sacudirCampo();
            return;
        }

        navegarASeleccionRobot(nombre);
    }

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/MenuPrincipal.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    private void navegarASeleccionRobot(String nombre) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/SeleccionHeroe.fxml")
            );
            Parent root = loader.load();

            SeleccionHeroeController siguiente = loader.getController();
            siguiente.setNombreJugador(nombre);

            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void sacudirCampo() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), txtNombre);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> txtNombre.setTranslateX(0));
        tt.play();
    }

    private void generarParticulas() {
        Random rnd = new Random(99);
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
            var panel = txtNombre.getParent();
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
