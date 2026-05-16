package controlador;

import dao.CombateDAO;
import dao.JugadorDAO;
import dao.PartidaDAO;
import dao.PersonajeDAO;
import modelo.*;
import motor.MotorCombate;
import motor.MotorCombate.ResultadoCombate;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.List;
import java.util.Random;
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

    /** Imagen del sprite del enemigo. Se anima con {@link #animarGolpe(Node)} al recibir daño. */
    @FXML private ImageView   imgEnemigo;

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

    // ── FXML: barra de Energía del Guerrero ──────────────────────────────────
    /**
     * Fila con etiqueta "EN" y barra de Energía del Guerrero.
     * Visible solo cuando el héroe es un {@link Guerrero}.
     */
    @FXML private HBox        filaEnergia;

    /** Fila con el texto numérico de Energía. Visible solo para el Guerrero. */
    @FXML private HBox        filaNumEnergia;

    /** Barra de progreso que representa la Energía actual del Guerrero (naranja). */
    @FXML private ProgressBar barraEnergia;

    /** Label numérico de Energía. Formato: "EN_actual / EN_max EN". */
    @FXML private Label       lblEnergiaHeroe;

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

    // ── FXML: overlay de diálogo de huida ────────────────────────────────────
    /**
     * StackPane semiopaco que cubre el combate mientras el jugador decide si huir.
     * Se muestra al pulsar HUIR y desaparece al confirmar o cancelar.
     */
    @FXML private StackPane overlayHuida;

    /** Línea base "Probabilidad base: 40%" del desglose. */
    @FXML private Label lblHuidaBase;

    /** Modificador de HP del héroe (siempre visible). */
    @FXML private Label lblHuidaVida;

    /** Modificador de clase del héroe (oculto si el modificador es 0). */
    @FXML private Label lblHuidaClase;

    /** Modificador según el tipo de enemigo (oculto si el modificador es 0). */
    @FXML private Label lblHuidaEnemigo;

    /** Label "🎲 Probabilidad de éxito: N%" destacado en el panel. */
    @FXML private Label lblHuidaProbFinal;

    /** Botón "✅ Intentar huir" del overlay de huida. */
    @FXML private Button btnConfirmarHuida;

    /** Botón "❌ Cancelar" del overlay de huida. */
    @FXML private Button btnCancelarHuida;

    // ── FXML: overlay de descanso (tras huida exitosa) ───────────────────────
    /**
     * StackPane semiopaco que cubre la pantalla mientras el héroe descansa
     * tras una huida exitosa. Muestra la animación de recuperación de HP (y PM)
     * antes de navegar al menú principal.
     */
    @FXML private StackPane overlayDescanso;

    /** Barra de progreso animada que refleja la vida recuperada (0 → porcentaje final). */
    @FXML private ProgressBar barraDescansoHp;

    /** Label "+N HP" que muestra cuánta vida se recuperó. */
    @FXML private Label lblDescansoHp;

    /**
     * Fila entera del panel de maná durante el descanso.
     * Se hace visible solo para personajes {@link Magico}.
     */
    @FXML private VBox filaDescansopm;

    /** Barra de progreso animada del maná recuperado durante el descanso. */
    @FXML private ProgressBar barraDescansoPm;

    /** Label "+N PM" que muestra cuánto maná se recuperó. */
    @FXML private Label lblDescansoPm;

    /** Barra de progreso general del overlay de descanso (avanza de 0 a 1 durante toda la pantalla). */
    @FXML private ProgressBar barCargaDescanso;

    // ── FXML: pantalla de carga entre fases ──────────────────────────────────
    /**
     * StackPane opaco que cubre toda la ventana entre fases.
     * Se muestra al pulsar "Siguiente Fase" y desaparece automáticamente
     * cuando la barra de progreso llega al 100 %.
     */
    @FXML private StackPane   pantallaEntrefase;

    /** Panel de partículas doradas animadas dentro del overlay de carga. */
    @FXML private Pane        panelParticulasCarga;

    /** Emoji grande que identifica la fase (⚔ para fases normales, 🐉 para el jefe). */
    @FXML private Label       lblCargaIcono;

    /** Label "FASE N" en el overlay de carga. */
    @FXML private Label       lblCargaFase;

    /** Label "— MAZMORRA —" o "— JEFE FINAL —" según la fase. */
    @FXML private Label       lblCargaSubtitulo;

    /** Frase temática / consejo de la fase mostrada durante la carga. */
    @FXML private Label       lblCargaConsejo;

    /** Barra de progreso animada que mide la duración del overlay de carga. */
    @FXML private ProgressBar barCarga;

    // ── Estado ────────────────────────────────────────────────────────────────
    /** Sesión de juego activa; contiene jugador, héroe, fase y referencia a la partida en BD. */
    private GameSession  sesion;

    /**
     * Probabilidad de huida calculada en {@link #handleHuir()} y usada luego
     * en {@link #handleConfirmarHuida()} cuando el jugador pulsa "Intentar huir".
     * Se almacena como campo porque los dos métodos son handlers FXML separados.
     */
    private int probHuidaActual = 0;

    /** Motor de combate por turnos; gestiona ataques, contraataques y resultado. */
    private MotorCombate motor;

    /**
     * Indica si el combate actual ha terminado (victoria o derrota).
     * Se usa como guardia en los handlers para ignorar pulsaciones tras el fin.
     */
    private boolean      combateTerminado = false;

    /** Reproductor de música de fondo; se cambia entre fases (Battle.mp3 / finalBoss.mp3). */
    private MediaPlayer mediaPlayer;

    /** Clip de sonido corto que suena al pasar el cursor sobre cualquier botón de combate. */
    private AudioClip sonidoHover;

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
     * Porcentaje de vida (y maná, si aplica) que se recupera al descansar
     * tras una huida exitosa antes de volver al menú principal.
     */
    private static final int RECUPERACION_HUIDA_PCT = 25;

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
        inicializarSonidoHover();
        configurarSonidoBotones();
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

        // Si hay una partida guardada con un enemigo activo (HP > 0), restaurarlo;
        // en caso contrario (nueva fase o enemigo derrotado) generarlo aleatoriamente.
        Partida partidaActual = sesion.getPartida();
        Enemigo enemigo;
        if (partidaActual != null
                && partidaActual.getTipoEnemigo() != null
                && partidaActual.getHpEnemigo() > 0) {
            enemigo = MotorCombate.generarEnemigoDeTipo(partidaActual.getTipoEnemigo());
            enemigo.setPuntosGolpe(partidaActual.getHpEnemigo());
            enemigo.setPm(partidaActual.getPmEnemigo()); // 0 si no usa magia, correcto igualmente
        } else {
            enemigo = MotorCombate.generarEnemigo(fase);
        }

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

        // ── Enemigo: nombre, imagen y barra de vida
        lblNombreEnemigo.setText(enemigo.getNombre() + " (" + enemigo.getTipo() + ")");
        try {
            Image imgEnemSrc = new Image(getClass().getResourceAsStream(enemigo.getRutaImagen()));
            imgEnemigo.setImage(imgEnemSrc);
        } catch (Exception e) {
            // Si la imagen no carga, el juego continúa sin el sprite (no es un error fatal)
            e.printStackTrace();
        }
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

        // ── Barra de Energía (solo para el Guerrero)
        filaEnergia.setVisible(esGuerrero);
        filaEnergia.setManaged(esGuerrero);
        filaNumEnergia.setVisible(esGuerrero);
        filaNumEnergia.setManaged(esGuerrero);
        if (esGuerrero) actualizarBarraEnergia();

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
     * Maneja el botón "HUIR": calcula la probabilidad de huida y muestra el
     * overlay in-game con el desglose de modificadores.
     *
     * <p>La probabilidad base (40 %) se modifica por tres factores acumulativos:</p>
     * <ul>
     *   <li><b>HP del héroe:</b> &lt;25 % → +20 %; &lt;50 % → +10 %; ≥50 % → ±0 %.</li>
     *   <li><b>Clase:</b> Guerrero −10 % (orgullo); Mago +5 % (escurridizo); Clérigo ±0 %.</li>
     *   <li><b>Enemigo:</b> Goblin +10 %; Ogro −5 %; Saga −10 %; Dragón −20 %.</li>
     * </ul>
     *
     * <p>La probabilidad resultante se clampea a [5 %, 90 %] y se almacena en
     * {@link #probHuidaActual} para que {@link #handleConfirmarHuida()} la use
     * al tirar el dado.</p>
     */
    @FXML
    private void handleHuir() {
        if (combateTerminado) return;

        Heroe   heroe   = sesion.getHeroe();
        Enemigo enemigo = motor.getEnemigo();

        // ── Modificador por HP ────────────────────────────────────────────────
        int pctVida = (int)((heroe.getPuntosGolpe() * 100.0) / heroe.getPuntosGolpeMax());
        int modVida; String msgVida;
        if (pctVida < 25) {
            modVida = 20; msgVida = "¡Estás malherido! Mayor probabilidad de huir.  (+20%)";
        } else if (pctVida < 50) {
            modVida = 10; msgVida = "Estás herido, pero aún puedes luchar.  (+10%)";
        } else {
            modVida = 0;  msgVida = "Aún puedes seguir luchando...  (±0%)";
        }

        // ── Modificador por clase ─────────────────────────────────────────────
        int modClase; String msgClase;
        if (heroe instanceof Guerrero) {
            modClase = -10; msgClase = "Los guerreros no huyen fácilmente.  (−10%)";
        } else if (heroe instanceof Mago) {
            modClase = 5;   msgClase = "Los magos son escurridizos.  (+5%)";
        } else {
            modClase = 0;   msgClase = null; // Clérigo: sin modificador
        }

        // ── Modificador por tipo de enemigo ───────────────────────────────────
        int modEnemigo; String msgEnemigo;
        switch (enemigo.getTipo().toUpperCase()) {
            case "GOBLIN": modEnemigo =  10; msgEnemigo = "Los goblins son fáciles de esquivar.  (+10%)"; break;
            case "OGRO":   modEnemigo =  -5; msgEnemigo = "Los ogros son implacables.  (−5%)";            break;
            case "SAGA":   modEnemigo = -10; msgEnemigo = "La Saga controla el campo de batalla.  (−10%)"; break;
            case "DRAGON": modEnemigo = -20; msgEnemigo = "¡Nadie escapa de un Dragón fácilmente!  (−20%)"; break;
            default:       modEnemigo =   0; msgEnemigo = null;
        }

        probHuidaActual = Math.max(5, Math.min(90, 40 + modVida + modClase + modEnemigo));

        // ── Rellenar labels del overlay ───────────────────────────────────────
        lblHuidaBase.setText("Probabilidad base: 40%");
        lblHuidaVida.setText(msgVida);

        if (msgClase != null) {
            lblHuidaClase.setText(msgClase);
            lblHuidaClase.setVisible(true);
            lblHuidaClase.setManaged(true);
        } else {
            lblHuidaClase.setVisible(false);
            lblHuidaClase.setManaged(false);
        }

        if (msgEnemigo != null) {
            lblHuidaEnemigo.setText(msgEnemigo);
            lblHuidaEnemigo.setVisible(true);
            lblHuidaEnemigo.setManaged(true);
        } else {
            lblHuidaEnemigo.setVisible(false);
            lblHuidaEnemigo.setManaged(false);
        }

        lblHuidaProbFinal.setText("🎲 Probabilidad de éxito: " + probHuidaActual + "%");

        // ── Mostrar overlay ───────────────────────────────────────────────────
        overlayHuida.setVisible(true);
        overlayHuida.setManaged(true);
    }

    /**
     * Ejecuta el intento de huida cuando el jugador pulsa "✅ Intentar huir".
     *
     * <p>Cierra el overlay, tira un dado (1–100) y lo compara con {@link #probHuidaActual}:</p>
     * <ul>
     *   <li><b>Éxito</b> (tirada ≤ prob): mensaje en log, botones bloqueados, navega al menú.</li>
     *   <li><b>Fracaso</b> (tirada &gt; prob): el enemigo ataca con la mitad de su poder como
     *       penalización; si el héroe muere, se activa la derrota.</li>
     * </ul>
     */
    @FXML
    private void handleConfirmarHuida() {
        overlayHuida.setVisible(false);
        overlayHuida.setManaged(false);

        Heroe   heroe   = sesion.getHeroe();
        Enemigo enemigo = motor.getEnemigo();
        int tirada = new Random().nextInt(100) + 1;

        if (tirada <= probHuidaActual) {
            // ── Éxito ─────────────────────────────────────────────────────────
            agregarLog("🏃 " + heroe.getNombre() + " intenta huir..."
                    + "  (tirada: " + tirada + " ≤ " + probHuidaActual + ")");
            agregarLog("✅ ¡Huida exitosa! Escapas del combate.");
            agregarLog("");

            btnAtacar.setDisable(true);
            btnHabilidad.setDisable(true);
            btnMagia.setDisable(true);
            btnHabilidades.setDisable(true);
            btnObjetos.setDisable(true);
            btnHuir.setDisable(true);

            // Breve pausa antes de mostrar la pantalla de descanso
            PauseTransition pausa = new PauseTransition(Duration.millis(600));
            pausa.setOnFinished(e -> mostrarPantallaDescanso());
            pausa.play();

        } else {
            // ── Fracaso: penalización (golpe por la espalda, sin reducción por defensa) ──
            agregarLog("🏃 " + heroe.getNombre() + " intenta huir..."
                    + "  (tirada: " + tirada + " > " + probHuidaActual + ")");
            agregarLog("❌ ¡No has podido huir! El enemigo te alcanza.");

            int danioPenalizacion = Math.max(1, enemigo.getPoder() / 2);
            heroe.recibirDanio(danioPenalizacion);
            agregarLog("💥 " + enemigo.getNombre() + " te golpea por la espalda por "
                    + danioPenalizacion + " de daño!");
            agregarLog("   " + heroe.getNombre() + ": " + heroe.getPuntosGolpe()
                    + " / " + heroe.getPuntosGolpeMax() + " HP");
            agregarLog("");

            PauseTransition espera = new PauseTransition(Duration.millis(200));
            espera.setOnFinished(e -> animarGolpe(imgHeroe));
            espera.play();

            actualizarBarraHeroe();
            if (heroe instanceof Magico) actualizarBarraPm();

            if (!heroe.estaVivo()) {
                combateTerminado = true;
                procesarFinCombate(ResultadoCombate.DERROTA);
            }
        }
    }

    /**
     * Cancela el intento de huida cerrando el overlay.
     * El combate continúa sin consumir el turno del héroe.
     */
    @FXML
    private void handleCancelarHuida() {
        overlayHuida.setVisible(false);
        overlayHuida.setManaged(false);
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
            // Checkpoint: guardar nueva fase y estadísticas actuales del héroe
            guardarAlAvanzarFase();
            // Mostrar pantalla de carga; al terminar, llamará a prepararCombate()
            detenerMusica();
            mostrarPantallaCarga();
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
    /**
     * Deshabilita todos los botones de acción de combate mientras se procesa
     * un turno (animación + pausa), evitando que el jugador pueda enviar
     * múltiples acciones simultáneas.
     */
    private void desactivarBotonesCombate() {
        btnAtacar.setDisable(true);
        btnMagia.setDisable(true);
        btnHabilidades.setDisable(true);
        btnHabilidad.setDisable(true);
        btnObjetos.setDisable(true);
        btnHuir.setDisable(true);
    }

    /**
     * Reactiva los botones de acción tras completar el turno, respetando el estado
     * real del inventario y PM del héroe.
     */
    private void activarBotonesCombate() {
        btnAtacar.setDisable(false);
        btnHuir.setDisable(false);
        btnMagia.setDisable(false);
        btnHabilidades.setDisable(false);
        btnHabilidad.setDisable(false);
        // Objetos: solo si quedan pociones
        btnObjetos.setDisable(pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0);
        // Re-actualizar submenús con el estado actualizado (PM / EN)
        if (sesion.getHeroe() instanceof Magico)
            construirSubmenuMagia((Magico) sesion.getHeroe());
        if (sesion.getHeroe() instanceof Guerrero)
            construirSubmenuHabilidades((Guerrero) sesion.getHeroe());
    }

    /**
     * Actualiza todas las barras de HP y PM de ambos combatientes.
     * Se llama al final de cada fase de turno para reflejar el estado actual.
     */
    private void actualizarTodasLasBarras() {
        actualizarBarraHeroe();
        actualizarBarraEnemigo();
        if (sesion.getHeroe() instanceof Magico)   actualizarBarraPm();
        if (sesion.getHeroe() instanceof Guerrero) actualizarBarraEnergia();
        if (motor.getEnemigo().tienePmMax())       actualizarBarraPmEnemigo();
    }

    /**
     * Comprueba el resultado del motor y, si el combate terminó, lo procesa.
     * De lo contrario reactiva los botones para el siguiente turno.
     */
    private void cerrarTurno() {
        ResultadoCombate res = motor.getResultado();
        if (res != ResultadoCombate.EN_CURSO) {
            combateTerminado = true;
            procesarFinCombate(res);
        } else {
            activarBotonesCombate();
        }
    }

    /**
     * Procesa un turno completo según la acción elegida por el jugador.
     *
     * <p>El turno se divide en dos fases separadas por una {@link PauseTransition}
     * de 750 ms para que el jugador pueda leer el resultado de su acción antes de
     * ver la reacción del enemigo:</p>
     * <ol>
     *   <li><b>Fase 1</b>: acción del héroe (ataque, habilidad, buff, poción) →
     *       log + animación + actualización de barras del enemigo.</li>
     *   <li><b>Pausa de 750 ms</b></li>
     *   <li><b>Fase 2</b>: reacción del enemigo → log + animación + actualización
     *       de barras del héroe → comprobación de resultado.</li>
     * </ol>
     *
     * <p>Los botones de acción se deshabilitan al inicio del turno y se reactivan
     * al final (si el combate continúa) para evitar acciones superpuestas.</p>
     *
     * @param accion la acción que el jugador ha elegido para este turno
     */
    private void ejecutarTurno(AccionHeroe accion) {
        if (combateTerminado) return;
        desactivarBotonesCombate();

        switch (accion) {

            // ── ATAQUE BÁSICO / HABILIDAD ESPECIAL ───────────────────────────
            case ATAQUE:
            case HABILIDAD: {
                // EN check para el Guerrero (Golpe Devastador)
                if (accion == AccionHeroe.HABILIDAD && sesion.getHeroe() instanceof Guerrero) {
                    Guerrero g = (Guerrero) sesion.getHeroe();
                    if (!g.gastarEnergia(Guerrero.COSTE_GOLPE_DEVASTADOR)) {
                        agregarLog("⚡ Energía insuficiente para Golpe Devastador"
                                + "  [EN: " + g.getEnergia() + "/" + g.getEnergiaMax() + "]");
                        actualizarBarraEnergia();
                        activarBotonesCombate();
                        return;
                    }
                    actualizarBarraEnergia(); // refleja el gasto antes del ataque
                }
                // PM check para héroes mágicos
                if (accion == AccionHeroe.HABILIDAD && sesion.getHeroe() instanceof Magico) {
                    Magico m = (Magico) sesion.getHeroe();
                    int coste = Math.max(1, m.getPmMax() / 2);
                    if (!m.gastarPm(coste)) {
                        agregarLog("⚠ PM insuficientes para usar " + m.getNombreHabilidad()
                                + "  [PM: " + m.getPm() + "/" + m.getPmMax() + "]");
                        activarBotonesCombate();
                        return;
                    }
                }

                // Fase 1: acción del héroe
                agregarLog(motor.iniciarTurno());
                motor.ejecutarAccionHeroe(accion == AccionHeroe.HABILIDAD)
                     .forEach(this::agregarLog);
                actualizarBarraEnemigo();
                if (motor.getEnemigo().tienePmMax()) actualizarBarraPmEnemigo();

                // Animar sprite enemigo (excepto curación propia del Clérigo)
                boolean atacaEnemigo = !(sesion.getHeroe() instanceof Clerigo)
                                        || accion != AccionHeroe.HABILIDAD;
                if (atacaEnemigo) animarGolpe(imgEnemigo);

                // Victoria inmediata (enemigo derrotado en fase 1)
                if (motor.haTerminado()) {
                    agregarLog("");
                    actualizarTodasLasBarras();
                    combateTerminado = true;
                    procesarFinCombate(motor.getResultado());
                    return;
                }

                // Fase 2: reacción del enemigo tras pausa
                PauseTransition pausa1 = new PauseTransition(Duration.millis(750));
                pausa1.setOnFinished(ev -> {
                    motor.ejecutarReaccionEnemigo().forEach(this::agregarLog);
                    agregarLog("");
                    if (accion == AccionHeroe.ATAQUE) regenerarEnergiaGuerrero();
                    actualizarTodasLasBarras();
                    PauseTransition anim = new PauseTransition(Duration.millis(150));
                    anim.setOnFinished(e -> animarGolpe(imgHeroe));
                    anim.play();
                    cerrarTurno();
                });
                pausa1.play();
                break;
            }

            // ── POSTURA DE HIERRO (Guerrero) ─────────────────────────────────
            case HABILIDAD_GUERRERO_2: {
                Guerrero g = (Guerrero) sesion.getHeroe();
                agregarLog(motor.iniciarTurno());

                // Guardia: postura ya activa (el botón debería estar deshabilitado)
                if (g.isPosturaDeHierroActiva()) {
                    agregarLog("  🛡️ La Postura de Hierro ya está activa.");
                    agregarLog("");
                    activarBotonesCombate();
                    break;
                }
                // EN check — se comprueba ANTES de gastar para no quedar en deuda
                if (!g.gastarEnergia(Guerrero.COSTE_POSTURA_HIERRO)) {
                    agregarLog("⚡ Energía insuficiente para Postura de Hierro"
                            + "  [EN: " + g.getEnergia() + "/" + g.getEnergiaMax() + "]");
                    agregarLog("");
                    actualizarBarraEnergia();
                    activarBotonesCombate();
                    break;
                }
                actualizarBarraEnergia(); // refleja el gasto antes de la animación

                String efecto = g.usarPosturaDeHierro();
                agregarLog("▸ " + efecto);

                PauseTransition pausa2 = new PauseTransition(Duration.millis(750));
                pausa2.setOnFinished(ev -> {
                    motor.ejecutarReaccionEnemigo().forEach(this::agregarLog);
                    agregarLog("");
                    actualizarTodasLasBarras();
                    PauseTransition anim = new PauseTransition(Duration.millis(150));
                    anim.setOnFinished(e -> animarGolpe(imgHeroe));
                    anim.play();
                    cerrarTurno();
                });
                pausa2.play();
                break;
            }

            // ── POCIÓN DE CURACIÓN ────────────────────────────────────────────
            case POCION: {
                pocionesRestantes--;
                Heroe h = sesion.getHeroe();
                int hpAntes = h.getPuntosGolpe();
                h.curar(CURACION_POCION);
                int curado = h.getPuntosGolpe() - hpAntes;

                agregarLog(motor.iniciarTurno());
                agregarLog(String.format("▸ 🧪 %s usa Poción de Curación  →  +%d HP  [%s: %d/%d HP]",
                        h.getNombre(), curado, h.getNombre(),
                        h.getPuntosGolpe(), h.getPuntosGolpeMax()));
                actualizarBarraHeroe(); // reflejar la curación de inmediato

                PauseTransition pausa3 = new PauseTransition(Duration.millis(750));
                pausa3.setOnFinished(ev -> {
                    motor.ejecutarReaccionEnemigo().forEach(this::agregarLog);
                    agregarLog("");
                    actualizarTodasLasBarras();
                    PauseTransition anim = new PauseTransition(Duration.millis(150));
                    anim.setOnFinished(e -> animarGolpe(imgHeroe));
                    anim.play();
                    cerrarTurno();
                });
                pausa3.play();
                break;
            }

            // ── POCIÓN MÁGICA ─────────────────────────────────────────────────
            case POCION_MAGICA: {
                pocionesMagicasRestantes--;
                Heroe h = sesion.getHeroe();

                agregarLog(motor.iniciarTurno());
                if (h instanceof Magico) {
                    Magico m = (Magico) h;
                    int pmAntes = m.getPm();
                    m.restaurarPmParcial(RESTAURACION_PM_POCION);
                    int restaurado = m.getPm() - pmAntes;
                    agregarLog(String.format("▸ 🔮 %s usa Poción Mágica  →  +%d PM  [%d/%d PM]",
                            h.getNombre(), restaurado, m.getPm(), m.getPmMax()));
                    actualizarBarraPm(); // reflejar la recarga de PM de inmediato
                } else {
                    agregarLog("▸ 🔮 " + h.getNombre()
                            + " usa Poción Mágica... ¡Sin PM! No hizo efecto.");
                }

                PauseTransition pausa4 = new PauseTransition(Duration.millis(750));
                pausa4.setOnFinished(ev -> {
                    motor.ejecutarReaccionEnemigo().forEach(this::agregarLog);
                    agregarLog("");
                    actualizarTodasLasBarras();
                    PauseTransition anim = new PauseTransition(Duration.millis(150));
                    anim.setOnFinished(e -> animarGolpe(imgHeroe));
                    anim.play();
                    cerrarTurno();
                });
                pausa4.play();
                break;
            }
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

        // Asegurar que la partida existe en BD para poder registrar el combate.
        // Este INSERT no es un checkpoint de progreso; el guardado real ocurre
        // en handleContinuar() → guardarAlAvanzarFase().
        asegurarPartidaCreada();
        if (sesion.getPartida() != null) {
            try {
                CombateDAO.registrar(
                    sesion.getPartida().getId(),
                    sesion.getFaseActual(),
                    motor.getEnemigo().getTipo(),
                    victoria,
                    motor.getTurno()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            agregarSonidoHover(btn);

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
        int energiaActual = guerrero.getEnergia();

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
            agregarSonidoHover(btn);

            Tooltip tip = new Tooltip(descripcion);
            tip.setWrapText(true);
            tip.setMaxWidth(220);
            btn.setTooltip(tip);

            if (esGolpe) {
                // Golpe Devastador: disponible si hay energía suficiente
                int coste = Guerrero.COSTE_GOLPE_DEVASTADOR;
                boolean puedeUsarlo = energiaActual >= coste;
                if (puedeUsarlo) {
                    btn.setText("⚔️ " + nombre.toUpperCase() + "  (" + coste + " EN)");
                    btn.setOnAction(e -> {
                        handleVolverMenuHabilidades();
                        ejecutarTurno(AccionHeroe.HABILIDAD);
                    });
                } else {
                    btn.setText("⚔️ " + nombre.toUpperCase() + "  (sin EN)");
                    btn.setDisable(true);
                }
            } else {
                // Postura de Hierro: usable solo una vez por combate y con EN suficiente
                int coste = Guerrero.COSTE_POSTURA_HIERRO;
                boolean yaActiva = guerrero.isPosturaDeHierroActiva();
                boolean sinEnergia = energiaActual < coste;
                if (yaActiva) {
                    btn.setText("✅ " + nombre.toUpperCase() + "  (activa)");
                    btn.setDisable(true);
                } else if (sinEnergia) {
                    btn.setText("🛡️ " + nombre.toUpperCase() + "  (sin EN)");
                    btn.setDisable(true);
                } else {
                    btn.setText("🛡️ " + nombre.toUpperCase() + "  (" + coste + " EN)");
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
        desactivarBotonesCombate();
        Magico magico = (Magico) sesion.getHeroe();

        // 1. Verificar y gastar PM
        int coste = magico.getCostePmHabilidad(nombre);
        if (!magico.gastarPm(coste)) {
            agregarLog("⚠ PM insuficientes para usar " + nombre
                    + "  [PM: " + magico.getPm() + "/" + magico.getPmMax() + "]");
            activarBotonesCombate();
            return;
        }

        // 2. Fase 1: aplicar el efecto de la habilidad
        agregarLog(motor.iniciarTurno());
        Personaje objetivo = motor.getEnemigo();
        String efecto = magico.ejecutarHabilidadAdicional(nombre, objetivo);
        if (efecto == null) {
            magico.restaurarPmParcial(coste);
            agregarLog("  ⚠ " + nombre + " no tuvo efecto.");
            agregarLog("");
            actualizarTodasLasBarras();
            activarBotonesCombate();
            return;
        }
        agregarLog("▸ " + efecto);
        actualizarBarraPm(); // reflejar el gasto de PM de inmediato

        // 3. Fase 2: contraataque del enemigo tras pausa
        PauseTransition pausa = new PauseTransition(Duration.millis(750));
        pausa.setOnFinished(ev -> {
            motor.ejecutarReaccionEnemigo().forEach(this::agregarLog);
            agregarLog("");
            actualizarTodasLasBarras();
            PauseTransition anim = new PauseTransition(Duration.millis(150));
            anim.setOnFinished(e -> animarGolpe(imgHeroe));
            anim.play();
            cerrarTurno();
        });
        pausa.play();
    }

    // ── Pantalla de carga entre fases ────────────────────────────────────────

    /**
     * Muestra el overlay de carga entre fases durante 2,5 segundos.
     *
     * <p>Configura el texto, el color y las partículas según si la fase siguiente
     * es normal (dorado) o el jefe final (rojo). La barra de progreso se anima
     * con un {@link Timeline}; al terminar, oculta el overlay y llama a
     * {@link #prepararCombate()} para arrancar el siguiente combate.</p>
     *
     * <p>Se llama desde {@link #handleContinuar()} después de haber avanzado
     * la fase y guardado el checkpoint, con la música ya detenida.</p>
     */
    private void mostrarPantallaCarga() {
        int     fase    = sesion.getFaseActual();
        boolean esFinal = (fase == 4);

        // ── Texto e iconografía ───────────────────────────────────────────────
        lblCargaIcono.setText(esFinal ? "🐉" : "⚔");
        lblCargaFase.setText("FASE " + fase);
        lblCargaSubtitulo.setText(esFinal ? "— JEFE FINAL —" : "— MAZMORRA —");
        lblCargaConsejo.setText(consejoFase(fase));

        // ── Color temático: dorado para fases normales, rojo para el jefe ────
        String color  = esFinal ? "#e05555"                : "#c8a84b";
        String shadow = esFinal ? "rgba(224,85,85,0.7)"   : "rgba(200,168,75,0.7)";

        lblCargaFase.setStyle(
            "-fx-font-family: Georgia; -fx-font-size: 52px; -fx-font-weight: bold;"
            + " -fx-text-fill: " + color + ";"
            + " -fx-effect: dropshadow(gaussian, " + shadow + ", 25, 0, 0, 0);");
        lblCargaSubtitulo.setStyle(
            "-fx-font-family: Georgia; -fx-font-size: 17px; -fx-font-style: italic;"
            + " -fx-text-fill: " + color + ";");
        barCarga.setStyle("-fx-accent: " + color + ";");

        // ── Partículas y visibilidad ──────────────────────────────────────────
        generarParticulasCarga();
        barCarga.setProgress(0);
        pantallaEntrefase.setVisible(true);
        pantallaEntrefase.setManaged(true);

        // ── Timeline: animar la barra de progreso durante 2,5 s ──────────────
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,         new KeyValue(barCarga.progressProperty(), 0.0)),
            new KeyFrame(Duration.seconds(2.5), new KeyValue(barCarga.progressProperty(), 1.0))
        );
        tl.setOnFinished(ev -> {
            pantallaEntrefase.setVisible(false);
            pantallaEntrefase.setManaged(false);
            prepararCombate(); // arranca el combate de la nueva fase
        });
        tl.play();
    }

    /**
     * Devuelve la frase temática que se muestra en el overlay de carga
     * para cada fase (2, 3 y 4). Para la fase 1 no hay carga previa.
     *
     * @param fase número de fase (2–4)
     * @return cadena con el consejo/frase, o cadena vacía si la fase no tiene
     */
    private String consejoFase(int fase) {
        switch (fase) {
            case 2: return "«La mazmorra se vuelve más peligrosa. Conserva tus recursos para los momentos de mayor necesidad.»";
            case 3: return "«Los monstruos de las profundidades no conocen la piedad. Mantén la guardia alta.»";
            case 4: return "«El Dragón te aguarda en lo más profundo de la mazmorra. Esta es tu última oportunidad de demostrar tu valía.»";
            default: return "";
        }
    }

    /**
     * Genera 60 partículas doradas animadas dentro del panel de carga.
     * Usa la misma lógica de parpadeo que los menús principales.
     * Se llama cada vez que se muestra el overlay para renovar las partículas.
     */
    private void generarParticulasCarga() {
        panelParticulasCarga.getChildren().clear();
        Random rnd = new Random(42);
        for (int i = 0; i < 60; i++) {
            double x = rnd.nextDouble() * 900, y = rnd.nextDouble() * 650;
            double r = 0.5 + rnd.nextDouble() * 1.2, o = 0.2 + rnd.nextDouble() * 0.5;
            Circle c = new Circle(x, y, r, Color.web("#c8a84b", o));
            FadeTransition ft = new FadeTransition(Duration.seconds(2 + rnd.nextDouble() * 3), c);
            ft.setFromValue(o * 0.3); ft.setToValue(o);
            ft.setAutoReverse(true); ft.setCycleCount(Animation.INDEFINITE);
            ft.setDelay(Duration.seconds(rnd.nextDouble() * 4)); ft.play();
            panelParticulasCarga.getChildren().add(c);
        }
    }

    // ── Guardar partida ───────────────────────────────────────────────────────

    /**
     * Devuelve los PM actuales del héroe si es {@link Magico}, o 0 si no usa magia.
     * Centraliza la comprobación para no duplicarla en cada método de guardado.
     */
    private int pmActualHeroe() {
        Heroe h = sesion.getHeroe();
        return (h instanceof Magico) ? ((Magico) h).getPm() : 0;
    }

    /**
     * Garantiza que existe una fila en {@code partidas} para esta sesión.
     * Si ya existe, no hace nada. Si no, realiza un INSERT con el estado actual
     * del combate (fase, HP/PM del héroe, tipo y HP/PM del enemigo activo).
     *
     * <p>Solo se llama desde {@link #procesarFinCombate} para que
     * {@link dao.CombateDAO} pueda usar el id de partida como clave foránea.
     * El guardado de progreso real (checkpoint) se realiza en
     * {@link #guardarAlAvanzarFase()}.</p>
     */
    private void asegurarPartidaCreada() {
        if (sesion.getPartida() != null) return;
        try {
            Heroe heroe = sesion.getHeroe();
            Partida p = new Partida(
                sesion.getJugador().getId(),
                heroe.getId(),
                sesion.getFaseActual(),
                heroe.getPuntosGolpe()
            );
            p.setPmActual(pmActualHeroe());
            p.setTipoEnemigo(motor.getEnemigo().getTipo());
            p.setHpEnemigo(motor.getEnemigo().getPuntosGolpe());
            p.setPmEnemigo(motor.getEnemigo().getPm());
            PartidaDAO.insertar(p);
            sesion.setPartida(p);
            PersonajeDAO.actualizarHp(heroe.getId(), heroe.getPuntosGolpe());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checkpoint de progreso: guarda la nueva fase y las estadísticas actuales
     * del héroe justo antes de empezar el combate de esa fase.
     *
     * <p>Los campos de enemigo se limpian (null / 0) porque en la nueva fase
     * el enemigo aún no ha sido generado; al cargar la partida se generará
     * uno aleatorio fresco.</p>
     *
     * <p>Se llama únicamente desde {@link #handleContinuar()} tras
     * {@link GameSession#avanzarFase()}, nunca al huir.</p>
     */
    private void guardarAlAvanzarFase() {
        try {
            Heroe heroe = sesion.getHeroe();
            if (sesion.getPartida() == null) {
                // No hubo combate anterior registrado: crear la fila ahora
                Partida p = new Partida(
                    sesion.getJugador().getId(),
                    heroe.getId(),
                    sesion.getFaseActual(),
                    heroe.getPuntosGolpe()
                );
                p.setPmActual(pmActualHeroe());
                // Enemigo aún no generado → campos vacíos (generarán uno fresco al cargar)
                PartidaDAO.insertar(p);
                sesion.setPartida(p);
            } else {
                Partida p = sesion.getPartida();
                p.setFaseActual(sesion.getFaseActual());
                p.setHpActual(heroe.getPuntosGolpe());
                p.setPmActual(pmActualHeroe());
                // Limpiar datos del enemigo anterior: la nueva fase empieza con enemigo fresco
                p.setTipoEnemigo(null);
                p.setHpEnemigo(0);
                p.setPmEnemigo(0);
                PartidaDAO.actualizar(p);
            }
            PersonajeDAO.actualizarHp(heroe.getId(), heroe.getPuntosGolpe());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Guarda la partida en BD tras una huida exitosa.
     *
     * <p>Se llama desde {@link #mostrarPantallaDescanso()} una vez aplicada la
     * recuperación de HP (y PM), de modo que la partida guardada ya refleja los
     * valores restaurados. El estado permanece {@code EN_CURSO} para que el jugador
     * pueda reanudarla desde el menú de carga. El enemigo se limpia (el héroe huyó)
     * para que al reanudar se genere uno nuevo.</p>
     */
    private void guardarAlHuir() {
        try {
            Heroe heroe = sesion.getHeroe();
            if (sesion.getPartida() == null) {
                // La partida todavía no existe en BD (el jugador huyó en fase 1 sin
                // haber ganado ningún combate previo): crear la fila ahora.
                Partida p = new Partida(
                    sesion.getJugador().getId(),
                    heroe.getId(),
                    sesion.getFaseActual(),
                    heroe.getPuntosGolpe()
                );
                p.setPmActual(pmActualHeroe());
                // Estado EN_CURSO por defecto; sin enemigo activo
                PartidaDAO.insertar(p);
                sesion.setPartida(p);
            } else {
                Partida p = sesion.getPartida();
                p.setFaseActual(sesion.getFaseActual());
                p.setHpActual(heroe.getPuntosGolpe());
                p.setPmActual(pmActualHeroe());
                p.setEstado(modelo.Partida.Estado.EN_CURSO);
                // Limpiar datos del enemigo: el héroe escapó, la fase empieza de nuevo
                p.setTipoEnemigo(null);
                p.setHpEnemigo(0);
                p.setPmEnemigo(0);
                PartidaDAO.actualizar(p);
            }
            PersonajeDAO.actualizarHp(heroe.getId(), heroe.getPuntosGolpe());
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
     * Muestra el overlay de descanso tras una huida exitosa.
     *
     * <p>El héroe recupera {@link #RECUPERACION_HUIDA_PCT}% de su HP máximo (y de su
     * PM máximo si es un personaje {@link Magico}). Las barras se animan de 0 hasta
     * el porcentaje final en 1.5 s y la barra de progreso general llega a 1 en 3.5 s,
     * tras lo cual se navega automáticamente al menú principal.</p>
     */
    private void mostrarPantallaDescanso() {
        Heroe heroe = sesion.getHeroe();

        // ── Calcular recuperación ─────────────────────────────────────────────
        int hpRecuperado = Math.max(1, heroe.getPuntosGolpeMax() * RECUPERACION_HUIDA_PCT / 100);
        heroe.curar(hpRecuperado);
        double ratioHpFinal = heroe.getPorcentajeVida();

        int pmRecuperado = 0;
        double ratioPmFinal = 0.0;
        boolean esMagico = heroe instanceof Magico;
        if (esMagico) {
            Magico magico = (Magico) heroe;
            pmRecuperado = Math.max(1, magico.getPmMax() * RECUPERACION_HUIDA_PCT / 100);
            magico.setPm(magico.getPm() + pmRecuperado);   // setPm clampea al máximo automáticamente
            ratioPmFinal = magico.getPmMax() > 0
                    ? (double) magico.getPm() / magico.getPmMax()
                    : 0.0;
        }

        // ── Guardar partida con los valores ya recuperados ────────────────────
        guardarAlHuir();

        // ── Configurar labels ─────────────────────────────────────────────────
        lblDescansoHp.setText("+" + hpRecuperado + " HP  →  "
                + heroe.getPuntosGolpe() + " / " + heroe.getPuntosGolpeMax() + " HP");

        if (esMagico) {
            Magico magico = (Magico) heroe;
            lblDescansoPm.setText("+" + pmRecuperado + " PM  →  "
                    + magico.getPm() + " / " + magico.getPmMax() + " PM");
            filaDescansopm.setVisible(true);
            filaDescansopm.setManaged(true);
        }

        // ── Mostrar overlay ───────────────────────────────────────────────────
        overlayDescanso.setMouseTransparent(false);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), overlayDescanso);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // ── Animaciones de barras (0 → ratio final en 1.5 s) ─────────────────
        final double hpTarget = ratioHpFinal;
        final double pmTarget = ratioPmFinal;

        Timeline barrasAnim = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(barraDescansoHp.progressProperty(), 0),
                new KeyValue(barraDescansoPm.progressProperty(), 0)
            ),
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(barraDescansoHp.progressProperty(), hpTarget),
                new KeyValue(barraDescansoPm.progressProperty(), pmTarget)
            )
        );
        barrasAnim.play();

        // ── Barra de progreso general (0 → 1 en 3.5 s) → luego navegar ───────
        Timeline progreso = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(barCargaDescanso.progressProperty(), 0)
            ),
            new KeyFrame(Duration.seconds(3.5),
                new KeyValue(barCargaDescanso.progressProperty(), 1)
            )
        );
        progreso.setOnFinished(e -> navegarAMenu());
        progreso.play();
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
     * Refresca la barra de Energía del Guerrero y el label numérico.
     *
     * <p>Escala de color:</p>
     * <ul>
     *   <li>{@code >50 %} — naranja brillante {@code #e09030}</li>
     *   <li>{@code 25–50 %} — naranja oscuro {@code #b06010}</li>
     *   <li>{@code ≤25 %} — rojo-naranja {@code #c03000} (energía crítica)</li>
     * </ul>
     *
     * <p>Solo debe llamarse cuando el héroe es instancia de {@link Guerrero}.</p>
     */
    private void actualizarBarraEnergia() {
        if (!(sesion.getHeroe() instanceof Guerrero)) return;
        Guerrero g = (Guerrero) sesion.getHeroe();
        barraEnergia.setProgress(g.getPorcentajeEnergia());
        lblEnergiaHeroe.setText(g.getEnergia() + " / " + g.getEnergiaMax() + " EN");
    }

    /**
     * Regenera la energía del Guerrero al final de cada turno y refresca la barra.
     * Se llama desde las lambdas de Fase 2 en {@link #ejecutarTurno} si el héroe es Guerrero.
     */
    private void regenerarEnergiaGuerrero() {
        if (!(sesion.getHeroe() instanceof Guerrero)) return;
        ((Guerrero) sesion.getHeroe()).regenerarEnergia();
        actualizarBarraEnergia();
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
     * Carga el clip {@code cursor.wav} en memoria para reproducirlo con
     * latencia mínima al pasar el cursor sobre los botones de combate.
     * Si el archivo no existe, el sonido queda desactivado sin error fatal.
     */
    private void inicializarSonidoHover() {
        try {
            URL url = getClass().getResource("/recursos/audio/cursor.wav");
            if (url != null) sonidoHover = new AudioClip(url.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registra el sonido de hover en un botón dado.
     * El clip se reproduce en {@code MOUSE_ENTERED}, igual que en el menú principal.
     *
     * @param btn botón al que añadir el efecto (ignorado si es {@code null})
     */
    private void agregarSonidoHover(Button btn) {
        if (btn == null) return;
        btn.setOnMouseEntered(e -> { if (sonidoHover != null) sonidoHover.play(); });
    }

    /**
     * Registra el sonido de hover en todos los botones estáticos de la pantalla
     * de combate (los que tienen {@code fx:id} en el FXML).
     * Se llama una sola vez desde {@link #iniciarSesion(GameSession)}.
     * Los botones dinámicos de los submenús reciben el sonido en sus propios
     * métodos de construcción ({@link #construirSubmenuMagia} y
     * {@link #construirSubmenuHabilidades}).
     */
    private void configurarSonidoBotones() {
        agregarSonidoHover(btnAtacar);
        agregarSonidoHover(btnObjetos);
        agregarSonidoHover(btnHabilidad);
        agregarSonidoHover(btnMagia);
        agregarSonidoHover(btnHabilidades);
        agregarSonidoHover(btnHuir);
        agregarSonidoHover(btnContinuar);
        agregarSonidoHover(btnPocionCuracion);
        agregarSonidoHover(btnPocionMagica);
        agregarSonidoHover(btnConfirmarHuida);
        agregarSonidoHover(btnCancelarHuida);
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
            mediaPlayer.dispose(); // liberar hilos nativos de audio
            mediaPlayer = null;
        }
    }
}
