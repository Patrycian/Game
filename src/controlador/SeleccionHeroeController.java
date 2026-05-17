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
 *
 * <p>El jugador elige una de las tres clases disponibles antes de entrar a la
 * mazmorra: <b>Mago</b>, <b>Guerrero</b> o <b>Clérigo</b>. Cada clase tiene sus
 * propias estadísticas base y habilidades especiales únicas.</p>
 *
 * <h3>Flujo de esta pantalla:</h3>
 * <ol>
 *   <li>Se recibe el nombre del jugador desde {@link NombreController} mediante
 *       {@link #setNombreJugador(String)}.</li>
 *   <li>El jugador hace clic sobre una de las tres tarjetas de héroe; se resalta
 *       la seleccionada y se actualiza el label informativo.</li>
 *   <li>Al pulsar "¡A la aventura!", se persiste el jugador y el héroe en BD,
 *       se crea la {@link GameSession} y se navega a la mazmorra con una
 *       transición de fade-out.</li>
 * </ol>
 *
 * <h3>Aspectos visuales:</h3>
 * <ul>
 *   <li>Fondo con 80 partículas doradas animadas (semilla fija {@code 55}).</li>
 *   <li>Fade-in del panel al cargar la pantalla.</li>
 *   <li>Efecto de escala 1.04 en la tarjeta seleccionada.</li>
 *   <li>Sonido de hover {@code cursor.wav} en botones y tarjetas.</li>
 * </ul>
 */
public class SeleccionHeroeController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    /** Panel de fondo con las partículas doradas animadas. */
    @FXML private Pane   panelParticulas;

    /** Saludo personalizado con el nombre del jugador ("Aventurero X, elige tu clase"). */
    @FXML private Label  lblSaludo;

    /** Etiqueta que muestra el héroe seleccionado y su habilidad especial. */
    @FXML private Label  lblSeleccionado;

    /** Botón principal de confirmación; deshabilitado hasta que se elija un héroe. */
    @FXML private Button btnAventura;

    /** Botón para volver a la pantalla de introducción de nombre. */
    @FXML private Button btnVolver;

    /** Tarjeta visual del Mago; detecta clic para seleccionarlo. */
    @FXML private VBox   cardMago;

    /** Tarjeta visual del Guerrero; detecta clic para seleccionarlo. */
    @FXML private VBox   cardGuerrero;

    /** Tarjeta visual del Clérigo; detecta clic para seleccionarlo. */
    @FXML private VBox   cardClerigo;

    // ── Estado ────────────────────────────────────────────────────────────────
    /** Nick introducido en la pantalla anterior; se usa para crear el jugador y el héroe. */
    private String    nombreJugador;

    /** Héroe que el jugador ha seleccionado; {@code null} si aún no ha elegido. */
    private Heroe     heroeSeleccionado;

    /** Clip de audio que suena al pasar el cursor sobre botones y tarjetas. */
    private AudioClip sonidoHover;

    /**
     * Llamado automáticamente por JavaFX al cargar el FXML.
     * Inicializa partículas, animación de entrada, sonido de hover y lo asigna
     * a los tres botones de acción y a cada tarjeta de héroe.
     *
     * @param url URL del FXML (no se usa directamente)
     * @param rb  ResourceBundle de localización (no se usa directamente)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generarParticulas();
        animarEntrada();
        inicializarSonidoHover();
        agregarSonidoHover(btnAventura);
        agregarSonidoHover(btnVolver);
        // Las tarjetas no son botones, se les añade el listener de hover manualmente
        cardMago.setOnMouseEntered(e     -> { if (sonidoHover != null) { sonidoHover.play(); } });
        cardGuerrero.setOnMouseEntered(e -> { if (sonidoHover != null) { sonidoHover.play(); } });
        cardClerigo.setOnMouseEntered(e  -> { if (sonidoHover != null) { sonidoHover.play(); } });
    }

    /**
     * Recibe el nombre del jugador desde la pantalla anterior y actualiza el
     * saludo en la parte superior de la pantalla.
     *
     * <p>Debe llamarse desde {@link NombreController#navegarASeleccionRobot(String)}
     * justo después de cargar el FXML.</p>
     *
     * @param nombre nombre validado del jugador
     */
    public void setNombreJugador(String nombre) {
        this.nombreJugador = nombre;
        lblSaludo.setText("✦  Aventurero " + nombre + ", elige tu clase  ✦");
    }

    // ── Handlers de selección ─────────────────────────────────────────────────

    /**
     * Selecciona al Mago como héroe.
     * Crea una instancia de {@link Mago} con el nombre del jugador y llama a
     * {@link #seleccionar(Heroe, VBox)}.
     */
    @FXML private void handleSeleccionarMago()     { seleccionar(new Mago(nombreJugador),     cardMago);     }

    /**
     * Selecciona al Guerrero como héroe.
     * Crea una instancia de {@link Guerrero} con el nombre del jugador y llama a
     * {@link #seleccionar(Heroe, VBox)}.
     */
    @FXML private void handleSeleccionarGuerrero() { seleccionar(new Guerrero(nombreJugador), cardGuerrero); }

    /**
     * Selecciona al Clérigo como héroe.
     * Crea una instancia de {@link Clerigo} con el nombre del jugador y llama a
     * {@link #seleccionar(Heroe, VBox)}.
     */
    @FXML private void handleSeleccionarClerigo()  { seleccionar(new Clerigo(nombreJugador),  cardClerigo);  }

    /**
     * Persiste el jugador y el héroe en la BD, crea la sesión de juego y navega
     * a la pantalla de mazmorra con una transición de fade-out (600 ms).
     *
     * <p>Flujo interno:</p>
     * <ol>
     *   <li>{@link JugadorDAO#insertar(Jugador)}: si el nick ya existe, devuelve
     *       el jugador existente sin duplicarlo.</li>
     *   <li>{@link PersonajeDAO#insertar(Heroe, int)}: guarda el héroe y asigna
     *       su id generado por BD.</li>
     *   <li>Crea un {@link GameSession} con jugador y héroe.</li>
     *   <li>Carga {@code Mazmorra.fxml} e inyecta la sesión en
     *       {@link MazmorraController#iniciarSesion(GameSession)}.</li>
     *   <li>Cambia de escena directamente: la pantalla de carga ya forma parte
     *       del flujo interno de la mazmorra.</li>
     * </ol>
     *
     * <p>Si ocurre un error de BD, muestra un diálogo de error informativo.</p>
     */
    @FXML
    private void handleAventura() {
        try {
            // 1. Persistir jugador y héroe en BD
            Jugador jugador = JugadorDAO.insertar(new Jugador(nombreJugador));
            PersonajeDAO.insertar(heroeSeleccionado, jugador.getId());

            // 2. Crear sesión de juego con los datos persistidos
            GameSession sesion = new GameSession(jugador, heroeSeleccionado);

            // 3. Cargar la pantalla de mazmorra (fase 1 por defecto)
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/Mazmorra.fxml")
            );
            Parent root = loader.load();

            MazmorraController siguiente = loader.getController();
            siguiente.iniciarSesion(sesion);

            // 4. Cambio de escena directo (sin transición): la pantalla de carga
            //    se muestra dentro de la propia mazmorra como parte del flujo.
            Stage stage = (Stage) btnAventura.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            mostrarError("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vuelve a la pantalla de introducción de nombre sin seleccionar ningún héroe.
     * Carga {@code Nombre.fxml} y lo establece como escena activa.
     */
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

    /**
     * Marca el héroe dado como seleccionado, actualiza el resaltado visual de las
     * tarjetas y habilita el botón de confirmación.
     *
     * <p>Efectos visuales:</p>
     * <ul>
     *   <li>Quita la clase CSS {@code card-robot-seleccionada} de todas las tarjetas.</li>
     *   <li>Añade la clase {@code card-robot-seleccionada} a la tarjeta elegida.</li>
     *   <li>Aplica un pulso de escala 1.04 en 150 ms, revertido automáticamente.</li>
     *   <li>Actualiza {@code lblSeleccionado} con icono, tipo y nombre de habilidad.</li>
     * </ul>
     *
     * @param heroe          instancia del héroe creada para el jugador
     * @param cardSeleccionada tarjeta VBox correspondiente al héroe elegido
     */
    private void seleccionar(Heroe heroe, VBox cardSeleccionada) {
        heroeSeleccionado = heroe;

        // Quitar resaltado de todas las tarjetas
        List.of(cardMago, cardGuerrero, cardClerigo)
            .forEach(c -> c.getStyleClass().removeAll("card-robot-seleccionada"));

        // Resaltar la tarjeta elegida
        cardSeleccionada.getStyleClass().add("card-robot-seleccionada");

        // Pulso de escala para feedback visual inmediato
        ScaleTransition st = new ScaleTransition(Duration.millis(150), cardSeleccionada);
        st.setToX(1.04);
        st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        // Mostrar información del héroe seleccionado
        String habilidades = heroe.getHabilidades().stream()
                .map(h -> h.getNombre())
                .collect(java.util.stream.Collectors.joining(" · "));
        lblSeleccionado.setText("✔  Has elegido: " + heroe.getIcono() + " " + heroe.getTipo()
                + "  —  " + habilidades);
        lblSeleccionado.setVisible(true);
        lblSeleccionado.setManaged(true);
        btnAventura.setDisable(false);
    }

    /**
     * Carga el clip de sonido {@code cursor.wav} en memoria para reproducirlo
     * con latencia mínima al hacer hover sobre los elementos interactivos.
     * Si el archivo no existe, el efecto de sonido simplemente queda desactivado.
     */
    private void inicializarSonidoHover() {
        try {
            URL url = getClass().getResource("/recursos/audio/cursor.wav");
            if (url != null) { sonidoHover = new AudioClip(url.toString()); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registra un listener en el evento {@code MOUSE_ENTERED} del botón dado
     * para reproducir el sonido de hover cuando el cursor entra en él.
     *
     * @param btn botón al que añadir el efecto de sonido
     */
    private void agregarSonidoHover(Button btn) {
        btn.setOnMouseEntered(e -> {
            if (sonidoHover != null) { sonidoHover.play(); }
        });
    }

    /**
     * Muestra un diálogo de error modal con el mensaje proporcionado.
     * Se usa para informar de fallos de conexión con la BD al intentar avanzar.
     *
     * @param msg texto del error a mostrar
     */
    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ── Animaciones y partículas ──────────────────────────────────────────────

    /**
     * Genera 80 partículas doradas (círculos semitransparentes) en posiciones
     * aleatorias del fondo y les aplica una animación de parpadeo continuo.
     *
     * <p>Usa la semilla fija {@code 55} para que el patrón sea reproducible.
     * El color base es {@code #c8a84b} (dorado) con opacidad entre 0,2 y 0,7.</p>
     */
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

    /**
     * Anima la entrada del panel principal con un fade-in de 700 ms y un retraso
     * de 150 ms (para que las partículas estén ya visibles cuando aparezca el panel).
     *
     * <p>Se ejecuta con {@link Platform#runLater} para garantizar que el árbol de
     * nodos ya esté completamente construido.</p>
     */
    private void animarEntrada() {
        Platform.runLater(() -> {
            var panel = btnAventura.getParent().getParent(); // VBox raíz del contenido central
            if (panel == null) { return; }
            panel.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(700), panel);
            fade.setToValue(1); fade.setDelay(Duration.millis(150)); fade.play();
        });
    }
}
