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
 */
public class RankingController implements Initializable {

    @FXML private javafx.scene.layout.Pane panelParticulas;
    @FXML private VBox contenedorRanking;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
        cargarRanking();
    }

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

    private HBox crearFilaRanking(String posicion, String nick, int puntos, boolean esPrimero) {
        HBox fila = new HBox(20);
        fila.setStyle("-fx-background-color: " + (esPrimero ? "#1c1830" : "#14141f") + "; "
                + "-fx-border-color: " + (esPrimero ? "#c8a84b" : "#2e2840") + "; "
                + "-fx-border-width: 1; -fx-border-radius: 2; -fx-background-radius: 2; "
                + "-fx-padding: 10 20;");
        fila.setPrefWidth(500);

        Label lblPos  = new Label(posicion);
        lblPos.setStyle("-fx-font-family: Georgia; -fx-font-size: 18px; "
                + "-fx-text-fill: #c8a84b; -fx-min-width: 40;");

        Label lblNick = new Label(nick);
        lblNick.setStyle("-fx-font-family: Georgia; -fx-font-size: 16px; "
                + "-fx-text-fill: #e8e0d0; -fx-min-width: 200;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label lblPts = new Label(puntos + " pts");
        lblPts.setStyle("-fx-font-family: Georgia; -fx-font-size: 16px; "
                + "-fx-font-weight: bold; -fx-text-fill: #f0d070;");

        fila.getChildren().addAll(lblPos, lblNick, spacer, lblPts);
        return fila;
    }

    @FXML
    private void handleVolver() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
            Stage stage = (Stage) contenedorRanking.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) { e.printStackTrace(); }
    }

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
