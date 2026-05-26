package controlador;

import controlador.util.Particulas;
import dao.JugadorDAO;
import dao.PersonajeDAO;
import modelo.*;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SeleccionHeroeController implements Initializable { // se ejecuta auto al cargar la pantalla

	/** Panel de fondo con las partículas doradas animadas. */
	@FXML
	private Pane panelParticulas;

	/**
	 * Saludo personalizado con el nombre del jugador.
	 */
	@FXML
	private Label lblSaludo;

	/** Etiqueta que muestra el héroe seleccionado y su habilidad especial. */
	@FXML
	private Label lblSeleccionado;

	/**
	 * Botón principal de confirmación (deshabilitado hasta que se elija un héroe).
	 */
	@FXML
	private Button btnAventura;

	/** Botón para volver a la pantalla de introducción de nombre. */
	@FXML
	private Button btnVolver;

	/** Tarjeta visual del Mago */
	@FXML
	private VBox cardMago;

	/** Tarjeta visual del Guerrero */
	@FXML
	private VBox cardGuerrero;

	/** Tarjeta visual del Clérigo */
	@FXML
	private VBox cardClerigo;

	/** Nombre del jugador */
	private String nombreJugador;

	/** Héroe que el jugador ha seleccionado */
	private Heroe heroeSeleccionado;

	/** Clip de audio que suena al pasar el cursor sobre botones y tarjetas. */
	private AudioClip sonidoHover;

	/**
	 * Llamado automáticamente por JavaFX al cargar el FXML. Inicializa partículas,
	 * animación de entrada, sonido de hover y lo asigna a los tres botones de
	 * acción y a cada tarjeta de héroe.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		Particulas.generar(panelParticulas, 80, 55, 1.5);
		animarEntrada();
		inicializarSonidoHover();
		agregarSonidoHover(btnAventura);
		agregarSonidoHover(btnVolver);
		agregarSonidoHover(cardMago);
		agregarSonidoHover(cardGuerrero);
		agregarSonidoHover(cardClerigo);
	}

	/**
	 * Recibe el nombre del jugador desde la pantalla anterior y actualiza el saludo
	 * en la parte superior de la pantalla.
	 */
	public void setNombreJugador(String nombre) {
		this.nombreJugador = nombre;
		lblSaludo.setText("✦  Aventurero " + nombre + ", elige tu clase  ✦");
	}

	/**
	 * Selecciona al Mago como héroe.
	 */
	@FXML
	private void handleSeleccionarMago() {
		seleccionar(new Mago(nombreJugador), cardMago);
	}

	/**
	 * Selecciona al Guerrero como héroe.
	 */
	@FXML
	private void handleSeleccionarGuerrero() {
		seleccionar(new Guerrero(nombreJugador), cardGuerrero);
	}

	/**
	 * Selecciona al Clérigo como héroe.
	 */
	@FXML
	private void handleSeleccionarClerigo() {
		seleccionar(new Clerigo(nombreJugador), cardClerigo);
	}

	/**
	 * Persiste el jugador y el héroe en la BD, crea la sesión de juego y navega a
	 * la pantalla de mazmorra
	 */
	@FXML
	private void handleAventura() {
		try {
			// Introduce jugador y héroe en BD
			Jugador jugador = JugadorDAO.insertar(new Jugador(nombreJugador));
			PersonajeDAO.insertar(heroeSeleccionado, jugador.getId());

			// Crear sesión de juego
			GameSession sesion = new GameSession(jugador, heroeSeleccionado);

			// Cargar la pantalla de mazmorra
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/Mazmorra.fxml"));
			Parent root = loader.load();

			MazmorraController siguiente = loader.getController();
			siguiente.iniciarSesion(sesion);

			// Cambio de escena
			Stage stage = (Stage) btnAventura.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			mostrarError("Error al conectar con la base de datos: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Vuelve a la pantalla de introducción de nombre sin seleccionar ningún héroe.
	 */
	@FXML
	private void handleVolver() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/Nombre.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) btnAventura.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Marca el héroe elegido como seleccionado, actualiza el resaltado visual de
	 * las tarjetas y habilita el botón de confirmación.
	 */
	private void seleccionar(Heroe heroe, VBox cardSeleccionada) {
		heroeSeleccionado = heroe;

		// Quita resaltado de todas las tarjetas
		List.of(cardMago, cardGuerrero, cardClerigo)
				.forEach(c -> c.getStyleClass().removeAll("card-robot-seleccionada"));

		// Resalta la tarjeta elegida
		cardSeleccionada.getStyleClass().add("card-robot-seleccionada");

		ScaleTransition st = new ScaleTransition(Duration.millis(150), cardSeleccionada);
		st.setToX(1.04);
		st.setToY(1.04);
		st.setAutoReverse(true);
		st.setCycleCount(2);
		st.play();

		// Mostrar información del héroe seleccionado
		String habilidades = heroe.getHabilidades().stream().map(h -> h.getNombre())
				.collect(java.util.stream.Collectors.joining(" · "));
		lblSeleccionado.setText("✔  Has elegido: " + heroe.getIcono() + " " + heroe.getTipo() + "  —  " + habilidades);
		lblSeleccionado.setVisible(true);
		lblSeleccionado.setManaged(true);
		btnAventura.setDisable(false); //el botón se habilita
	}

	/**
	 * Carga el clip de sonido en memoria para reproducirlo al hacer hover sobre los
	 * elementos interactivos.
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
	 * entra en el nodo (botón, tarjeta, etc.).
	 *
	 * @param nodo nodo al que añadir el efecto de sonido
	 */
	private void agregarSonidoHover(Node nodo) {
		nodo.setOnMouseEntered(e -> {
			if (sonidoHover != null) {
				sonidoHover.play();
			}
		});
	}

	/**
	 * Muestra un diálogo de error con el mensaje proporcionado. Se usa para
	 * informar de fallos de conexión con la BD al intentar avanzar.
	 */
	private void mostrarError(String msg) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	/**
	 * Anima la entrada del panel principal. Realiza fade-in de 700ms sobre el panel
	 * central, con un pequeño retraso de 150ms para que las partículas ya estén
	 * visibles cuando aparezca el contenido.
	 */
	private void animarEntrada() {
		Platform.runLater(() -> {
			var panel = btnAventura.getParent().getParent(); // VBox raíz del contenido central
			if (panel == null) {
				return;
			}
			panel.setOpacity(0);
			FadeTransition fade = new FadeTransition(Duration.millis(700), panel);
			fade.setToValue(1);
			fade.setDelay(Duration.millis(150));
			fade.play();
		});
	}
}
