package controlador;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de introducción de nombre del jugador.
 *
 * <p>Es la segunda pantalla del flujo, a la que se llega desde el menú principal
 * al pulsar "Nueva Partida". El jugador escribe su nombre y confirma para avanzar
 * a la selección de héroe.</p>
 *
 * <h3>Características de esta pantalla:</h3>
 * <ul>
 *   <li><b>TextField con validación</b>: el nombre no puede estar vacío ni tener
 *       menos de 2 caracteres; en caso de error se muestra un label de aviso y
 *       el campo se sacude horizontalmente.</li>
 *   <li><b>Teclado virtual</b>: se despliega automáticamente al hacer clic en el
 *       campo de texto, permitiendo escribir con el ratón (útil en pantallas táctiles
 *       o si no se dispone de teclado físico). Se cierra haciendo clic fuera.</li>
 *   <li><b>Partículas de fondo</b>: 80 estrellas doradas con parpadeo continuo,
 *       generadas con semilla fija {@code 99} para reproducibilidad visual.</li>
 *   <li><b>Animación de entrada</b>: el panel central aparece con fade-in + slide-up
 *       en 800 ms.</li>
 * </ul>
 *
 * <p>Al confirmar el nombre, navega a {@code SeleccionHeroe.fxml} pasando el nombre
 * mediante {@link SeleccionHeroeController#setNombreJugador(String)}.</p>
 */
public class NombreController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    /** Contenedor raíz de la pantalla; se usa para calcular posiciones relativas del teclado. */
    @FXML private StackPane rootPane;

    /** Panel de fondo que contiene las partículas doradas animadas. */
    @FXML private Pane      panelParticulas;

    /** Campo de texto donde el jugador escribe su nombre. */
    @FXML private TextField txtNombre;

    /** Etiqueta de error que se muestra cuando la validación falla. Oculta por defecto. */
    @FXML private Label     lblError;

    // ── Teclado virtual ───────────────────────────────────────────────────────
    /** Panel VBox que contiene todas las filas del teclado virtual. Se añade al rootPane dinámicamente. */
    private VBox    panelTeclado;

    /** Indica si el teclado virtual está actualmente visible en pantalla. */
    private boolean tecladoVisible = false;

    /**
     * Distribución de teclas del teclado virtual QWERTY.
     * Cada elemento del array externo es una fila de teclas.
     * Teclas especiales: "⌫" (borrar), "ESPACIO" y "ENTER".
     */
    private static final String[][] FILAS_TECLADO = {
        {"Q","W","E","R","T","Y","U","I","O","P"},
        {"A","S","D","F","G","H","J","K","L"},
        {"Z","X","C","V","B","N","M","⌫"},
        {"ESPACIO","ENTER"}
    };

    // ── Inicialización ────────────────────────────────────────────────────────

    /**
     * Llamado automáticamente por JavaFX tras cargar el FXML.
     * Inicializa el fondo de partículas, la animación de entrada y el teclado virtual.
     *
     * <p>El listener de clic externo (para cerrar el teclado al pulsar fuera de él)
     * se registra con {@link Platform#runLater} para garantizar que la Scene ya
     * esté completamente construida antes de acceder a ella.</p>
     *
     * @param url            URL del recurso FXML (no se usa directamente)
     * @param rb             ResourceBundle de localización (no se usa directamente)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
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
                    if (!tecladoVisible) { return; }

                    // Convertir coordenadas de escena al espacio local del rootPane
                    var puntoLocal = rootPane.sceneToLocal(e.getSceneX(), e.getSceneY());
                    boolean enTeclado = panelTeclado.getBoundsInParent().contains(puntoLocal);
                    boolean enCampo  = txtNombre.localToScene(txtNombre.getBoundsInLocal())
                                                .contains(e.getSceneX(), e.getSceneY());

                    // Si el click fue fuera del teclado y fuera del campo, ocultar teclado
                    if (!enTeclado && !enCampo) {
                        ocultarTeclado();
                    }
                });
            }
        });
    }

    // ── Teclado virtual ───────────────────────────────────────────────────────

    /**
     * Construye el teclado virtual con las filas definidas en {@link #FILAS_TECLADO}
     * y lo añade al {@code rootPane} en estado invisible y transparente a eventos.
     *
     * <p>La posición exacta del teclado se calcula dinámicamente en
     * {@link #mostrarTeclado()} cuando es necesario mostrarlo, ya que en este
     * momento el layout puede no haberse completado aún.</p>
     */
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

    /**
     * Crea un botón para el teclado virtual y le asigna estilo y comportamiento.
     *
     * <p>Cada tecla recibe la clase CSS correspondiente a su tipo:
     * {@code btn-tecla}, {@code btn-tecla-borrar}, {@code btn-tecla-espacio} o
     * {@code btn-tecla-enter}. La acción de cada tecla se gestiona mediante un
     * {@code switch} sobre su etiqueta:</p>
     * <ul>
     *   <li><b>⌫</b>: borra el carácter anterior al cursor en el TextField.</li>
     *   <li><b>ESPACIO</b>: inserta un espacio en la posición del cursor.</li>
     *   <li><b>ENTER</b>: llama a {@link #handleContinuar()} para validar el nombre.</li>
     *   <li><b>Resto</b>: inserta la letra en la posición del cursor.</li>
     * </ul>
     *
     * <p>Tras cada pulsación (excepto ENTER), el foco vuelve al TextField mediante
     * {@link Platform#runLater} para que el cursor siga visible.</p>
     *
     * @param tecla texto de la tecla (letra, "⌫", "ESPACIO" o "ENTER")
     * @return el botón configurado
     */
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
     * Muestra el teclado virtual con una animación de fade-in + slide-up (220 ms).
     * Calcula dinámicamente el margen superior del teclado para que aparezca
     * justo debajo del campo de texto, independientemente del tamaño de la ventana.
     *
     * <p>No hace nada si el teclado ya estaba visible.</p>
     */
    private void mostrarTeclado() {
        if (tecladoVisible) { return; }
        tecladoVisible = true;
        panelTeclado.setMouseTransparent(false);

        // Calcular margen dinámico: justo debajo del TextField (6 px de separación)
        var campoEnEscena = txtNombre.localToScene(txtNombre.getBoundsInLocal());
        var rootEnEscena  = rootPane.localToScene(rootPane.getBoundsInLocal());
        double margenTop  = campoEnEscena.getMaxY() - rootEnEscena.getMinY() + 6;
        StackPane.setAlignment(panelTeclado, Pos.TOP_CENTER);
        StackPane.setMargin(panelTeclado, new Insets(margenTop, 0, 0, 0));

        FadeTransition      fade  = new FadeTransition(Duration.millis(220), panelTeclado);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), panelTeclado);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }

    /**
     * Oculta el teclado virtual con una animación de fade-out + slide-down (180 ms).
     * También marca el panel como transparente a eventos para que los clics
     * en el área que ocupa no queden bloqueados.
     *
     * <p>No hace nada si el teclado ya estaba oculto.</p>
     */
    private void ocultarTeclado() {
        if (!tecladoVisible) { return; }
        tecladoVisible = false;
        panelTeclado.setMouseTransparent(true);

        FadeTransition     fade  = new FadeTransition(Duration.millis(180), panelTeclado);
        fade.setToValue(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(180), panelTeclado);
        slide.setToY(20);
        new ParallelTransition(fade, slide).play();
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    /**
     * Valida el nombre introducido y, si es correcto, navega a la pantalla de
     * selección de héroe.
     *
     * <p>Reglas de validación:</p>
     * <ol>
     *   <li>El nombre no puede estar vacío (tras hacer {@code trim()}).</li>
     *   <li>El nombre debe tener al menos 2 caracteres.</li>
     * </ol>
     *
     * <p>Si falla alguna regla, se muestra el mensaje de error con
     * {@link #mostrarError(String)} y se anima el campo con {@link #sacudirCampo()}.</p>
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
     * Carga {@code MenuPrincipal.fxml} y lo establece como escena activa.
     */
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

    /**
     * Carga la pantalla de selección de héroe, le pasa el nombre del jugador y
     * la establece como escena activa.
     *
     * <p>Se llama solo si la validación en {@link #handleContinuar()} ha pasado.
     * El nombre se inyecta en el siguiente controlador mediante
     * {@link SeleccionHeroeController#setNombreJugador(String)}.</p>
     *
     * @param nombre nombre validado que ha introducido el jugador
     */
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

    /**
     * Muestra el mensaje de error en el label {@code lblError} y lo hace visible.
     *
     * @param mensaje texto de error a mostrar (puede incluir emojis de aviso)
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    /**
     * Aplica una animación de sacudida horizontal al campo de texto para señalar
     * visualmente que ha fallado la validación.
     *
     * <p>El campo oscila 8 px a cada lado durante 6 ciclos de 60 ms cada uno
     * (duración total ~360 ms), volviendo a su posición original al terminar.</p>
     */
    private void sacudirCampo() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), txtNombre);
        tt.setFromX(0);
        tt.setByX(8);        // desplazamiento máximo en píxeles
        tt.setCycleCount(6); // número de oscilaciones
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> txtNombre.setTranslateX(0)); // restablecer posición exacta
        tt.play();
    }

    /**
     * Genera 80 partículas doradas (círculos semitransparentes) en posiciones
     * aleatorias del fondo y les aplica una animación de parpadeo continuo.
     *
     * <p>Se usa la semilla fija {@code 99} para que el patrón sea reproducible
     * entre sesiones. Los parámetros de cada partícula son:</p>
     * <ul>
     *   <li>Posición X/Y aleatoria dentro de 900×650 px.</li>
     *   <li>Radio entre 0,5 y 1,7 px.</li>
     *   <li>Opacidad base entre 0,2 y 0,7.</li>
     *   <li>FadeTransition entre el 30 % y el 100 % de la opacidad base,
     *       duración aleatoria de 1,5 a 4,5 s, con retraso aleatorio de 0 a 4 s.</li>
     * </ul>
     */
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
            ft.setFromValue(opacidad * 0.3); // brillo mínimo
            ft.setToValue(opacidad);          // brillo máximo (opacidad base)
            ft.setAutoReverse(true);
            ft.setCycleCount(Animation.INDEFINITE);
            ft.setDelay(Duration.seconds(rnd.nextDouble() * 4)); // desfase para no sincronizar
            ft.play();

            panelParticulas.getChildren().add(estrella);
        }
    }

    /**
     * Anima la entrada del panel central con fade-in + slide-up en 800 ms.
     *
     * <p>El panel pasa de opacidad 0 a 1 y de 20 px desplazado hacia abajo
     * a su posición natural. Se ejecuta con {@link Platform#runLater} para
     * asegurar que el árbol de nodos de la Scene ya esté construido antes
     * de acceder al padre del TextField.</p>
     */
    private void animarEntrada() {
        Platform.runLater(() -> {
            var panel = txtNombre.getParent(); // VBox o contenedor del campo y los botones
            if (panel == null) { return; }
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
