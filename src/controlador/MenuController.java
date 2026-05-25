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
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

	@FXML
	private Pane panelParticulas; // Fondo con partículas doradas animadas
	@FXML
	private Button btnNuevaPartida;
	@FXML
	private Button btnCargarPartida;
	@FXML
	private Button btnRanking;
	@FXML
	private Button btnSalir;

	private MediaPlayer mediaPlayer; 
	
	private AudioClip sonidoHover; 

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		generarParticulas(); // Crear estrellas doradas en el fondo
		animarEntrada(); // Fade-in + slide-up del panel de botones
		iniciarMusica(); // Reproducir Menu.mp3 en bucle
		inicializarSonidoHover(); // Cargar cursor.wav en memoria
		// Asignar el sonido de hover a cada botón del menú
		agregarSonidoHover(btnNuevaPartida);
		agregarSonidoHover(btnCargarPartida);
		agregarSonidoHover(btnRanking);
		agregarSonidoHover(btnSalir);
	}

	// ── Handlers ─────────────────────────────────────────────────────────────

	/**
	 * Navega a la pantalla de introducción de nombre del jugador para iniciar una
	 * nueva partida. Detiene la música de menú antes de navegar.
	 */
	@FXML
	private void handleNuevaPartida() {
		detenerMusica();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/Nombre.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) btnNuevaPartida.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Navega a la pantalla de carga de partida, donde se listan las partidas
	 * EN_CURSO que el jugador puede reanudar. Detiene la música de menú.
	 */
	@FXML
	private void handleCargarPartida() {
		detenerMusica();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/CargarPartida.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) btnCargarPartida.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Navega a la pantalla de ranking. Detiene la música de menú.
	 */
	@FXML
	private void handleRanking() {
		detenerMusica();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/Ranking.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) btnRanking.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Detiene la música y cierra la aplicación limpiamente mediante
	 */
	@FXML
	private void handleSalir() {
		detenerMusica();
		Platform.exit();
	}

	private void generarParticulas() {
		Random rnd = new Random(42);
		for (int i = 0; i < 80; i++) {
			double x = rnd.nextDouble() * 900; // posición X aleatoria
			double y = rnd.nextDouble() * 650; // posición Y aleatoria
			double radio = 0.5 + rnd.nextDouble() * 1.2; // radio entre 0.5 y 1.7 px
			double opacidad = 0.2 + rnd.nextDouble() * 0.5; // opacidad base entre 0.2 y 0.7
			Circle estrella = new Circle(x, y, radio, Color.web("#c8a84b", opacidad));

			// FadeTransition: alterna entre opacidad baja y opacidad base de forma continua
			FadeTransition ft = new FadeTransition(Duration.seconds(1.5 + rnd.nextDouble() * 3), estrella);
			ft.setFromValue(opacidad * 0.3); // parpadeo mínimo
			ft.setToValue(opacidad); // parpadeo máximo
			ft.setAutoReverse(true); // va y vuelve
			ft.setCycleCount(Animation.INDEFINITE); // sin fin
			ft.setDelay(Duration.seconds(rnd.nextDouble() * 4)); // desfase para no sincronizar
			ft.play();

			panelParticulas.getChildren().add(estrella);
		}
	}

	/**
	 * Anima la entrada del panel de botones
	 */
	private void animarEntrada() {
		Platform.runLater(() -> {
			var panel = btnNuevaPartida.getParent(); // VBox que contiene los botones
			if (panel == null) {
				return;
			}
			panel.setOpacity(0); // inicialmente invisible
			panel.setTranslateY(20); // desplazado 20 px hacia abajo

			FadeTransition fade = new FadeTransition(Duration.millis(800), panel);
			fade.setToValue(1); // fundido hasta opacidad total

			TranslateTransition slide = new TranslateTransition(Duration.millis(800), panel);
			slide.setToY(0); // deslizar hasta posición original

			new ParallelTransition(fade, slide).play(); // ambas animaciones simultáneas
		});
	}

	/**
	 * Carga el clip de sonido en memoria para reproducirlo al hacer hover sobre los
	 * botones.
	 */
	private void inicializarSonidoHover() {
		try {
			URL url = getClass().getResource("/recursos/audio/cursor.wav");
			if (url != null) {
				sonidoHover = new AudioClip(url.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Registra un listener para reproducir el sonido de hover cuando el cursor
	 * entra en él.
	 */
	private void agregarSonidoHover(Button btn) {
		btn.setOnMouseEntered(e -> {
			if (sonidoHover != null) {
				sonidoHover.play();
			}
		});
	}

	/**
	 * Inicia la reproducción de la música de fondo del menú en bucle infinito con
	 * volumen al 50 %.
	 */
	private void iniciarMusica() {
		try {
			URL recurso = getClass().getResource("/recursos/audio/Menu.mp3");
			if (recurso == null) {
				System.err.println("No se encontró el archivo de audio Menu.mp3");
				return;
			}
			Media media = new Media(recurso.toExternalForm());
			mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // bucle infinito
			mediaPlayer.setVolume(0.5); // 50 % de volumen
			mediaPlayer.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Detiene la música de fondo si está reproduciéndose. Se llama antes de navegar
	 * a otra pantalla para evitar que la música del menú solape con la música de
	 * combate.
	 */
	private void detenerMusica() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.dispose(); // liberar hilos nativos de audio
			mediaPlayer = null;
		}
	}
}
