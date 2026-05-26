package controlador;

import controlador.util.Particulas;
import modelo.GameSession;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de resultado final.
 */
public class ResultadoController implements Initializable {

	//Panel de fondo con las partículas doradas animadas.
	@FXML
	private Pane panelParticulas;

	//Título principal
	@FXML
	private Label lblTitulo;

	//Subtítulo descriptivo del desenlace de la partida.
	@FXML
	private Label lblSubtitulo;

	//Muestra el nick del jugador.
	@FXML
	private Label lblJugador;

	// Muestra el icono, nombre y tipo del héroe
	@FXML
	private Label lblHeroe;

	// Muestra la puntuación final acumulada. Formato: "Puntuación: 30 pts"
	@FXML
	private Label lblPuntos;

	//Muestra la última fase alcanzada. Formato: "Fase alcanzada: 3 / 4" */
	@FXML
	private Label lblFase;

	/**
	 * Llamado automáticamente por JavaFX al cargar el FXML. Solo genera las
	 * partículas de fondo, los datos se inyectan después.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		Particulas.generar(panelParticulas, 60, 33, 2);
	}

	/**
	 * Inyecta los datos del resultado final en los labels de la pantalla.
	 */
	public void mostrarResultado(GameSession sesion, boolean victoria) {
		if (victoria) {
			lblTitulo.setText("🏆  ¡VICTORIA!");
			lblTitulo.setStyle("-fx-text-fill: #f0d070; -fx-font-size: 40px; -fx-font-family: Georgia; "
					+ "-fx-effect: dropshadow(gaussian, rgba(240,208,112,0.8), 30, 0, 0, 0);");
			lblSubtitulo.setText("Has completado la mazmorra.");
		} else {
			lblTitulo.setText("☠  DERROTA");
			lblTitulo.setStyle("-fx-text-fill: #e05555; -fx-font-size: 40px; -fx-font-family: Georgia; "
					+ "-fx-effect: dropshadow(gaussian, rgba(224,85,85,0.8), 30, 0, 0, 0);");
			lblSubtitulo.setText("Tu héroe ha caído en la oscuridad de la mazmorra.");
		}

		// Rellenamos los labels de estadísticas
		lblJugador.setText("Jugador:  " + sesion.getJugador().getNick());
		lblHeroe.setText("Héroe:    " + sesion.getHeroe().getIcono() + "  " + sesion.getHeroe().getNombre() + " ("
				+ sesion.getHeroe().getTipo() + ")");
		lblPuntos.setText("Puntuación:  " + sesion.getJugador().getPuntuacion() + " pts");
		lblFase.setText("Fase alcanzada:  " + sesion.getFaseActual() + " / 4");
	}

	/**
	 * Vuelve al menú principal desde la pantalla de resultado.
	 */
	@FXML
	private void handleVolver() {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
			Stage stage = (Stage) lblTitulo.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
