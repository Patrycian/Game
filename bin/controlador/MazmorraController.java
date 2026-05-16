package controlador;

import dao.CombateDAO;
import dao.JugadorDAO;
import dao.PartidaDAO;
import dao.PersonajeDAO;
import modelo.*;
import motor.MotorCombate;
import motor.MotorCombate.ResultadoCombate;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador principal de la pantalla de combate en la mazmorra (estilo Pokémon).
 *
 * <p>Gestiona toda la lógica de la interfaz de combate: turnos, barras de vida y
 * de magia, submenús contextuales, animaciones, persistencia entre fases y
 * navegación al terminar la partida.</p>
 *
 * <h3>Flujo general de una partida:</h3>
 * <ol>
 *   <li>{@link #iniciarSesion(GameSession)} es llamado por el controlador anterior
 *       con la sesión ya preparada.</li>
 *   <li>{@link #prepararCombate()} configura la UI para la fase actual: muestra
 *       el enemigo, resetea barras, construye submenús y arranca la música.</li>
 *   <li>El jugador elige una acción (ataque, habilidad, objeto, huir).</li>
 *   <li>{@link #ejecutarTurno(AccionHeroe)} procesa la acción, aplica daños,
 *       refresca barras y comprueba el resultado del combate.</li>
 *   <li>Si el combate termina, {@link #procesarFinCombate(ResultadoCombate)} registra
 *       el combate en BD, actualiza la puntuación y muestra el botón de continuar.</li>
 *   <li>Al continuar, si hay más fases se llama de nuevo a {@link #prepararCombate()};
 *       si no, se navega a la pantalla de resultado.</li>
 * </ol>
 *
 * <h3>Submenús disponibles:</h3>
 * <ul>
 *   <li><b>menuMagia</b>: para personajes {@link Magico} (Mago, Clérigo).
 *       Muestra las habilidades mágicas del personaje con su coste en PM.</li>
 *   <li><b>menuHabilidades</b>: para el {@link Guerrero}.
 *       Muestra Golpe Devastador y Postura de Hierro.</li>
 *   <li><b>menuObjetos</b>: compartido por todos. Permite usar pociones de
 *       curación (×3) o mágicas (×2) durante toda la partida.</li>
 * </ul>
 *
 * <h3>Música:</h3>
 * <ul>
 *   <li>Fases 1-3: {@code Battle.mp3} en bucle.</li>
 *   <li>Fase 4 (jefe final): {@code finalBoss.mp3} en bucle.</li>
 * </ul>
 */
public class MazmorraController implements Initializable {

    // ── FXML: cabecera y escena ──────────────────────────────────────────────
    /** Muestra "⚔ FASE N — MAZMORRA" o "⚔ FASE 4 — JEFE FINAL". */
    @FXML private Label       lblFase;

    /** Nombre y tipo del héroe. Formato: "Gandalf (MAGO)". */
    @FXML private Label       lblNombreHeroe;

    /** HP actual / HP máximo del héroe. Se actualiza tras cada turno. */
    @FXML private Label       lblHpHeroe;

    /** Barra de vida del héroe. Verde > 50 %, naranja > 25 %, rojo ≤ 25 %. */
    @FXML private ProgressBar barraVidaHeroe;

    /** Nombre y tipo del enemigo actual. */
    @FXML private Label       lblNombreEnemigo;

    /** Barra de vida del enemigo con la misma escala de colores que la del héroe. */
    @FXML private ProgressBar barraVidaEnemigo;

    /** HP actual / HP máximo del enemigo. */
    @FXML private Label       lblHpEnemigo;

    /** Imagen del sprite del héroe (PNG cargado desde {@link Heroe#getRutaImagen()}). */
    @FXML private ImageView   imgHeroe;

    /** Emoji grande del enemigo. Se anima con {@link #animarGolpe(Node)} al recibir daño. */
    @FXML private Label       lblIconoEnemigo;

    // ── FXML: caja de diálogo y menú ─────────────────────────────────────────
    /**
     * Prompt de la caja de diálogo estilo Pokémon.
     * Muestra "¿Qué acción realizará Nombre?" al inicio y "¡Victoria!" / "Derrota…" al final.
     */
    @FXML private Label       lblPrompt;

    /** Área de texto donde se acumula el log de combate turno a turno. */
    @FXML private TextArea    txtLog;

    /**
     * Label de resultado al terminar el combate.
     * Victoria en dorado ("🏆 ¡VICTORIA! +10 puntos") o derrota en rojo ("💀 DERROTA…").
     */
    @FXML private Label       lblResultado;

    /** Menú principal de batalla con los botones ATACAR, OBJETOS, HABILIDAD/MAGIA/HABILIDADES y HUIR. */
    @FXML private VBox        menuBatalla;

    /** Botón de ataque básico; siempre visible. */
    @FXML private Button      btnAtacar;

    /** Botón de objetos (pociones); se deshabilita cuando el inventario está vacío. */
    @FXML private Button      btnObjetos;

    /**
     * Botón de habilidad especial genérico; visible solo para personajes sin submenú
     * propio (no Magico ni Guerrero). Actualmente solo el Clérigo lo usa así
     * (Curación Divina desde el propio botón, sin submenú).
     *
     * <p>Nota: el Clérigo también es Magico y tiene submenú, por lo que en la
     * práctica este botón nunca se muestra solo. Se mantiene por compatibilidad.</p>
     */
    @FXML private Button      btnHabilidad;

    /**
     * Botón "🧙 MAGIA"; visible únicamente para personajes {@link Magico}.
     * Abre {@link #menuMagia} al pulsarlo.
     */
    @FXML private Button      btnMagia;

    /**
     * Botón "⚔️ HABILIDADES"; visible únicamente para el {@link Guerrero}.
     * Abre {@link #menuHabilidades} al pulsarlo.
     */
    @FXML private Button      btnHabilidades;

    /** Botón de huida; abre un diálogo de confirmación antes de salvar y salir. */
    @FXML private Button      btnHuir;

    /**
     * Botón que aparece al terminar el combate.
     * Victoria: "→ Siguiente Fase" o "🎉 Ver Resultado Final".
     * Derrota: "📜 Volver al Menú".
     */
    @FXML private Button      btnContinuar;

    // ── FXML: submenú de magia ────────────────────────────────────────────────
    /**
     * Panel del submenú de magia; ocupa el mismo espacio que {@code menuBatalla}
     * gracias al StackPane. Se hace visible / oculto en exclusión mutua con el menú principal.
     */
    @FXML private VBox        menuMagia;

    /**
     * Contenedor donde se generan dinámicamente los botones de habilidades mágicas
     * en {@link #construirSubmenuMagia(Magico)}.
     */
    @FXML private VBox        contenedorHabilidades;

    // ── FXML: submenú de habilidades del guerrero ─────────────────────────────
    /**
     * Panel del submenú de habilidades del Guerrero.
     * Funciona igual que {@link #menuMagia} pero para las habilidades físicas.
     */
    @FXML private VBox        menuHabilidades;

    /**
     * Contenedor donde se generan dinámicamente los botones de habilidades
     * del Guerrero en {@link #construirSubmenuHabilidades(Guerrero)}.
     */
    @FXML private VBox        contenedorHabilidadesGuerrero;

    // ── FXML: submenú de objetos ──────────────────────────────────────────────
    /** Panel del submenú de objetos (pociones). */
    @FXML private VBox        menuObjetos;

    /**
     * Botón de poción de curación en el submenú de objetos.
     * Se deshabilita y muestra "(agotadas)" cuando el stock llega a 0.
     */
    @FXML private Button      btnPocionCuracion;

    /**
     * Botón de poción mágica en el submenú de objetos.
     * Solo tiene efecto real para personajes {@link Magico}; para los demás
     * consume la poción sin restaurar PM (con mensaje informativo).
     */
    @FXML private Button      btnPocionMagica;

    // ── FXML: barra de PM del héroe ──────────────────────────────────────────
    /**
     * Fila del panel de héroe que contiene la etiqueta "PM" y la barra de PM.
     * Visible solo para personajes {@link Magico}.
     */
    @FXML private HBox        filaPm;

    /**
     * Fila con el texto numérico de PM del héroe (p. ej. "20 / 30 PM").
     * Visible solo para personajes {@link Magico}.
     */
    @FXML private HBox        filaNumPm;

    /** Barra de progreso que representa los PM actuales del héroe mágico. */
    @FXML private ProgressBar barraPoderMagico;

    /** Label numérico de PM del héroe. Formato: "PM_actual / PM_max PM". */
    @FXML private Label       lblPmHeroe;

    // ── FXML: barra de PM del enemigo ────────────────────────────────────────
    /**
     * Fila del panel de enemigo con la etiqueta "PM" y la barra de PM.
     * Visible solo cuando el enemigo usa magia (Saga, Dragón).
     */
    @FXML private HBox        filaPmEnemigo;

    /**
     * Fila con el texto numérico de PM del enemigo.
     * Visible solo cuando el enemigo usa magia.
     */
    @FXML private HBox        filaNumPmEnemigo;

    /** Barra de progreso que representa los PM actuales del enemigo mágico. */
    @FXML private ProgressBar barraPmEnemigo;

    /** Label numérico de PM del enemigo. Formato: "PM_actual / PM_max PM". */
    @FXML private Label       lblPmEnemigo;

    // ── Estado ────────────────────────────────────────────────────────────────
    /** Sesión de juego activa; contiene jugador, héroe, fase y referencia a la partida en BD. */
    private GameSession  sesion;

    /** Motor de combate por turnos; gestiona ataques, contraataques y resultado. */
    private MotorCombate motor;

    /**
     * Indica si el combate actual ha terminado (victoria o derrota).
     * Se usa como guardia en los handlers para ignorar pulsaciones tras el fin.
     */
    private boolean      combateTerminado = false;

    /** Reproductor de música de fondo; se cambia entre fases (Battle.mp3 / finalBoss.mp3). */
    private MediaPlayer mediaPlayer;

    // ── Inventario del combate ────────────────────────────────────────────────
    /**
     * Pociones de curación disponibles para toda la partida.
     * Se inicializan a 3 en {@link #iniciarSesion(GameSession)} y persisten entre fases.
     */
    private int pocionesRestantes;

    /** Cantidad de HP que restaura cada poción de curación. */
    private static final int CURACION_POCION = 30;

    /**
     * Pociones mágicas disponibles para toda la partida.
     * Se inicializan a 2 en {@link #iniciarSesion(GameSession)} y persisten entre fases.
     */
    private int pocionesMagicasRestantes;

    /** Cantidad de PM que restaura cada poción mágica. */
    private static final int RESTAURACION_PM_POCION = 10;

    /**
     * Método vacío requerido por {@link Initializable}.
     * Toda la configuración real se realiza en {@link #iniciarSesion(GameSession)},
     * que recibe los datos de la sesión del controlador anterior.
     *
     * @param url URL del FXML (no se usa)
     * @param rb  ResourceBundle de localización (no se usa)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) { /* configuración en iniciarSesion */ }

    /**
     * Punto de entrada principal del controlador.
     * Recibe la sesión del controlador anterior ({@link SeleccionHeroeController} o
     * {@link CargarPartidaController}), inicializa el inventario y arranca el combate
     * de la fase actual.
     *
     * <p>El inventario (3 pociones de curación, 2 mágicas) se inicializa aquí,
     * una sola vez, y persiste entre todas las fases de la misma partida.</p>
     *
     * @param sesion sesión activa con jugador, héroe y estado de la partida
     */
    public void iniciarSesion(GameSession sesion) {
        this.sesion = sesion;
        // El inventario se inicializa aquí, una sola vez para toda la partida
        pocionesRestantes        = 3;
        pocionesMagicasRestantes = 2;
        prepararCombate();
    }

    // ── Preparación ───────────────────────────────────────────────────────────

    /**
     * Configura toda la interfaz para el combate de la fase actual.
     * Se llama al inicio de cada fase (incluyendo la primera).
     *
     * <p>Acciones que realiza:</p>
     * <ol>
     *   <li>Genera el enemigo de la fase mediante {@link MotorCombate#generarEnemigo(int)}.</li>
     *   <li>Reinicia la habilidad especial del héroe (que se recarga entre fases).</li>
     *   <li>Actualiza los labels de fase, nombre y HP del héroe y del enemigo.</li>
     *   <li>Carga el sprite del héroe (fallo no-fatal si el archivo no existe).</li>
     *   <li>Muestra / oculta las barras de PM del héroe y del enemigo según el tipo.</li>
     *   <li>Configura la visibilidad de los botones según la clase del héroe:
     *       {@code btnHabilidad} para el Clérigo, {@code btnMagia} para personajes
     *       Mágicos, {@code btnHabilidades} para el Guerrero.</li>
     *   <li>Construye los submenús correspondientes.</li>
     *   <li>Limpia el log y escribe los mensajes de inicio.</li>
     *   <li>Inicia la música apropiada para la fase.</li>
     * </ol>
     */
    private void prepararCombate() {
        int fase = sesion.getFaseActual();
        Heroe   heroe   = sesion.getHeroe();
        Enemigo enemigo = MotorCombate.generarEnemigo(fase);

        heroe.reiniciarHabilidad();   // la habilidad especial se recarga entre fases
        motor = new MotorCombate(heroe, enemigo);
        combateTerminado = false;

        // ── Labels de fase
        lblFase.setText("⚔  FASE " + fase + (fase == 4 ? "  —  JEFE FINAL" : "  —  MAZMORRA"));

        // ── Héroe: nombre, imagen y barra de vida
        lblNombreHeroe.setText(heroe.getNombre() + " (" + heroe.getTipo() + ")");
        try {
            Image imgSrc = new Image(getClass().getResourceAsStream(heroe.getRutaImagen()));
            imgHeroe.setImage(imgSrc);
        } catch (Exception e) {
            // Si la imagen no carga, el juego continúa sin el sprite (no es un error fatal)
            e.printStackTrace();
        }
        actualizarBarraHeroe();

        // ── Enemigo: nombre, icono y barra de vida
        lblNombreEnemigo.setText(enemigo.getNombre() + " (" + enemigo.getTipo() + ")");
        lblIconoEnemigo.setText(enemigo.getIcono());
        actualizarBarraEnemigo();

        // ── Barra de PM del enemigo (solo para Saga y Dragón, que tienen pmMax > 0)
        boolean enemigoTienePm = enemigo.tienePmMax();
        filaPmEnemigo.setVisible(enemigoTienePm);
        filaPmEnemigo.setManaged(enemigoTienePm);
        filaNumPmEnemigo.setVisible(enemigoTienePm);
        filaNumPmEnemigo.setManaged(enemigoTienePm);
        if (enemigoTienePm) actualizarBarraPmEnemigo();

        // ── Visibilidad de los botones de habilidad según la clase del héroe
        boolean esMagico   = heroe instanceof Magico;
        boolean esGuerrero = heroe instanceof Guerrero;

        // btnHabilidad: solo para clases sin submenú propio
        // (actualmente ninguna clase llega a este caso, pero se mantiene por extensibilidad)
        btnHabilidad.setVisible(!esMagico && !esGuerrero);
        btnHabilidad.setManaged(!esMagico && !esGuerrero);

        // btnMagia: submenú de hechizos para Mago y Clérigo
        btnMagia.setVisible(esMagico);
        btnMagia.setManaged(esMagico);

        // btnHabilidades: submenú de habilidades físicas para el Guerrero
        btnHabilidades.setVisible(esGuerrero);
        btnHabilidades.setManaged(esGuerrero);

        // ── Construcción del submenú correspondiente
        if (esMagico) {
            btnMagia.setDisable(false);
            construirSubmenuMagia((Magico) heroe);
        } else if (esGuerrero) {
            btnHabilidades.setDisable(false);
            construirSubmenuHabilidades((Guerrero) heroe);
        } else {
            btnHabilidad.setText("✨ " + heroe.getNombreHabilidad().toUpperCase());
            btnHabilidad.setDisable(false);
        }

        // ── Asegurar que todos los submenús empiecen ocultos
        if (menuMagia != null) {
            menuMagia.setVisible(false);
            menuMagia.setManaged(false);
        }
        if (menuObjetos != null) {
            menuObjetos.setVisible(false);
            menuObjetos.setManaged(false);
        }
        if (menuHabilidades != null) {
            menuHabilidades.setVisible(false);
            menuHabilidades.setManaged(false);
        }

        // ── Barra de PM del héroe (solo para personajes Mágicos)
        filaPm.setVisible(esMagico);
        filaPm.setManaged(esMagico);
        filaNumPm.setVisible(esMagico);
        filaNumPm.setManaged(esMagico);
        if (esMagico) actualizarBarraPm();

        // ── Log de inicio del combate
        txtLog.clear();
        agregarLog("¡Un " + enemigo.getNombre() + " salvaje apareció!");
        agregarLog("");
        agregarLog(heroe.getDescHabilidad());
        agregarLog("");

        if (lblPrompt != null) {
            lblPrompt.setText("¿Qué acción realizará " + heroe.getNombre() + "?");
            lblPrompt.setVisible(true);
        }

        // ── Estado inicial de los botones de resultado
        lblResultado.setVisible(false);
        if (btnContinuar != null) {
            btnContinuar.setVisible(false);
            btnContinuar.setManaged(false);
            // Restaurar el handler por defecto (puede haberse sobreescrito tras una derrota)
            btnContinuar.setOnAction(e -> handleContinuar());
        }

        if (menuBatalla != null) {
            menuBatalla.setVisible(true);
            menuBatalla.setManaged(true);
        }

        // btnObjetos: deshabilitado solo si el inventario completo está vacío
        btnAtacar.setDisable(false);
        btnObjetos.setDisable(pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0);
        btnHuir.setDisable(false);

        // ── Música: Battle.mp3 para fases 1-3, finalBoss.mp3 para la fase 4
        iniciarMusica(sesion.getFaseActual() == 4);
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    /**
     * Maneja la pulsación del botón "ATACAR".
     * Ejecuta un turno con la acción {@link AccionHeroe#ATAQUE}.
     */
    @FXML
    private void handleAtacar() {
        ejecutarTurno(AccionHeroe.ATAQUE);
    }

    /**
     * Maneja la pulsación del botón "HABILIDAD" (versión genérica, sin submenú).
     * Ejecuta un turno con la acción {@link AccionHeroe#HABILIDAD}.
     * Solo es visible para personajes sin submenú propio.
     */
    @FXML
    private void handleHabilidad() {
        ejecutarTurno(AccionHeroe.HABILIDAD);
    }

    /**
     * Maneja la pulsación del botón "OBJETOS".
     * Abre el submenú de objetos si el inventario tiene al menos un objeto disponible.
     * Si el inventario está vacío muestra un mensaje en el log y no abre el submenú.
     */
    @FXML
    private void handleObjetos() {
        if (combateTerminado) return;
        if (pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0) {
            agregarLog("🎒 No te quedan objetos.");
            return;
        }
        actualizarSubmenuObjetos();
        menuBatalla.setVisible(false);
        menuBatalla.setManaged(false);
        menuObjetos.setVisible(true);
        menuObjetos.setManaged(true);
    }

    /**
     * Maneja la pulsación del botón "HUIR".
     * Muestra un diálogo de confirmación con estilo oscuro del juego.
     * Si el jugador confirma, guarda la partida en BD y vuelve al menú principal.
     * La partida queda en estado {@code EN_CURSO} para poder reanudarla después.
     */
    @FXML
    private void handleHuir() {
        if (combateTerminado) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Huida");
        alert.setHeaderText("🏃  ¿Huir del combate?");
        alert.setContentText("La partida se guardará y volverás al menú principal.");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/application/vistas/estilos.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-oscuro");

        Optional<ButtonType> resp = alert.showAndWait();
        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            guardarPartida();
            navegarAMenu();
        }
    }

    /**
     * Maneja la pulsación del botón "Continuar" al terminar un combate.
     * Si hay más fases disponibles, avanza a la siguiente con {@link #prepararCombate()}.
     * Si se ha completado la última fase (4), navega a la pantalla de resultado con victoria.
     */
    @FXML
    private void handleContinuar() {
        if (sesion.hayMasFases()) {
            sesion.avanzarFase();
            prepararCombate();
        } else {
            // El jugador ha superado las 4 fases → victoria total
            navegarAResultado(true);
        }
    }

    // ── Lógica de turno ───────────────────────────────────────────────────────

    /**
     * Enumeración de las posibles acciones del héroe en cada turno.
     *
     * <ul>
     *   <li>{@code ATAQUE}: ataque físico básico.</li>
     *   <li>{@code HABILIDAD}: habilidad especial del héroe (Bola de Fuego,
     *       Golpe Devastador, Curación Divina).</li>
     *   <li>{@code HABILIDAD_GUERRERO_2}: Postura de Hierro del Guerrero
     *       (buff defensivo, no hace daño directo).</li>
     *   <li>{@code POCION}: usa una poción de curación (+{@link #CURACION_POCION} HP).</li>
     *   <li>{@code POCION_MAGICA}: usa una poción mágica (+{@link #RESTAURACION_PM_POCION} PM).</li>
     * </ul>
     */
    private enum AccionHeroe { ATAQUE, HABILIDAD, HABILIDAD_GUERRERO_2, POCION, POCION_MAGICA }

    /**
     * Procesa un turno completo según la acción elegida por el jugador.
     *
     * <p>Flujo general para las acciones de combate directo (ATAQUE, HABILIDAD):</p>
     * <ol>
     *   <li>Gastar PM si corresponde (Magico usando HABILIDAD).</li>
     *   <li>Llamar al motor para ejecutar el turno y obtener los mensajes.</li>
     *   <li>Animar el golpe al enemigo (excepto curación del Clérigo).</li>
     *   <li>Si el enemigo sobrevive, animar el contraataque sobre el héroe (con pausa de 350 ms).</li>
     * </ol>
     *
     * <p>Para las acciones de objeto (POCION, POCION_MAGICA):</p>
     * <ol>
     *   <li>Decrementar el contador de pociones.</li>
     *   <li>Aplicar el efecto de curación/restauración.</li>
     *   <li>El enemigo aprovecha el turno y contraataca.</li>
     *   <li>Si el héroe muere por contraataque, el resultado se marca como DERROTA.</li>
     * </ol>
     *
     * <p>Después de toda acción se actualizan las barras de vida y de PM, y se
     * comprueba si el resultado del motor es distinto de {@code EN_CURSO}.</p>
     *
     * <p><b>Importante</b>: los casos POCION y POCION_MAGICA usan
     * {@code motor.ejecutarContraataqueEnemigo()} en lugar de llamar directamente a
     * {@code enemigo.realizarAtaque()}, para que {@code motor.turno} se incremente
     * correctamente y {@code motor.resultado} se actualice a DERROTA si el héroe muere.
     * Usan {@code break} (no {@code return}) para que la actualización de barras y la
     * comprobación de fin de combate siempre se ejecuten.</p>
     *
     * @param accion la acción que el jugador ha elegido para este turno
     */
    private void ejecutarTurno(AccionHeroe accion) {
        if (combateTerminado) return;

        switch (accion) {
            case ATAQUE:
            case HABILIDAD: {
                // Si es un personaje mágico usando su habilidad especial, consume la mitad de su PM máximo
                if (accion == AccionHeroe.HABILIDAD && sesion.getHeroe() instanceof Magico) {
                    Magico m = (Magico) sesion.getHeroe();
                    int coste = Math.max(1, m.getPmMax() / 2);
                    if (!m.gastarPm(coste)) {
                        // Red de seguridad: el botón debería estar deshabilitado si PM es insuficiente,
                        // pero si por algún motivo se llega aquí, abortar el turno sin penalización.
                        agregarLog("⚠ PM insuficientes para usar " + m.getNombreHabilidad()
                                + ". (PM: " + m.getPm() + "/" + m.getPmMax() + ")");
                        return;
                    }
                }
                List<String> mensajes = motor.ejecutarTurnoHeroe(accion == AccionHeroe.HABILIDAD);
                mensajes.forEach(this::agregarLog);

                // Animar golpe al enemigo, EXCEPTO cuando el Clérigo usa su curación
                // (en ese caso no ataca al enemigo, solo se cura a sí mismo)
                if (!(sesion.getHeroe() instanceof Clerigo) || accion != AccionHeroe.HABILIDAD) {
                    animarGolpe(lblIconoEnemigo);
                }
                // Si el enemigo sobrevivió al ataque del héroe, también contraataca
                if (motor.getEnemigo().estaVivo()) {
                    PauseTransition espera = new PauseTransition(Duration.millis(350));
                    espera.setOnFinished(ev -> animarGolpe(imgHeroe));
                    espera.play();
                }
                break;
            }
            case HABILIDAD_GUERRERO_2: {
                // Postura de Hierro: buff defensivo pasivo, no ataca directamente al enemigo
                Guerrero g = (Guerrero) sesion.getHeroe();
                String efecto = g.usarPosturaDeHierro();
                if (efecto == null) {
                    // La postura ya estaba activa (no debería ocurrir: el botón se deshabilita)
                    agregarLog("🛡️ La Postura de Hierro ya está activa.");
                    break; // break, no return: las barras y el fin de combate siguen procesándose
                }
                agregarLog(efecto);

                // El enemigo contraataca (el buff ocupa el turno del héroe)
                List<String> contraataque = motor.ejecutarContraataqueEnemigo();
                contraataque.forEach(this::agregarLog);
                if (!contraataque.isEmpty()) {
                    PauseTransition espera = new PauseTransition(Duration.millis(200));
                    espera.setOnFinished(ev -> animarGolpe(imgHeroe));
                    espera.play();
                }
                break;
            }
            case POCION: {
                pocionesRestantes--;
                Heroe h = sesion.getHeroe();
                int hpAntes = h.getPuntosGolpe();
                h.curar(CURACION_POCION);
                int curado = h.getPuntosGolpe() - hpAntes; // puede ser < CURACION_POCION si HP ya estaba alto

                agregarLog("🧪 " + h.getNombre() + " usa una poción y recupera " +
                        curado + " HP. (HP: " + h.getPuntosGolpe() + "/" +
                        h.getPuntosGolpeMax() + ")");

                // El enemigo aprovecha el turno — pasamos por el motor para que turno++
                // y motor.resultado se actualicen correctamente (igual que HABILIDAD_GUERRERO_2)
                List<String> contraPocion = motor.ejecutarContraataqueEnemigo();
                contraPocion.forEach(this::agregarLog);
                if (!contraPocion.isEmpty()) animarGolpe(imgHeroe);

                if (pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0)
                    btnObjetos.setDisable(true);
                break;
            }
            case POCION_MAGICA: {
                pocionesMagicasRestantes--;
                Heroe h = sesion.getHeroe();

                if (h instanceof Magico) {
                    Magico m = (Magico) h;
                    int pmAntes = m.getPm();
                    m.restaurarPmParcial(RESTAURACION_PM_POCION);
                    int restaurado = m.getPm() - pmAntes; // puede ser < RESTAURACION_PM si PM ya estaba lleno
                    agregarLog("🔮 " + h.getNombre() + " usa una poción mágica y recupera "
                            + restaurado + " PM. (PM: " + m.getPm() + "/" + m.getPmMax() + ")");
                } else {
                    // Personaje no mágico: la poción se consume sin efecto
                    agregarLog("🔮 " + h.getNombre() + " usa una poción mágica..."
                            + " ¡No tienes PM! La poción no hizo efecto.");
                }

                // El enemigo aprovecha el turno — pasamos por el motor para coherencia
                List<String> contraMagica = motor.ejecutarContraataqueEnemigo();
                contraMagica.forEach(this::agregarLog);
                if (!contraMagica.isEmpty()) animarGolpe(imgHeroe);

                if (pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0)
                    btnObjetos.setDisable(true);
                break;
            }
        }

        // Separador y actualización de barras (siempre, independientemente de la acción)
        agregarLog("");
        actualizarBarraHeroe();
        actualizarBarraEnemigo();
        if (sesion.getHeroe() instanceof Magico) actualizarBarraPm();
        if (motor.getEnemigo().tienePmMax()) actualizarBarraPmEnemigo();

        // Determinar resultado final del combate
        // motor.getResultado() ya refleja correctamente DERROTA si el héroe murió
        // en cualquiera de los casos (incluyendo poción), al pasar por ejecutarContraataqueEnemigo()
        ResultadoCombate resultado = motor.getResultado();

        if (resultado != ResultadoCombate.EN_CURSO) {
            combateTerminado = true;
            procesarFinCombate(resultado);
        }
    }

    /**
     * Procesa el fin del combate: desactiva los botones, registra el combate en BD,
     * actualiza la puntuación (si es victoria) y muestra el botón de continuar.
     *
     * <p>En caso de victoria:</p>
     * <ul>
     *   <li>Suma +10 puntos al jugador y actualiza BD.</li>
     *   <li>Guarda o actualiza la partida con el nuevo estado.</li>
     *   <li>Muestra "→ Siguiente Fase" o "🎉 Ver Resultado Final" según si quedan fases.</li>
     * </ul>
     *
     * <p>En caso de derrota:</p>
     * <ul>
     *   <li>Actualiza la partida a estado {@code DERROTA} con HP = 0.</li>
     *   <li>Cambia el handler del botón de continuar para navegar al resultado de derrota.</li>
     * </ul>
     *
     * <p>En ambos casos se cierran los submenús abiertos y se registra el combate
     * en la tabla {@code combates} mediante {@link CombateDAO#registrar}.</p>
     *
     * @param resultado resultado del combate ({@code VICTORIA} o {@code DERROTA})
     */
    private void procesarFinCombate(ResultadoCombate resultado) {
        // Deshabilitar todos los botones de acción
        btnAtacar.setDisable(true);
        btnHabilidad.setDisable(true);
        btnMagia.setDisable(true);
        btnHabilidades.setDisable(true);
        btnObjetos.setDisable(true);
        btnHuir.setDisable(true);

        // Cerrar cualquier submenú que pudiera estar abierto al terminar el combate
        if (menuMagia        != null && menuMagia.isVisible())        handleVolverMenu();
        if (menuObjetos      != null && menuObjetos.isVisible())      handleVolverMenuObjetos();
        if (menuHabilidades  != null && menuHabilidades.isVisible())  handleVolverMenuHabilidades();

        boolean victoria = resultado == ResultadoCombate.VICTORIA;

        // Registrar el combate en BD (historial de combates de la partida)
        try {
            guardarOActualizarPartida();
            // Comprobar que la partida fue creada antes de registrar el combate;
            // si guardarOActualizarPartida() falló, sesion.getPartida() puede ser null.
            if (sesion.getPartida() != null) {
                CombateDAO.registrar(
                    sesion.getPartida().getId(),
                    sesion.getFaseActual(),
                    motor.getEnemigo().getTipo(),
                    victoria,
                    motor.getTurno()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (victoria) {
            // Victoria: +10 puntos y guardar en BD
            sesion.getJugador().sumarPuntos(10);
            try { JugadorDAO.actualizarPuntuacion(sesion.getJugador()); }
            catch (Exception e) { e.printStackTrace(); }

            lblResultado.setText("🏆  ¡VICTORIA! +10 puntos");
            lblResultado.setStyle("-fx-text-fill: #f0d070;");

            if (sesion.hayMasFases()) {
                btnContinuar.setText("→  Siguiente Fase");
            } else {
                btnContinuar.setText("🎉  Ver Resultado Final");
            }
        } else {
            // Derrota: marcar partida como DERROTA en BD (solo si existe)
            if (sesion.getPartida() != null) {
                try {
                    Partida p = sesion.getPartida();
                    p.setEstado(modelo.Partida.Estado.DERROTA);
                    p.setHpActual(0);
                    PartidaDAO.actualizar(p);
                } catch (Exception e) { e.printStackTrace(); }
            }

            lblResultado.setText("💀  DERROTA. Tu aventura ha terminado.");
            lblResultado.setStyle("-fx-text-fill: #e05555;");
            btnContinuar.setText("📜  Volver al Menú");
            // Sobreescribir el handler del botón para navegar a la pantalla de derrota
            btnContinuar.setOnAction(e -> navegarAResultado(false));
        }

        if (lblPrompt != null) {
            lblPrompt.setText(victoria ? "¡Victoria!" : "Derrota...");
        }

        lblResultado.setVisible(true);

        if (btnContinuar != null) {
            btnContinuar.setVisible(true);
            btnContinuar.setManaged(true);
        }
    }

    // ── Submenú de Magia ──────────────────────────────────────────────────────

    /**
     * Abre el submenú de magia, reconstruyendo los botones según el estado actual
     * del personaje (PM disponibles, habilidades ya activas, etc.).
     *
     * <p>Se reconstruye en cada apertura para reflejar cambios en el PM que
     * pudieran haberse producido desde la última vez que se abrió el submenú.</p>
     */
    @FXML
    private void handleMagia() {
        if (combateTerminado) return;
        construirSubmenuMagia((Magico) sesion.getHeroe());
        menuBatalla.setVisible(false);
        menuBatalla.setManaged(false);
        menuMagia.setVisible(true);
        menuMagia.setManaged(true);
    }

    /**
     * Cierra el submenú de magia y vuelve al menú principal de batalla.
     */
    @FXML
    private void handleVolverMenu() {
        menuMagia.setVisible(false);
        menuMagia.setManaged(false);
        menuBatalla.setVisible(true);
        menuBatalla.setManaged(true);
    }

    /**
     * Construye dinámicamente los botones del submenú de magia a partir del
     * catálogo de habilidades del personaje {@link Magico}.
     *
     * <p>Para la habilidad especial (la que coincide con {@link Heroe#getNombreHabilidad()})
     * siempre se muestra como activable.</p>
     *
     * <p>Para las habilidades adicionales (Escudo Arcano, Bendición Sagrada…) se
     * aplica la siguiente lógica de estado:</p>
     * <ul>
     *   <li><b>Ya activa</b>: botón deshabilitado con texto "(activa)".</li>
     *   <li><b>PM suficientes</b>: botón habilitado con el coste en PM.</li>
     *   <li><b>PM insuficientes</b>: botón deshabilitado con "(PM insuf.)".</li>
     *   <li><b>Sin implementación de combate</b> (coste = 0): botón bloqueado con 🔒.</li>
     * </ul>
     *
     * <p>Cada botón tiene un {@link Tooltip} con la descripción de la habilidad.</p>
     *
     * @param magico personaje mágico del que se obtiene el catálogo de habilidades
     */
    private void construirSubmenuMagia(Magico magico) {
        contenedorHabilidades.getChildren().clear();

        String nombreEspecial = magico.getNombreHabilidad();

        for (String[] h : magico.getHabilidadesMagicas()) {
            String  nombre      = h[0];
            String  descripcion = h[1];
            boolean esEspecial  = nombre.equals(nombreEspecial);

            Button btn = new Button();
            btn.getStyleClass().add("btn-batalla-barra");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(36);
            btn.setMinHeight(32);
            VBox.setVgrow(btn, javafx.scene.layout.Priority.ALWAYS);

            Tooltip tip = new Tooltip(descripcion);
            tip.setWrapText(true);
            tip.setMaxWidth(210);
            btn.setTooltip(tip);

            if (esEspecial) {
                // La habilidad especial cuesta la mitad del PM máximo (mínimo 1)
                int costeEspecial = Math.max(1, magico.getPmMax() / 2);
                boolean tienePmEspecial = magico.getPm() >= costeEspecial;

                if (tienePmEspecial) {
                    btn.setText("✨ " + nombre.toUpperCase() + "  (−" + costeEspecial + " PM)");
                    btn.setOnAction(e -> {
                        handleVolverMenu();
                        ejecutarTurno(AccionHeroe.HABILIDAD);
                    });
                } else {
                    // PM insuficientes: mostrar como bloqueada
                    btn.setText("✨ " + nombre.toUpperCase() + "  (PM insuf.)");
                    btn.setDisable(true);
                }
            } else {
                // Habilidad adicional: determinar su estado actual
                int     coste      = magico.getCostePmHabilidad(nombre);
                boolean estaActiva = magico.isHabilidadAdicionalActiva(nombre);
                boolean tienePm    = magico.getPm() >= coste;

                if (estaActiva) {
                    btn.setText("✅ " + nombre.toUpperCase() + "  (activa)");
                    btn.setDisable(true);
                } else if (coste > 0 && tienePm) {
                    btn.setText("✨ " + nombre.toUpperCase() + "  (−" + coste + " PM)");
                    final String nombreFinal = nombre;
                    btn.setOnAction(e -> {
                        handleVolverMenu();
                        ejecutarHabilidadMagicaAdicional(nombreFinal);
                    });
                } else if (coste > 0) {
                    btn.setText("✨ " + nombre.toUpperCase() + "  (PM insuf.)");
                    btn.setDisable(true);
                } else {
                    // Habilidad sin implementación activa en combate (informativa o pasiva)
                    btn.setText("🔒 " + nombre.toUpperCase());
                    Tooltip tipBloq = new Tooltip(descripcion
                            + "\n\n(Habilidad no disponible en combate directo)");
                    tipBloq.setWrapText(true);
                    tipBloq.setMaxWidth(210);
                    btn.setTooltip(tipBloq);
                    btn.setDisable(true);
                }
            }

            contenedorHabilidades.getChildren().add(btn);
        }
    }

    // ── Submenú de Objetos ────────────────────────────────────────────────────

    /**
     * Cierra el submenú de objetos y vuelve al menú principal de batalla.
     */
    @FXML
    private void handleVolverMenuObjetos() {
        menuObjetos.setVisible(false);
        menuObjetos.setManaged(false);
        menuBatalla.setVisible(true);
        menuBatalla.setManaged(true);
    }

    /**
     * Usa una poción de curación desde el submenú de objetos.
     * Cierra el submenú y ejecuta el turno con {@link AccionHeroe#POCION}.
     */
    @FXML
    private void handleUsarPocionCuracion() {
        handleVolverMenuObjetos();
        ejecutarTurno(AccionHeroe.POCION);
    }

    /**
     * Usa una poción mágica desde el submenú de objetos.
     * Cierra el submenú y ejecuta el turno con {@link AccionHeroe#POCION_MAGICA}.
     */
    @FXML
    private void handleUsarPocionMagica() {
        handleVolverMenuObjetos();
        ejecutarTurno(AccionHeroe.POCION_MAGICA);
    }

    // ── Submenú de Habilidades del Guerrero ───────────────────────────────────

    /**
     * Abre el submenú de habilidades del Guerrero, reconstruyendo los botones
     * según el estado actual (Postura de Hierro activa o no).
     */
    @FXML
    private void handleHabilidades() {
        if (combateTerminado) return;
        construirSubmenuHabilidades((Guerrero) sesion.getHeroe());
        menuBatalla.setVisible(false);
        menuBatalla.setManaged(false);
        menuHabilidades.setVisible(true);
        menuHabilidades.setManaged(true);
    }

    /**
     * Cierra el submenú de habilidades del Guerrero y vuelve al menú principal.
     */
    @FXML
    private void handleVolverMenuHabilidades() {
        menuHabilidades.setVisible(false);
        menuHabilidades.setManaged(false);
        menuBatalla.setVisible(true);
        menuBatalla.setManaged(true);
    }

    /**
     * Construye dinámicamente los botones del submenú de habilidades del Guerrero.
     *
     * <p>Habilidades disponibles:</p>
     * <ul>
     *   <li><b>Golpe Devastador</b> (habilidad especial): siempre activable.</li>
     *   <li><b>Postura de Hierro</b>: activable solo una vez por fase.
     *       Si ya está activa, el botón se muestra deshabilitado con "(activa)".</li>
     * </ul>
     *
     * <p>Cada botón tiene un {@link Tooltip} con la descripción de la habilidad.</p>
     *
     * @param guerrero guerrero del que se obtiene el catálogo de habilidades
     */
    private void construirSubmenuHabilidades(Guerrero guerrero) {
        contenedorHabilidadesGuerrero.getChildren().clear();

        for (String[] h : guerrero.getHabilidadesGuerrero()) {
            String nombre      = h[0];
            String descripcion = h[1];
            boolean esGolpe    = nombre.equals(guerrero.getNombreHabilidad());

            Button btn = new Button();
            btn.getStyleClass().add("btn-batalla-barra");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(36);
            btn.setMinHeight(32);
            VBox.setVgrow(btn, javafx.scene.layout.Priority.ALWAYS);

            Tooltip tip = new Tooltip(descripcion);
            tip.setWrapText(true);
            tip.setMaxWidth(210);
            btn.setTooltip(tip);

            if (esGolpe) {
                // Golpe Devastador: siempre disponible
                btn.setText("⚔️ " + nombre.toUpperCase());
                btn.setOnAction(e -> {
                    handleVolverMenuHabilidades();
                    ejecutarTurno(AccionHeroe.HABILIDAD);
                });
            } else {
                // Postura de Hierro: usable solo una vez por combate
                boolean yaActiva = guerrero.isPosturaDeHierroActiva();
                if (yaActiva) {
                    btn.setText("✅ " + nombre.toUpperCase() + "  (activa)");
                    btn.setDisable(true);
                } else {
                    btn.setText("🛡️ " + nombre.toUpperCase());
                    btn.setOnAction(e -> {
                        handleVolverMenuHabilidades();
                        ejecutarTurno(AccionHeroe.HABILIDAD_GUERRERO_2);
                    });
                }
            }

            contenedorHabilidadesGuerrero.getChildren().add(btn);
        }
    }

    /**
     * Actualiza el texto y el estado (habilitado/deshabilitado) de los botones
     * del submenú de objetos según el inventario actual.
     * Se llama justo antes de mostrar el submenú para que el stock mostrado
     * esté siempre actualizado.
     */
    private void actualizarSubmenuObjetos() {
        if (pocionesRestantes > 0) {
            btnPocionCuracion.setText("🧪 POCIÓN DE CURACIÓN  ×" + pocionesRestantes);
            btnPocionCuracion.setDisable(false);
        } else {
            btnPocionCuracion.setText("🧪 POCIÓN DE CURACIÓN  (agotadas)");
            btnPocionCuracion.setDisable(true);
        }

        if (pocionesMagicasRestantes > 0) {
            btnPocionMagica.setText("🔮 POCIÓN MÁGICA  ×" + pocionesMagicasRestantes);
            btnPocionMagica.setDisable(false);
        } else {
            btnPocionMagica.setText("🔮 POCIÓN MÁGICA  (agotadas)");
            btnPocionMagica.setDisable(true);
        }
    }

    /**
     * Ejecuta una habilidad mágica adicional del personaje (Escudo Arcano,
     * Bendición Sagrada, etc.) siguiendo este flujo de turno:
     * <ol>
     *   <li>Verificar y gastar los PM necesarios. Si no alcanza, muestra aviso y aborta.</li>
     *   <li>Aplicar el efecto mediante {@link Magico#ejecutarHabilidadAdicional}.</li>
     *   <li>Si el efecto devuelve {@code null} (ya activa u otro motivo),
     *       reembolsar los PM y mostrar aviso.</li>
     *   <li>El enemigo contraataca (la habilidad ocupa el turno del héroe).</li>
     *   <li>Actualizar barras y comprobar si el combate terminó.</li>
     * </ol>
     *
     * @param nombre nombre de la habilidad adicional a ejecutar (debe estar en el catálogo del Magico)
     */
    private void ejecutarHabilidadMagicaAdicional(String nombre) {
        if (combateTerminado) return;
        Magico magico = (Magico) sesion.getHeroe();

        // 1. Verificar y gastar PM
        int coste = magico.getCostePmHabilidad(nombre);
        if (!magico.gastarPm(coste)) {
            agregarLog("⚠ PM insuficientes para usar " + nombre
                    + ". (PM: " + magico.getPm() + "/" + magico.getPmMax() + ")");
            return;
        }

        // 2. Aplicar el efecto (el objetivo puede ser el enemigo o el propio héroe según la habilidad)
        Personaje objetivo = motor.getEnemigo();
        String efecto = magico.ejecutarHabilidadAdicional(nombre, objetivo);
        if (efecto == null) {
            // La habilidad no tuvo efecto (ya activa u otro motivo): reembolsar PM
            magico.restaurarPmParcial(coste);
            agregarLog("⚠ " + nombre + " no tuvo efecto.");
            return;
        }
        agregarLog(efecto);

        // 3. Contraataque del enemigo (el turno del héroe se consume usando la habilidad)
        List<String> contraataque = motor.ejecutarContraataqueEnemigo();
        contraataque.forEach(this::agregarLog);

        // Animación del contraataque (el héroe recibe el golpe)
        if (!contraataque.isEmpty()) {
            PauseTransition espera = new PauseTransition(Duration.millis(200));
            espera.setOnFinished(ev -> animarGolpe(imgHeroe));
            espera.play();
        }

        agregarLog("");
        actualizarBarraHeroe();
        actualizarBarraEnemigo();
        actualizarBarraPm();
        if (motor.getEnemigo().tienePmMax()) actualizarBarraPmEnemigo();

        // 4. Comprobar fin de combate (el héroe podría haber muerto por el contraataque)
        ResultadoCombate resultado = motor.getResultado();
        if (resultado != ResultadoCombate.EN_CURSO) {
            combateTerminado = true;
            procesarFinCombate(resultado);
        }
    }

    // ── Guardar partida ───────────────────────────────────────────────────────

    /**
     * Guarda o actualiza la partida en BD según si ya tiene un id asignado.
     *
     * <p>Si es la primera vez que se guarda ({@link GameSession#getPartida()} es null),
     * realiza un INSERT y asigna el id generado al objeto {@link Partida} de la sesión.
     * En llamadas posteriores, realiza un UPDATE con la fase y el HP actuales.</p>
     *
     * <p>También sincroniza el HP del personaje en la tabla {@code personajes}
     * para que al reanudar la partida se restaure el HP correcto.</p>
     *
     * @throws Exception si ocurre un error de acceso a la BD
     */
    private void guardarOActualizarPartida() throws Exception {
        Heroe heroe = sesion.getHeroe();

        if (sesion.getPartida() == null) {
            // Primera vez: crear la fila en BD
            Partida p = new Partida(
                sesion.getJugador().getId(),
                heroe.getId(),
                sesion.getFaseActual(),
                heroe.getPuntosGolpe()
            );
            PartidaDAO.insertar(p);
            sesion.setPartida(p); // asociar la partida a la sesión para llamadas futuras
        } else {
            // Actualizar la fila existente con la fase y HP actuales
            Partida p = sesion.getPartida();
            p.setFaseActual(sesion.getFaseActual());
            p.setHpActual(heroe.getPuntosGolpe());
            PartidaDAO.actualizar(p);
        }
        // Sincronizar HP del personaje en su propia tabla
        PersonajeDAO.actualizarHp(heroe.getId(), heroe.getPuntosGolpe());
    }

    /**
     * Wrapper sin checked exception para llamar desde flujos donde no se puede
     * propagar el error (p. ej. al huir). Los errores se imprimen en consola.
     */
    private void guardarPartida() {
        try {
            guardarOActualizarPartida();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    /**
     * Detiene la música, actualiza el estado final de la partida en BD y navega
     * a la pantalla de resultado ({@code Resultado.fxml}).
     *
     * <p>Antes de navegar marca la partida como {@code COMPLETADA} (victoria) o
     * {@code DERROTA} según el parámetro recibido, y guarda el HP final del héroe.</p>
     *
     * <p>El resultado visual se inyecta en {@link ResultadoController} mediante
     * {@link ResultadoController#mostrarResultado(GameSession, boolean)}.</p>
     *
     * @param victoria {@code true} para victoria total; {@code false} para derrota
     */
    private void navegarAResultado(boolean victoria) {
        detenerMusica();
        try {
            if (sesion.getPartida() != null) {
                sesion.getPartida().setEstado(victoria
                    ? modelo.Partida.Estado.COMPLETADA
                    : modelo.Partida.Estado.DERROTA);
                sesion.getPartida().setHpActual(sesion.getHeroe().getPuntosGolpe());
                PartidaDAO.actualizar(sesion.getPartida());
            }

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/vistas/Resultado.fxml")
            );
            Parent root = loader.load();
            ResultadoController siguiente = loader.getController();
            siguiente.mostrarResultado(sesion, victoria);
            Stage stage = (Stage) btnAtacar.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Detiene la música y navega al menú principal ({@code MenuPrincipal.fxml}).
     * Se usa al huir del combate (la partida ya ha sido guardada antes de llamar esto).
     */
    private void navegarAMenu() {
        detenerMusica();
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
            Stage stage = (Stage) btnAtacar.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    /**
     * Refresca la barra de vida del héroe y el label numérico de HP.
     * Aplica el color correspondiente según el porcentaje de vida restante.
     */
    private void actualizarBarraHeroe() {
        Heroe h = sesion.getHeroe();
        double pct = h.getPorcentajeVida();
        barraVidaHeroe.setProgress(pct);
        lblHpHeroe.setText(h.getPuntosGolpe() + " / " + h.getPuntosGolpeMax() + " HP");
        colorearBarra(barraVidaHeroe, pct);
    }

    /**
     * Refresca la barra de PM del héroe mágico y el label numérico.
     * El color cambia de morado brillante ({@code #7c6fcd}) a morado oscuro
     * ({@code #4a3d8f}) cuando los PM bajan del 30 %.
     *
     * <p>Solo debe llamarse cuando el héroe es instancia de {@link Magico}.</p>
     */
    private void actualizarBarraPm() {
        if (!(sesion.getHeroe() instanceof Magico)) return;
        Magico m = (Magico) sesion.getHeroe();
        double pct = m.getPorcentajePm();
        barraPoderMagico.setProgress(pct);
        lblPmHeroe.setText(m.getPm() + " / " + m.getPmMax() + " PM");
        String color = pct > 0.3 ? "#7c6fcd" : "#4a3d8f";
        barraPoderMagico.setStyle("-fx-accent: " + color + ";");
    }

    /**
     * Refresca la barra de vida del enemigo y el label numérico de HP.
     * Aplica el mismo esquema de color que la barra del héroe.
     */
    private void actualizarBarraEnemigo() {
        Enemigo e = motor.getEnemigo();
        double pct = e.getPorcentajeVida();
        barraVidaEnemigo.setProgress(pct);
        lblHpEnemigo.setText(e.getPuntosGolpe() + " / " + e.getPuntosGolpeMax() + " HP");
        colorearBarra(barraVidaEnemigo, pct);
    }

    /**
     * Refresca la barra de PM del enemigo mágico (Saga, Dragón) y el label numérico.
     * Mismo esquema de color que la barra de PM del héroe mágico.
     *
     * <p>Solo debe llamarse cuando {@code motor.getEnemigo().tienePmMax()} es {@code true}.</p>
     */
    private void actualizarBarraPmEnemigo() {
        Enemigo e = motor.getEnemigo();
        double pct = e.getPorcentajePm();
        barraPmEnemigo.setProgress(pct);
        lblPmEnemigo.setText(e.getPm() + " / " + e.getPmMax() + " PM");
        String color = pct > 0.3 ? "#7c6fcd" : "#4a3d8f";
        barraPmEnemigo.setStyle("-fx-accent: " + color + ";");
    }

    /**
     * Aplica un color dinámico a una barra de progreso según el porcentaje:
     * <ul>
     *   <li>> 50 %: verde ({@code #4caf50})</li>
     *   <li>25 % – 50 %: naranja ({@code #ff9800})</li>
     *   <li>≤ 25 %: rojo ({@code #e05555})</li>
     * </ul>
     *
     * @param barra barra de progreso a colorear
     * @param pct   porcentaje actual (0.0 – 1.0)
     */
    private void colorearBarra(ProgressBar barra, double pct) {
        String color = pct > 0.5 ? "#4caf50" : pct > 0.25 ? "#ff9800" : "#e05555";
        barra.setStyle("-fx-accent: " + color + ";");
    }

    /**
     * Añade una línea al log de combate seguida de un salto de línea.
     * El {@link TextArea} hace scroll automático al final gracias a {@code appendText}.
     *
     * @param mensaje texto a mostrar en el log
     */
    private void agregarLog(String mensaje) {
        txtLog.appendText(mensaje + "\n");
    }

    /**
     * Aplica un efecto de "temblor" horizontal al nodo indicado, simulando
     * que recibe un golpe. El nodo oscila ±8 px en horizontal durante ~250 ms
     * y vuelve a su posición original al terminar.
     *
     * <p>La secuencia de movimiento es: izquierda → derecha → izquierda → derecha → centro,
     * con 50 ms por paso. Se usa {@link SequentialTransition} para encadenarlos.</p>
     *
     * @param objetivo nodo JavaFX que debe sacudirse (puede ser {@link ImageView} o {@link Label})
     */
    private void animarGolpe(Node objetivo) {
        if (objetivo == null) return;

        int    desplazamiento = 8;     // píxeles a cada lado
        double duracionPaso   = 50;    // ms por sacudida

        TranslateTransition izq1 = new TranslateTransition(Duration.millis(duracionPaso), objetivo);
        izq1.setByX(-desplazamiento);
        TranslateTransition der1 = new TranslateTransition(Duration.millis(duracionPaso), objetivo);
        der1.setByX(desplazamiento * 2);
        TranslateTransition izq2 = new TranslateTransition(Duration.millis(duracionPaso), objetivo);
        izq2.setByX(-desplazamiento * 2);
        TranslateTransition der2 = new TranslateTransition(Duration.millis(duracionPaso), objetivo);
        der2.setByX(desplazamiento * 2);
        TranslateTransition centro = new TranslateTransition(Duration.millis(duracionPaso), objetivo);
        centro.setByX(-desplazamiento); // volver al centro

        SequentialTransition secuencia =
                new SequentialTransition(izq1, der1, izq2, der2, centro);
        secuencia.play();
    }

    /**
     * Inicia la música de fondo del combate.
     * Detiene cualquier música previa antes de arrancar la nueva (importante
     * al cambiar de fase, donde la música puede cambiar).
     *
     * <p>La música se reproduce en bucle infinito al 60 % de volumen:</p>
     * <ul>
     *   <li>Fases 1-3: {@code /recursos/audio/Battle.mp3}</li>
     *   <li>Fase 4 (jefe final): {@code /recursos/audio/finalBoss.mp3}</li>
     * </ul>
     *
     * <p>Si el archivo de audio no existe, se ignora el error silenciosamente
     * (el juego funciona sin música).</p>
     *
     * @param esFaseFinal {@code true} para cargar la música del jefe final
     */
    private void iniciarMusica(boolean esFaseFinal) {
        // Detener cualquier música anterior (importante al cambiar de fase)
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        try {
            String archivo = esFaseFinal ? "/recursos/audio/finalBoss.mp3"
                                         : "/recursos/audio/Battle.mp3";
            URL recurso = getClass().getResource(archivo);
            if (recurso == null) return;
            mediaPlayer = new MediaPlayer(new Media(recurso.toExternalForm()));
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.6);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Detiene la música de fondo si está reproduciéndose.
     * Se llama siempre antes de navegar a otra pantalla para evitar que la
     * música de combate solape con la música del menú o la pantalla de resultado.
     */
    private void detenerMusica() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 