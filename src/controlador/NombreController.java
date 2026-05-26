package controlador;

import controlador.util.Particulas;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class NombreController implements Initializable {

	/**
	 * Contenedor raíz de la pantalla; se usa para calcular posiciones relativas del
	 * teclado.
	 */
	@FXML
	private StackPane rootPane;

	@FXML
	private Pane panelParticulas;

	/** Campo de texto donde el jugador escribe su nombre. */
	@FXML
	private TextField txtNombre;

	@FXML
	private Label lblError;

	private VBox panelTeclado;

	private boolean tecladoVisible = false;

	/**
	 * Distribución de teclas del teclado virtual QWERTY.
	 */
	private static final String[][] FILAS_TECLADO = { { "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P" },
			{ "A", "S", "D", "F", "G", "H", "J", "K", "L" }, { "Z", "X", "C", "V", "B", "N", "M", "⌫" },
			{ "ESPACIO", "ENTER" } };

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		Particulas.generar(panelParticulas, 80, 99, 1.5);
		animarEntrada();
		// Al pulsar Enter en el TextField, intenta continuar sin abrir teclado
		txtNombre.setOnAction(e -> handleContinuar());
		// Al hacer clic en el TextField, despliega el teclado virtual
		txtNombre.setOnMouseClicked(e -> mostrarTeclado());

		construirTeclado();

		// Registrar el filtro de click externo una vez que la Scene esté disponible
		Platform.runLater(() -> {
			Scene scene = rootPane.getScene();
			if (scene != null) {
				scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
					if (!tecladoVisible) {
						return;
					}

					// Convertir coordenadas de escena al espacio local del rootPane
					var puntoLocal = rootPane.sceneToLocal(e.getSceneX(), e.getSceneY());
					boolean enTeclado = panelTeclado.getBoundsInParent().contains(puntoLocal);
					boolean enCampo = txtNombre.localToScene(txtNombre.getBoundsInLocal()).contains(e.getSceneX(),
							e.getSceneY());

					// Si el click fue fuera del teclado y fuera del campo, ocultar teclado
					if (!enTeclado && !enCampo) {
						ocultarTeclado();
					}
				});
			}
		});
	}

	// ── Teclado virtual ───────────────────────────────────────────────────────

	private void construirTeclado() {
		panelTeclado = new VBox(3);
		panelTeclado.getStyleClass().add("panel-teclado");
		panelTeclado.setAlignment(Pos.CENTER);
		panelTeclado.setPadding(new Insets(8, 10, 9, 10));
		panelTeclado.setMaxWidth(460);

		// Estado inicial: invisible y desplazado levemente hacia abajo
		panelTeclado.setOpacity(0);
		panelTeclado.setTranslateY(20);
		panelTeclado.setMouseTransparent(true);

		for (String[] fila : FILAS_TECLADO) {
			HBox filaBox = new HBox(3);
			filaBox.setAlignment(Pos.CENTER);
			for (String tecla : fila) {
				filaBox.getChildren().add(crearBotonTecla(tecla));
			}
			panelTeclado.getChildren().add(filaBox);
		}

		// Posición inicial: se actualizará dinámicamente en mostrarTeclado()
		StackPane.setAlignment(panelTeclado, Pos.TOP_CENTER);
		StackPane.setMargin(panelTeclado, new Insets(0, 0, 0, 0));
		rootPane.getChildren().add(panelTeclado);
	}

	private Button crearBotonTecla(String tecla) {
		Button btn = new Button(tecla);
		btn.setFocusTraversable(false); // evita que el teclado robe el foco del TextField

		switch (tecla) {
		case "⌫":
			btn.getStyleClass().add("btn-tecla-borrar");
			break;
		case "ESPACIO":
			btn.getStyleClass().add("btn-tecla-espacio");
			break;
		case "ENTER":
			btn.getStyleClass().add("btn-tecla-enter");
			break;
		default:
			btn.getStyleClass().add("btn-tecla");
			break;
		}

		btn.setOnAction(e -> {
			switch (tecla) {
			case "⌫": {
				// Borrar el carácter justo antes del cursor (si no estamos al principio)
				int caret = txtNombre.getCaretPosition();
				if (caret > 0) {
					txtNombre.deleteText(caret - 1, caret);
				}
				break;
			}
			case "ESPACIO":
				txtNombre.insertText(txtNombre.getCaretPosition(), " ");
				break;
			case "ENTER":
				handleContinuar();
				return; // no hace falta re-enfocar, handleContinuar navega o sacude
			default:
				txtNombre.insertText(txtNombre.getCaretPosition(), tecla);
				break;
			}
			lblError.setVisible(false);
			// Devolver el foco al TextField tras pulsar una tecla
			Platform.runLater(() -> txtNombre.requestFocus());
		});

		return btn;
	}

	/**
	 * Muestra el teclado virtual
	 */
	private void mostrarTeclado() {
		if (tecladoVisible) {
			return;
		}
		tecladoVisible = true;
		panelTeclado.setMouseTransparent(false);

		// Calcular margen dinámico: justo debajo del TextField (6 px de separación)
		var campoEnEscena = txtNombre.localToScene(txtNombre.getBoundsInLocal());
		var rootEnEscena = rootPane.localToScene(rootPane.getBoundsInLocal());
		double margenTop = campoEnEscena.getMaxY() - rootEnEscena.getMinY() + 6;
		StackPane.setAlignment(panelTeclado, Pos.TOP_CENTER);
		StackPane.setMargin(panelTeclado, new Insets(margenTop, 0, 0, 0));

		FadeTransition fade = new FadeTransition(Duration.millis(220), panelTeclado);
		fade.setToValue(1);
		TranslateTransition slide = new TranslateTransition(Duration.millis(220), panelTeclado);
		slide.setToY(0);
		new ParallelTransition(fade, slide).play();
	}

	/**
	 * Oculta el teclado virtual
	 */
	private void ocultarTeclado() {
		if (!tecladoVisible) {
			return;
		}
		tecladoVisible = false;
		panelTeclado.setMouseTransparent(true);

		FadeTransition fade = new FadeTransition(Duration.millis(180), panelTeclado);
		fade.setToValue(0);
		TranslateTransition slide = new TranslateTransition(Duration.millis(180), panelTeclado);
		slide.setToY(20);
		new ParallelTransition(fade, slide).play();
	}

	/**
	 * Valida el nombre introducido y, si es correcto, navega a la pantalla de
	 * selección de héroe.
	 */
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

	/**
	 * Vuelve al menú principal sin guardar ningún nombre.
	 */
	@FXML
	private void handleVolver() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) txtNombre.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Carga la pantalla de selección de héroe, le pasa el nombre del jugador y la
	 * establece como escena activa.
	 */
	private void navegarASeleccionRobot(String nombre) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/SeleccionHeroe.fxml"));
			Parent root = loader.load();

			SeleccionHeroeController siguiente = loader.getController();
			siguiente.setNombreJugador(nombre);

			Stage stage = (Stage) txtNombre.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Muestra el mensaje de error en lblError y lo hace visible.
	 */
	private void mostrarError(String mensaje) {
		lblError.setText(mensaje);
		lblError.setVisible(true);
	}

	/**
	 * Aplica una animación de sacudida horizontal al campo de texto para señalar
	 * visualmente que ha fallado la validación.
	 */
	private void sacudirCampo() {
		TranslateTransition tt = new TranslateTransition(Duration.millis(60), txtNombre);
		tt.setFromX(0);
		tt.setByX(8); // desplazamiento máximo en píxeles
		tt.setCycleCount(6); // número de oscilaciones
		tt.setAutoReverse(true);
		tt.setOnFinished(e -> txtNombre.setTranslateX(0)); // restablecer posición exacta
		tt.play();
	}

	/**
	 * Anima la entrada del panel central con fade-in + slide-up.
	 */
	private void animarEntrada() {
		Platform.runLater(() -> {
			var panel = txtNombre.getParent(); // VBox o contenedor del campo y los botones
			if (panel == null) {
				return;
			}
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
