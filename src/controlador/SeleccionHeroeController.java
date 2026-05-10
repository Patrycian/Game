package controlador;

import dao.JugadorDAO;
import dao.PersonajeDAO;
import modelo.*;

import javafx.animation.*;
import javafx.application.Platform;
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
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de selección de héroe.
 * El jugador elige entre Mago, Guerrero o Clérigo antes de entrar a la mazmorra.
 */
public class SeleccionHeroeController implements Initializable {

    @FXML private Pane   panelParticulas;
    @FXML private Label  lblSaludo;
    @FXML private Label  lblSeleccionado;
    @FXML private Button btnAventura;
    @FXML private Button btnVolver;
    @FXML private VBox   cardMago;
    @FXML private VBox   cardGuerrero;
    @FXML private VBox   cardClerigo;

    private String    nombreJugador;
    private Heroe     heroeSeleccionado;
    private AudioClip sonidoHover;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
        animarEntrada();
        inicializarSonidoHover();
        agregarSonidoHover(btnAventura);
        agregarSonidoHover(btnVolver);
        cardMago.setOnMouseEntered(e     -> { if (sonidoHover != null) sonidoHover.play(); });
        cardGuerrero.setOnMouseEntered(e -> { if (sonidoHover != null) sonidoHover.play(); });
        cardClerigo.setOnMouseEntered(e  -> { if (sonidoHover != null) sonidoHover.play(); });
    }

    public void setNombreJugador(String nombre) {
        this.nombreJugador = nombre;
        lblSaludo.setText("✦  Aventurero " + nombre + ", elige tu clase  ✦");
    }

    // ── Handlers de selección ─────────────────────────────────────────────────

    @FXML private void handleSeleccionarMago()     { seleccionar(new Mago(nombreJugador),     cardMago);     }
    @FXML private void handleSeleccionarGuerrero() { seleccionar(new Guerrero(nombreJugador), cardGuerrero); }
    @FXML private void handleSeleccionarClerigo()  { seleccionar(new Clerigo(nombreJugador),  cardClerigo);  }

    @FXML
    private void handleAventura() {
        try {
            // 1. Persistir jugador y héroe en BD
            Jugador jugador = JugadorDAO.insertar(new Jugador(nombreJugador));
            PersonajeDAO.insertar(heroeSeleccionado, jugador.getId());

            // 2. Crear sesión de juego
            GameSession sesion = new GameSession(jugador, heroeSeleccionado);

            // 3. Navegar a la pantalla de mazmorra (fase 1)
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/Mazmorra.fxml")
            );
            Parent root = loader.load();

            MazmorraController siguiente = loader.getController();
            siguiente.iniciarSesion(sesion);

            Stage stage = (Stage) btnAventura.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            mostrarError("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/Nombre.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) btnAventura.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Lógica visual ─────────────────────────────────────────────────────────

    private void seleccionar(Heroe heroe, VBox cardSeleccionada) {
        heroeSeleccionado = heroe;

        List.of(cardMago, cardGuerrero, cardClerigo)
            .forEach(c -> c.getStyleClass().removeAll("card-robot-seleccionada"));

        cardSeleccionada.getStyleClass().add("card-robot-seleccionada");

        ScaleTransition st = new ScaleTransition(Duration.millis(150), cardSeleccionada);
        st.setToX(1.04);
        st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        lblSeleccionado.setText("✔  Has elegido: " + heroe.getIcono() + " " + heroe.getTipo()
                + "  —  " + heroe.getNombreHabilidad());
        lblSeleccionado.setVisible(true);
        lblSeleccionado.setManaged(true);
        btnAventura.setDisable(false);
    }

    private void inicializarSonidoHover() {
        try {
            URL url = getClass().getResource("/recursos/audio/cursor.wav");
            if (url != null) sonidoHover = new AudioClip(url.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void agregarSonidoHover(Button btn) {
        btn.setOnMouseEntered(e -> {
            if (sonidoHover != null) sonidoHover.play();
        });
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ── Animaciones y partículas ──────────────────────────────────────────────

    private void generarParticulas() {
        Random rnd = new Random(55);
        for (int i = 0; i < 80; i++) {
            double x = rnd.nextDouble() * 900, y = rnd.nextDouble() * 650;
            double r = 0.5 + rnd.nextDouble() * 1.2, o = 0.2 + rnd.nextDouble() * 0.5;
            Circle c = new Circle(x, y, r, Color.web("#c8a84b", o));
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5 + rnd.nextDouble() * 3), c);
            ft.setFromValue(o * 0.3); ft.setToValue(o);
            ft.setAutoReverse(true); ft.setCycleCount(Animation.INDEFINITE);
            ft.setDelay(Duration.seconds(rnd.nextDouble() * 4)); ft.play();
            panelParticulas.getChildren().add(c);
        }
    }

    private void animarEntrada() {
        Platform.runLater(() -> {
            var panel = btnAventura.getParent().getParent();
            if (panel == null) return;
            panel.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(700), panel);
            fade.setToValue(1); fade.setDelay(Duration.millis(150)); fade.play();
        });
    }
}
