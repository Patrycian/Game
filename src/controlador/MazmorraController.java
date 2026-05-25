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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

public class MazmorraController implements Initializable {

	@FXML
	private Label lblFase;

	/** Nombre y tipo del héroe. Formato: "Gandalf (MAGO)". */
	@FXML
	private Label lblNombreHeroe;

	/** HP actual / HP máximo del héroe. Se actualiza tras cada turno. */
	@FXML
	private Label lblHpHeroe;

	/** Barra de vida del héroe. Verde > 50 %, naranja > 25 %, rojo ≤ 25 %. */
	@FXML
	private ProgressBar barraVidaHeroe;

	/** Nombre y tipo del enemigo actual. */
	@FXML
	private Label lblNombreEnemigo;

	/**
	 * Barra de vida del enemigo con la misma escala de colores que la del héroe.
	 */
	@FXML
	private ProgressBar barraVidaEnemigo;

	/** HP actual / HP máximo del enemigo. */
	@FXML
	private Label lblHpEnemigo;

	/** Imagen del sprite del héroe. */
	@FXML
	private ImageView imgHeroe;

	/** Imagen del sprite del enemigo. */
	@FXML
	private ImageView imgEnemigo;

	/**
	 * Prompt de la caja de diálogo.
	 */
	@FXML
	private Label lblPrompt;

	/** Área de texto donde se acumula el log de combate */
	@FXML
	private TextArea txtLog;

	/**
	 * Label de resultado al terminar el combate.
	 */
	@FXML
	private Label lblResultado;

	/**
	 * Menú principal de batalla
	 */
	@FXML
	private VBox menuBatalla;

	/** Botón de ataque básico. */
	@FXML
	private Button btnAtacar;

	/**
	 * Botón de objetos.
	 */
	@FXML
	private Button btnObjetos;

	/**
	 * Botón de habilidad (cambiar)
	 */
	@FXML
	private Button btnHabilidad;

	/**
	 * Botón de habilidades mágicas
	 */
	@FXML
	private Button btnMagia;

	/**
	 * Botón de habilidad
	 */
	@FXML
	private Button btnHabilidades;

	/** Botón de huida; abre un diálogo de confirmación antes de guardar y salir. */
	@FXML
	private Button btnHuir;

	/**
	 * Botón que aparece al terminar el combate.
	 */
	@FXML
	private Button btnContinuar;

	/**
	 * Panel del submenú de magia
	 */
	@FXML
	private VBox menuMagia;

	/**
	 * Contenedor donde se generan los botones de habilidades mágicas.
	 */
	@FXML
	private VBox contenedorHabilidades;

	/**
	 * Panel del submenú de habilidades del Guerrero.
	 */
	@FXML
	private VBox menuHabilidades;

	/**
	 * Contenedor donde se generan dinámicamente los botones de habilidades del
	 * Guerrero.
	 */
	@FXML
	private VBox contenedorHabilidadesGuerrero;

	/** Panel del submenú de objetos (pociones). */
	@FXML
	private VBox menuObjetos;

	/**
	 * Botón de poción de curación en el submenú de objetos.
	 */
	@FXML
	private Button btnPocionCuracion;

	/**
	 * Botón de poción mágica en el submenú de objetos.
	 */
	@FXML
	private Button btnPocionMagica;

	/**
	 * Fila del panel de héroe que contiene la etiqueta "PM" y la barra de PM.
	 */
	@FXML
	private HBox filaPm;

	/**
	 * Fila con el texto numérico de PM del héroe (ejemplo: "20 / 30 PM").
	 */
	@FXML
	private HBox filaNumPm;

	/** Barra de progreso que representa los PM actuales del héroe mágico. */
	@FXML
	private ProgressBar barraPoderMagico;

	/** Label numérico de PM del héroe. */
	@FXML
	private Label lblPmHeroe;

	/**
	 * Fila con etiqueta "EN" y barra de Energía del Guerrero.
	 */
	@FXML
	private HBox filaEnergia;

	/** Fila con el texto numérico de Energía. */
	@FXML
	private HBox filaNumEnergia;

	/**
	 * Barra de progreso que representa la Energía actual del Guerrero.
	 */
	@FXML
	private ProgressBar barraEnergia;

	/** Label numérico de Energía. */
	@FXML
	private Label lblEnergiaHeroe;

	/**
	 * Fila del panel de enemigo con la etiqueta "PM" y la barra de PM.
	 */
	@FXML
	private HBox filaPmEnemigo;

	/**
	 * Fila con el texto numérico de PM del enemigo.
	 */
	@FXML
	private HBox filaNumPmEnemigo;

	/** Barra de progreso que representa los PM actuales del enemigo mágico. */
	@FXML
	private ProgressBar barraPmEnemigo;

	/** Label numérico de PM del enemigo. */
	@FXML
	private Label lblPmEnemigo;

	/**
	 * StackPane semiopaco que cubre el combate mientras el jugador decide si huir.
	 * Se muestra al pulsar HUIR y desaparece al confirmar o cancelar.
	 */
	@FXML
	private StackPane overlayHuida;

	/** Línea base "Probabilidad base: 40%" del desglose. */
	@FXML
	private Label lblHuidaBase;

	/** Modificador de HP del héroe. */
	@FXML
	private Label lblHuidaVida;

	/** Modificador de clase del héroe. */
	@FXML
	private Label lblHuidaClase;

	/** Modificador según el tipo de enemigo. */
	@FXML
	private Label lblHuidaEnemigo;

	/** Label de probabilidad destacado en el panel. */
	@FXML
	private Label lblHuidaProbFinal;

	/** Botón del overlay de huida. */
	@FXML
	private Button btnConfirmarHuida;

	/** Botón del overlay de huida. */
	@FXML
	private Button btnCancelarHuida;

	/**
	 * StackPane semiopaco que cubre la pantalla mientras el héroe descansa tras una
	 * huida exitosa. Muestra la animación de recuperación de HP (y PM) antes de
	 * navegar al menú principal.
	 */
	@FXML
	private StackPane overlayDescanso;

	/**
	 * Barra de progreso animada que refleja la vida recuperada.
	 */
	@FXML
	private ProgressBar barraDescansoHp;

	/** Label que muestra la vida recuperada. */
	@FXML
	private Label lblDescansoHp;

	/**
	 * Fila entera del panel de maná durante el descanso.
	 */
	@FXML
	private VBox filaDescansopm;

	/** Barra de progreso animada del maná recuperado durante el descanso. */
	@FXML
	private ProgressBar barraDescansoPm;

	/** Label que muestra el maná recuperado */
	@FXML
	private Label lblDescansoPm;

	/**
	 * Barra de progreso general del overlay de descanso.
	 */
	@FXML
	private ProgressBar barCargaDescanso;

	// PANTALLA DE CARGA
	/**
	 * StackPane opaco que cubre toda la ventana entre fases. Se muestra al pulsar
	 * "Siguiente Fase" y desaparece automáticamente cuando la barra de progreso
	 * llega al 100 %.
	 */
	@FXML
	private StackPane pantallaEntrefase;

	/** Panel de partículas doradas animadas dentro del overlay de carga. */
	@FXML
	private Pane panelParticulasCarga;

	/**
	 * Emoji grande que identifica la fase.
	 */
	@FXML
	private Label lblCargaIcono;

	/** Label "FASE N" en el overlay de carga. */
	@FXML
	private Label lblCargaFase;

	/** Label que indica la fase. */
	@FXML
	private Label lblCargaSubtitulo;

	/** Frase temática/consejo de la fase mostrada durante la carga. */
	@FXML
	private Label lblCargaConsejo;

	/** Barra de progreso animada que mide la duración del overlay de carga. */
	@FXML
	private ProgressBar barCarga;

	/**
	 * Sesión de juego activa; contiene jugador, héroe, fase y referencia a la
	 * partida en BD.
	 */
	private GameSession sesion;

	/**
	 * Probabilidad de huida calculada en handleHuir() y usada luego en
	 * handleConfirmarHuida() cuando el jugador intenta huir.
	 */
	private int probHuidaActual = 0;

	/** Motor de combate por turnos; gestiona ataques, contraataques y resultado. */
	private MotorCombate motor;

	/**
	 * Indica si el combate actual ha terminado (victoria o derrota).
	 */
	private boolean combateTerminado = false;

	/**
	 * Reproductor de música de fondo
	 */
	private MediaPlayer mediaPlayer;

	/**
	 * Clip de sonido corto
	 */
	private AudioClip sonidoHover;

	/** Clip de sonido que suena al ejecutar un ataque normal. */
	private AudioClip sonidoAtaque;

	/** Map de clips de audio de habilidades */
	private final Map<String, AudioClip> cacheSonidosHabilidad = new HashMap<>();

	// INVENTARIO

	/**
	 * Pociones de curación disponibles.
	 */
	private int pocionesRestantes;

	/** Cantidad de HP que restaura cada poción de curación. */
	private static final int CURACION_POCION = 30;

	/**
	 * Pociones mágicas disponibles.
	 */
	private int pocionesMagicasRestantes;

	/** Cantidad de PM que restaura cada poción mágica. */
	private static final int RESTAURACION_PM_POCION = 10;

	/**
	 * Porcentaje de vida (y maná, si aplica) que se recupera al descansar tras una
	 * huida exitosa antes de volver al menú principal.
	 */
	private static final int RECUPERACION_HUIDA_PCT = 25;

	// INITIALIZE

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		/* configuración en iniciarSesion */ }

	/**
	 * Punto de entrada principal del controlador. Recibe la sesión del controlador
	 * anterior ({@link SeleccionHeroeController} o
	 * {@link CargarPartidaController}), inicializa el inventario y arranca el
	 * combate de la fase actual.
	 */
	public void iniciarSesion(GameSession sesion) {
		this.sesion = sesion;
		// El inventario se inicializa aquí, una sola vez para toda la partida
		pocionesRestantes = 3;
		pocionesMagicasRestantes = 2;
		inicializarSonidoHover();
		inicializarSonidoAtaque();
		configurarSonidoBotones();
		// Mostrar pantalla de carga antes de la fase actual (1 para partida nueva,
		// o la fase guardada para partida cargada). Al terminar, llamará a
		// prepararCombate() automáticamente.
		mostrarPantallaCarga();
	}

	// ── Preparación ───────────────────────────────────────────────────────────

	/**
	 * Configura toda la interfaz para el combate de la fase actual. Se llama al
	 * inicio de cada fase (incluyendo la primera).
	 */
	private void prepararCombate() {
		int fase = sesion.getFaseActual();
		Heroe heroe = sesion.getHeroe();

		// Si hay una partida guardada con un enemigo activo (HP > 0), restaurarlo;
		// en caso contrario (nueva fase o enemigo derrotado) generarlo aleatoriamente.
		Partida partidaActual = sesion.getPartida();
		Enemigo enemigo;
		if (partidaActual != null && partidaActual.getTipoEnemigo() != null && partidaActual.getHpEnemigo() > 0) {
			enemigo = MotorCombate.generarEnemigoDeTipo(partidaActual.getTipoEnemigo());
			enemigo.setPuntosGolpe(partidaActual.getHpEnemigo());
			enemigo.setPm(partidaActual.getPmEnemigo()); // 0 si no usa magia, correcto igualmente
		} else {
			enemigo = MotorCombate.generarEnemigo(fase);
		}

		heroe.reiniciarHabilidad(); // la habilidad especial se recarga entre fases
		motor = new MotorCombate(heroe, enemigo);
		combateTerminado = false;

		// Labels de fase
		lblFase.setText("⚔  FASE " + fase + (fase == 4 ? "  —  JEFE FINAL" : "  —  MAZMORRA"));

		// Héroe: nombre, imagen y barra de vida
		lblNombreHeroe.setText(heroe.getNombre() + " (" + heroe.getTipo() + ")");
		try {
			Image imgSrc = new Image(getClass().getResourceAsStream(heroe.getRutaImagen()));
			imgHeroe.setImage(imgSrc);
		} catch (Exception e) {
			// Si la imagen no carga, el juego continúa sin el sprite (no es un error fatal)
			e.printStackTrace();
		}
		actualizarBarraHeroe();

		// Enemigo: nombre, imagen y barra de vida
		lblNombreEnemigo.setText(enemigo.getNombre() + " (" + enemigo.getTipo() + ")");
		try {
			Image imgEnemSrc = new Image(getClass().getResourceAsStream(enemigo.getRutaImagen()));
			imgEnemigo.setImage(imgEnemSrc);
		} catch (Exception e) {
			// Si la imagen no carga, el juego continúa sin el sprite (no es un error fatal)
			e.printStackTrace();
		}
		actualizarBarraEnemigo();

		// Barra de PM del enemigo (solo para Saga y Dragón, que tienen pmMax > 0)
		boolean enemigoTienePm = enemigo.tienePmMax();
		filaPmEnemigo.setVisible(enemigoTienePm);
		filaPmEnemigo.setManaged(enemigoTienePm);
		filaNumPmEnemigo.setVisible(enemigoTienePm);
		filaNumPmEnemigo.setManaged(enemigoTienePm);
		if (enemigoTienePm) {
			actualizarBarraPmEnemigo();
		}

		// Visibilidad de los botones de habilidad según la clase del héroe
		boolean esMagico = heroe instanceof Magico;
		boolean esGuerrero = heroe instanceof Guerrero;

		// btnHabilidad: solo para clases sin submenú propio
		// (actualmente ninguna clase llega a este caso, APUNTE: CAMBIAR)

		btnHabilidad.setVisible(!esMagico && !esGuerrero);
		btnHabilidad.setManaged(!esMagico && !esGuerrero);

		// btnMagia: submenú de hechizos para Mago y Clérigo
		btnMagia.setVisible(esMagico);
		btnMagia.setManaged(esMagico);

		// btnHabilidades: submenú de habilidades físicas para el Guerrero
		btnHabilidades.setVisible(esGuerrero);
		btnHabilidades.setManaged(esGuerrero);

		// Construcción del submenú correspondiente
		if (esMagico) {
			btnMagia.setDisable(false);
			construirBotonesHabilidades(heroe, contenedorHabilidades);
		} else if (esGuerrero) {
			btnHabilidades.setDisable(false);
			construirBotonesHabilidades(heroe, contenedorHabilidadesGuerrero);
		}

		// Aseguramos que todos los submenús empiecen ocultos
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

		// Barra de PM del héroe (solo para personajes Mágicos)
		filaPm.setVisible(esMagico);
		filaPm.setManaged(esMagico);
		filaNumPm.setVisible(esMagico);
		filaNumPm.setManaged(esMagico);
		if (esMagico) {
			actualizarBarraPm();
		}

		// Barra de Energía (solo para el Guerrero)
		filaEnergia.setVisible(esGuerrero);
		filaEnergia.setManaged(esGuerrero);
		filaNumEnergia.setVisible(esGuerrero);
		filaNumEnergia.setManaged(esGuerrero);
		if (esGuerrero) {
			actualizarBarraEnergia();
		}

		// Log de inicio del combate
		txtLog.clear();
		agregarLog("¡Un " + enemigo.getNombre() + " salvaje apareció!");
		agregarLog("");
		for (Habilidad h : heroe.getHabilidades()) {
			agregarLog("✨ " + h.getNombre() + ": " + h.getDescripcion());
		}
		agregarLog("");

		if (lblPrompt != null) {
			lblPrompt.setText("¿Qué acción realizará " + heroe.getNombre() + "?");
			lblPrompt.setVisible(true);
		}

		// Estado inicial de los botones de resultado
		lblResultado.setVisible(false);
		if (btnContinuar != null) {
			btnContinuar.setVisible(false);
			btnContinuar.setManaged(false);
			// Restaurar el handler por defecto (puede haberse sobreescrito tras una
			// derrota)
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

		// Música
		iniciarMusica(sesion.getFaseActual() == 4);
	}

	// ── Handlers ──────────────────────────────────────────────────────────────

	/**
	 * Ejecuta un turno con la acción atacar.
	 */
	@FXML
	private void handleAtacar() {
		ejecutarTurno(AccionHeroe.ATAQUE);
	}

	/**
	 * Maneja la pulsación del botón "HABILIDAD" (CAMBIAR).
	 */
	@FXML
	private void handleHabilidad() {
	}

	/**
	 * Maneja la pulsación del botón "OBJETOS". Abre el submenú de objetos si el
	 * inventario tiene al menos un objeto disponible. Si el inventario está vacío
	 * muestra un mensaje en el log y no abre el submenú.
	 */
	@FXML
	private void handleObjetos() {
		if (combateTerminado) {
			return;
		}
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
	 * Maneja el botón "HUIR": calcula la probabilidad de huida y muestra el overlay
	 * in-game con el desglose de modificadores.
	 */
	@FXML
	private void handleHuir() {
		if (combateTerminado) {
			return;
		}

		Heroe heroe = sesion.getHeroe();
		Enemigo enemigo = motor.getEnemigo();

		// Modificador por HP

		int pctVida = (int) ((heroe.getPuntosGolpe() * 100.0) / heroe.getPuntosGolpeMax());
		int modVida;
		String msgVida;
		if (pctVida < 25) {
			modVida = 20;
			msgVida = "¡Estás malherido! Mayor probabilidad de huir.  (+20%)";
		} else if (pctVida < 50) {
			modVida = 10;
			msgVida = "Estás herido, pero aún puedes luchar.  (+10%)";
		} else {
			modVida = 0;
			msgVida = "Aún puedes seguir luchando...  (±0%)";
		}

		// Modificador por clase

		int modClase;
		String msgClase;
		if (heroe instanceof Guerrero) {
			modClase = -10;
			msgClase = "Los guerreros no huyen fácilmente.  (−10%)";
		} else if (heroe instanceof Mago) {
			modClase = 5;
			msgClase = "Los magos son escurridizos.  (+5%)";
		} else {
			modClase = 0;
			msgClase = null; // Clérigo sin modificador
		}

		// Modificador por tipo de enemigo

		int modEnemigo;
		String msgEnemigo;
		switch (enemigo.getTipo().toUpperCase()) {
		case "GOBLIN":
			modEnemigo = 10;
			msgEnemigo = "Los goblins son fáciles de esquivar.  (+10%)";
			break;
		case "OGRO":
			modEnemigo = -5;
			msgEnemigo = "Los ogros son implacables.  (−5%)";
			break;
		case "SAGA":
			modEnemigo = -10;
			msgEnemigo = "La Saga controla el campo de batalla.  (−10%)";
			break;
		case "DRAGON":
			modEnemigo = -20;
			msgEnemigo = "¡Nadie escapa de un Dragón fácilmente!  (−20%)";
			break;
		default:
			modEnemigo = 0;
			msgEnemigo = null;
		}

		probHuidaActual = Math.max(5, Math.min(90, 40 + modVida + modClase + modEnemigo));

		// Rellenamos labels del overlay
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

		// Mostrar overlay
		overlayHuida.setVisible(true);
		overlayHuida.setManaged(true);
	}

	/**
	 * Ejecuta el intento de huida cuando el jugador pulsa "Huir".
	 */
	@FXML
	private void handleConfirmarHuida() {
		overlayHuida.setVisible(false);
		overlayHuida.setManaged(false);

		Heroe heroe = sesion.getHeroe();
		Enemigo enemigo = motor.getEnemigo();
		int tirada = new Random().nextInt(100) + 1;

		if (tirada <= probHuidaActual) {
			// Éxito
			agregarLog("🏃 " + heroe.getNombre() + " intenta huir..." + "  (tirada: " + tirada + " ≤ " + probHuidaActual
					+ ")");
			agregarLog("✅ ¡Huida exitosa! Escapas del combate.");
			agregarLog("");

			btnAtacar.setDisable(true);
			btnHabilidad.setDisable(true);
			btnMagia.setDisable(true);
			btnHabilidades.setDisable(true);
			btnObjetos.setDisable(true);
			btnHuir.setDisable(true);

			// Breve pausa antes de mostrar la pantalla de descanso
			pausarYEjecutar(Duration.millis(600), this::mostrarPantallaDescanso);

		} else {
			// Fracaso: penalización (golpe por la espalda, sin reducción por defensa) ──
			agregarLog("🏃 " + heroe.getNombre() + " intenta huir..." + "  (tirada: " + tirada + " > " + probHuidaActual
					+ ")");
			agregarLog("❌ ¡No has podido huir! El enemigo te alcanza.");

			int danioPenalizacion = Math.max(1, enemigo.getPoder() / 2);
			heroe.recibirDanio(danioPenalizacion);
			agregarLog(
					"💥 " + enemigo.getNombre() + " te golpea por la espalda por " + danioPenalizacion + " de daño!");
			agregarLog("   " + heroe.getNombre() + ": " + heroe.getPuntosGolpe() + " / " + heroe.getPuntosGolpeMax()
					+ " HP");
			agregarLog("");

			pausarYEjecutar(Duration.millis(200), () -> animarGolpe(imgHeroe));

			actualizarBarraHeroe();
			if (heroe instanceof Magico) {
				actualizarBarraPm();
			}

			if (!heroe.estaVivo()) {
				combateTerminado = true;
				procesarFinCombate(ResultadoCombate.DERROTA);
			}
		}
	}

	/**
	 * Cancela el intento de huida. El combate continúa sin consumir el turno del
	 * héroe.
	 */
	@FXML
	private void handleCancelarHuida() {
		overlayHuida.setVisible(false);
		overlayHuida.setManaged(false);
	}

	/**
	 * Maneja la pulsación del botón "Continuar" al terminar un combate. Si hay más
	 * fases disponibles, avanza a la siguiente con prepararCombate(). Si se ha
	 * completado la última fase, navega a la pantalla de resultado con victoria.
	 */
	@FXML
	private void handleContinuar() {
		if (sesion.hayMasFases()) {
			sesion.avanzarFase();
			// Mostrar pantalla de carga; al terminar, llama a prepararCombate()
			detenerMusica();
			mostrarPantallaCarga();
		} else {
			// El jugador ha superado las 4 fases
			navegarAResultado(true);
		}
	}

	// ── Lógica de turno ───────────────────────────────────────────────────────

	/**
	 * Enumeración de las posibles acciones del héroe en cada turno mediante el menú
	 * principal.
	 */
	private enum AccionHeroe {
		ATAQUE, POCION, POCION_MAGICA
	}

	/**
	 * Deshabilita todos los botones de acción de combate mientras se procesa un
	 * turno para evitar que el jugador pueda enviar múltiples acciones simultáneas.
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
	 * Reactiva los botones de acción tras completar el turno.
	 */
	private void activarBotonesCombate() {
		btnAtacar.setDisable(false);
		btnHuir.setDisable(false);
		btnMagia.setDisable(false);
		btnHabilidades.setDisable(false);
		btnHabilidad.setDisable(false);
		// Objetos: solo si quedan pociones
		btnObjetos.setDisable(pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0);
		// Reconstruir submenús con el estado actualizado
		Heroe heroeActual = sesion.getHeroe();
		if (heroeActual instanceof Magico) {
			construirBotonesHabilidades(heroeActual, contenedorHabilidades);
		}
		if (heroeActual instanceof Guerrero) {
			construirBotonesHabilidades(heroeActual, contenedorHabilidadesGuerrero);
		}
	}

	/**
	 * Actualiza todas las barras de HP y PM de ambos combatientes. Se llama al
	 * final de cada fase de turno para reflejar el estado actual.
	 */
	private void actualizarTodasLasBarras() {
		actualizarBarraHeroe();
		actualizarBarraEnemigo();
		if (sesion.getHeroe() instanceof Magico) {
			actualizarBarraPm();
		}
		if (sesion.getHeroe() instanceof Guerrero) {
			actualizarBarraEnergia();
		}
		if (motor.getEnemigo().tienePmMax()) {
			actualizarBarraPmEnemigo();
		}
	}

	/**
	 * Comprueba el resultado del motor y, si el combate terminó, lo procesa. De lo
	 * contrario reactiva los botones para el siguiente turno.
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
	 */
	private void ejecutarTurno(AccionHeroe accion) {
		if (combateTerminado) { // evita que acciones tardías procesen turno
			return;
		}
		desactivarBotonesCombate();

		switch (accion) {
		case ATAQUE:
			ejecutarTurnoAtaqueBasico();
			break;
		case POCION:
			ejecutarTurnoPocion();
			break;
		case POCION_MAGICA:
			ejecutarTurnoPocionMagica();
			break;
		}
	}

	/**
	 * Maneja el ataque básico del héroe.
	 */
	private void ejecutarTurnoAtaqueBasico() {
		agregarLog(motor.iniciarTurno());
		if (sonidoAtaque != null) {
			sonidoAtaque.play();
		}
		motor.ejecutarAtaqueBasico().forEach(this::agregarLog);
		actualizarBarraEnemigo();
		if (motor.getEnemigo().tienePmMax()) {
			actualizarBarraPmEnemigo();
		}
		animarGolpe(imgEnemigo);

		if (motor.haTerminado()) {
			agregarLog("");
			actualizarTodasLasBarras();
			combateTerminado = true;
			procesarFinCombate(motor.getResultado());
			return;
		}
		// La energía del Guerrero se regenera solo con ataques normales
		ejecutarFase2Enemiga(/* regenerarEnergia= */ true);
	}

	/**
	 * maneja habilidad especial elegida desde el submenú.
	 */
	private void ejecutarTurnoHabilidad(Habilidad habilidad) {
		if (combateTerminado) {
			return;
		}
		desactivarBotonesCombate();

		Heroe heroe = sesion.getHeroe();

		// Verificar recursos antes de actuar
		if (!habilidad.puedeUsarse(heroe)) {
			agregarLog("⚠ No puedes usar " + habilidad.getNombre() + "  — " + motivoNoDisponible(heroe, habilidad));
			activarBotonesCombate();
			return;
		}

		// Fase 1: ejecutar la habilidad
		Personaje objetivo = habilidad.afectaAlEnemigo() ? motor.getEnemigo() : heroe;
		agregarLog(motor.iniciarTurno());
		reproducirSonidoHabilidad(habilidad);
		agregarLog("▸ " + habilidad.ejecutar(heroe, objetivo));
		actualizarTodasLasBarras();

		if (habilidad.afectaAlEnemigo()) {
			animarGolpe(imgEnemigo);
		}

		// Verifica si el combate ha terminado
		motor.verificarResultado().forEach(this::agregarLog);

		if (motor.haTerminado()) {
			agregarLog("");
			combateTerminado = true;
			procesarFinCombate(motor.getResultado());
			return;
		}

		// Las habilidades especiales no regeneran Energía del Guerrero
		ejecutarFase2Enemiga(/* regenerarEnergia= */ false);
	}

	/**
	 * Determina el motivo por el que una habilidad no puede usarse en este momento,
	 * para mostrarlo en el botón o en el log.
	 */
	private String motivoNoDisponible(Heroe heroe, Habilidad habilidad) {
		if (habilidad.getTipoRecurso() == TipoRecurso.ENERGIA && heroe instanceof Guerrero) {
			Guerrero g = (Guerrero) heroe;
			return g.getEnergia() < habilidad.getCoste() ? "(sin EN)" : "(activa)";
		}
		if (habilidad.getTipoRecurso() == TipoRecurso.MANA && heroe instanceof Magico) {
			Magico m = (Magico) heroe;
			return m.getPm() < habilidad.getCoste() ? "(PM insuf.)" : "(activa)";
		}
		return "(no disponible)";
	}

	/**
	 * Manejo de acción Poción de Curación.
	 */
	private void ejecutarTurnoPocion() {
		pocionesRestantes--;
		Heroe heroe = sesion.getHeroe();

		int hpAntes = heroe.getPuntosGolpe();
		heroe.curar(CURACION_POCION);
		int hpCurado = heroe.getPuntosGolpe() - hpAntes;

		agregarLog(motor.iniciarTurno());
		agregarLog(String.format("▸ 🧪 %s usa Poción de Curación  →  +%d HP  [%s: %d/%d HP]", heroe.getNombre(),
				hpCurado, heroe.getNombre(), heroe.getPuntosGolpe(), heroe.getPuntosGolpeMax()));
		actualizarBarraHeroe(); // refleja la curación antes de la pausa

		ejecutarFase2Enemiga(/* regenerarEnergia= */ false);
	}

	/**
	 * Manejo de acción Poción Mágica.
	 */
	private void ejecutarTurnoPocionMagica() {
		pocionesMagicasRestantes--;
		Heroe heroe = sesion.getHeroe();

		agregarLog(motor.iniciarTurno());

		if (heroe instanceof Magico) {
			Magico magico = (Magico) heroe;
			int pmAntes = magico.getPm();
			magico.restaurarPmParcial(RESTAURACION_PM_POCION);
			int pmRestaurado = magico.getPm() - pmAntes;
			agregarLog(String.format("▸ 🔮 %s usa Poción Mágica  →  +%d PM  [%d/%d PM]", heroe.getNombre(),
					pmRestaurado, magico.getPm(), magico.getPmMax()));
			actualizarBarraPm(); // refleja la recarga antes de la pausa
		} else {
			agregarLog("▸ 🔮 " + heroe.getNombre() + " usa Poción Mágica... ¡Sin PM! No hizo efecto.");
		}

		ejecutarFase2Enemiga(/* regenerarEnergia= */ false);
	}

	/**
	 * Ejecuta la Fase 2 del turno: tras una pausa de 750 ms, el enemigo reacciona,
	 * se actualizan todas las barras y se cierra el turno.
	 */
	private void ejecutarFase2Enemiga(boolean regenerarEnergia) {
		pausarYEjecutar(Duration.millis(750), () -> {
			if (sonidoAtaque != null) {
				sonidoAtaque.play();
			}
			motor.ejecutarReaccionEnemigo().forEach(this::agregarLog);
			agregarLog("");
			if (regenerarEnergia) {
				regenerarEnergiaGuerrero();
			}
			actualizarTodasLasBarras();
			pausarYEjecutar(Duration.millis(150), () -> animarGolpe(imgHeroe));
			cerrarTurno();
		});
	}

	/**
	 * Crea una PauseTransition con la duración indicada y ejecuta accionAlTerminar
	 * cuando finaliza.
	 */
	private void pausarYEjecutar(Duration duracion, Runnable accionAlTerminar) {
		PauseTransition pausa = new PauseTransition(duracion);
		pausa.setOnFinished(e -> accionAlTerminar.run());
		pausa.play();
	}

	/**
	 * Procesa el fin del combate: desactiva los botones, registra el combate en BD,
	 * actualiza la puntuación (si es victoria) y muestra el botón de continuar.
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
		if (menuMagia != null && menuMagia.isVisible()) {
			handleVolverMenu();
		}
		if (menuObjetos != null && menuObjetos.isVisible()) {
			handleVolverMenuObjetos();
		}
		if (menuHabilidades != null && menuHabilidades.isVisible()) {
			handleVolverMenuHabilidades();
		}

		boolean victoria = resultado == ResultadoCombate.VICTORIA;

		// Asegurar que la partida existe en BD para poder registrar el combate.
		asegurarPartidaCreada();
		if (sesion.getPartida() != null) {
			try {
				CombateDAO.registrar(sesion.getPartida().getId(), sesion.getFaseActual(), motor.getEnemigo().getTipo(),
						victoria, motor.getTurno());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (victoria) {
			// Victoria: +10 puntos y guardar en BD
			sesion.getJugador().sumarPuntos(10);
			try {
				JugadorDAO.actualizarPuntuacion(sesion.getJugador());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Drop: el enemigo puede soltar un objeto al caer
			procesarDrop(motor.getEnemigo());

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
				} catch (Exception e) {
					e.printStackTrace();
				}
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
	 */
	@FXML
	private void handleMagia() {
		if (combateTerminado) {
			return;
		}
		construirBotonesHabilidades(sesion.getHeroe(), contenedorHabilidades);
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
	 * Construye dinámicamente los botones de habilidades.
	 */
	private void construirBotonesHabilidades(Heroe heroe, VBox contenedor) {
		contenedor.getChildren().clear();

		for (Habilidad habilidad : heroe.getHabilidades()) {
			Button btn = new Button();
			btn.getStyleClass().add("btn-batalla-barra");
			btn.setMaxWidth(Double.MAX_VALUE);
			btn.setPrefHeight(36);
			btn.setMinHeight(32);
			VBox.setVgrow(btn, javafx.scene.layout.Priority.ALWAYS);
			agregarSonidoHover(btn);

			Tooltip tip = new Tooltip(habilidad.getDescripcion());
			tip.setWrapText(true);
			tip.setMaxWidth(210);
			btn.setTooltip(tip);

			if (habilidad.puedeUsarse(heroe)) {
				// Construir etiqueta con el coste si lo tiene
				String etiquetaCoste = "";
				if (habilidad.getCoste() > 0) {
					String unidad = habilidad.getTipoRecurso() == TipoRecurso.ENERGIA ? "EN" : "PM";
					etiquetaCoste = "  (−" + habilidad.getCoste() + " " + unidad + ")";
				}
				btn.setText("✨ " + habilidad.getNombre().toUpperCase() + etiquetaCoste);
				final Habilidad h = habilidad;
				btn.setOnAction(e -> {
					cerrarSubmenusHabilidades();
					ejecutarTurnoHabilidad(h);
				});
			} else {
				btn.setText("✨ " + habilidad.getNombre().toUpperCase() + "  " + motivoNoDisponible(heroe, habilidad));
				btn.setDisable(true);
			}

			contenedor.getChildren().add(btn);
		}
	}

	/**
	 * Cierra cualquier submenú de habilidades que esté abierto y vuelve al menú
	 * principal de batalla. Llamado desde los handlers de los botones de habilidad.
	 */
	private void cerrarSubmenusHabilidades() {
		if (menuMagia != null && menuMagia.isVisible()) {
			handleVolverMenu();
		}
		if (menuHabilidades != null && menuHabilidades.isVisible()) {
			handleVolverMenuHabilidades();
		}
	}

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
	 * Usa una poción de curación desde el submenú de objetos. Cierra el submenú y
	 * ejecuta el turno.
	 */
	@FXML
	private void handleUsarPocionCuracion() {
		handleVolverMenuObjetos();
		ejecutarTurno(AccionHeroe.POCION);
	}

	/**
	 * Usa una poción mágica desde el submenú de objetos. Cierra el submenú y
	 * ejecuta el turno.
	 */
	@FXML
	private void handleUsarPocionMagica() {
		handleVolverMenuObjetos();
		ejecutarTurno(AccionHeroe.POCION_MAGICA);
	}

	/**
	 * Abre el submenú de habilidades del Guerrero, reconstruyendo los botones según
	 * el estado actual (Energía disponible, Postura de Hierro activa o no).
	 */
	@FXML
	private void handleHabilidades() {
		if (combateTerminado) {
			return;
		}
		construirBotonesHabilidades(sesion.getHeroe(), contenedorHabilidadesGuerrero);
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
	 * Actualiza el texto y el estado (habilitado/deshabilitado) de los botones del
	 * submenú de objetos según el inventario actual.
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

	// ── Pantalla de carga entre fases ────────────────────────────────────────

	/**
	 * Muestra el overlay de carga entre fases durante 2,5 segundos.
	 */
	private void mostrarPantallaCarga() {
		int fase = sesion.getFaseActual();
		boolean esFinal = (fase == 4);

		lblCargaIcono.setText(esFinal ? "🐉" : "⚔");
		lblCargaFase.setText("FASE " + fase);
		lblCargaSubtitulo.setText(esFinal ? "— JEFE FINAL —" : "— MAZMORRA —");
		lblCargaConsejo.setText(consejoFase(fase));

		// Color: dorado para fases normales, rojo para el jefe
		String color = esFinal ? "#e05555" : "#c8a84b";
		String shadow = esFinal ? "rgba(224,85,85,0.7)" : "rgba(200,168,75,0.7)";

		lblCargaFase.setStyle("-fx-font-family: Georgia; -fx-font-size: 52px; -fx-font-weight: bold;"
				+ " -fx-text-fill: " + color + ";" + " -fx-effect: dropshadow(gaussian, " + shadow + ", 25, 0, 0, 0);");
		lblCargaSubtitulo.setStyle("-fx-font-family: Georgia; -fx-font-size: 17px; -fx-font-style: italic;"
				+ " -fx-text-fill: " + color + ";");
		barCarga.setStyle("-fx-accent: " + color + ";");

		generarParticulasCarga();
		barCarga.setProgress(0);
		pantallaEntrefase.setVisible(true);
		pantallaEntrefase.setManaged(true);

		// animar la barra de progreso
		Timeline tl = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(barCarga.progressProperty(), 0.0)),
				new KeyFrame(Duration.seconds(2.5), new KeyValue(barCarga.progressProperty(), 1.0)));
		tl.setOnFinished(ev -> {
			pantallaEntrefase.setVisible(false);
			pantallaEntrefase.setManaged(false);
			prepararCombate(); // arranca el combate de la nueva fase
		});
		tl.play();
	}

	/**
	 * Devuelve la frase temática que se muestra en el overlay de carga para cada
	 * fase.
	 */
	private String consejoFase(int fase) {
		switch (fase) {
		case 1:
			return "«Tu aventura comienza ahora. Que tu valor te guíe en las profundidades de la mazmorra.»";
		case 2:
			return "«La mazmorra se vuelve más peligrosa. Conserva tus recursos para los momentos de mayor necesidad.»";
		case 3:
			return "«Los monstruos de las profundidades no conocen la piedad. Mantén la guardia alta.»";
		case 4:
			return "«El Dragón te aguarda en lo más profundo de la mazmorra. Esta es tu última oportunidad de demostrar tu valía.»";
		default:
			return "";
		}
	}

	/**
	 * Genera partículas doradas animadas dentro del panel de carga.
	 */
	private void generarParticulasCarga() {
		panelParticulasCarga.getChildren().clear();
		Random rnd = new Random(42);
		for (int i = 0; i < 60; i++) {
			double x = rnd.nextDouble() * 900, y = rnd.nextDouble() * 650;
			double r = 0.5 + rnd.nextDouble() * 1.2, o = 0.2 + rnd.nextDouble() * 0.5;
			Circle c = new Circle(x, y, r, Color.web("#c8a84b", o));
			FadeTransition ft = new FadeTransition(Duration.seconds(2 + rnd.nextDouble() * 3), c);
			ft.setFromValue(o * 0.3);
			ft.setToValue(o);
			ft.setAutoReverse(true);
			ft.setCycleCount(Animation.INDEFINITE);
			ft.setDelay(Duration.seconds(rnd.nextDouble() * 4));
			ft.play();
			panelParticulasCarga.getChildren().add(c);
		}
	}

	/**
	 * Devuelve los PM actuales del héroe si es "Magico", o 0 si no usa magia.
	 * Centraliza la comprobación para no duplicarla en cada método de guardado.
	 */
	private int pmActualHeroe() {
		Heroe h = sesion.getHeroe();
		return (h instanceof Magico) ? ((Magico) h).getPm() : 0;
	}

	/**
	 * Garantiza que existe una fila para esta sesión. Si ya existe, no hace nada.
	 * Si no, realiza un INSERT con el estado actual del combate (fase, HP/PM del
	 * héroe, tipo y HP/PM del enemigo activo).
	 */
	private void asegurarPartidaCreada() {
		if (sesion.getPartida() != null) {
			return;
		}
		try {
			Heroe heroe = sesion.getHeroe();
			Partida p = new Partida(sesion.getJugador().getId(), heroe.getId(), sesion.getFaseActual(),
					heroe.getPuntosGolpe());
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
	 * Guarda la partida en BD tras una huida exitosa.
	 */
	private void guardarAlHuir() {
		try {
			Heroe heroe = sesion.getHeroe();
			Enemigo enemigo = motor.getEnemigo();
			if (sesion.getPartida() == null) {
				// La partida todavía no existe en BD (el jugador huyó en fase 1 sin
				// haber ganado ningún combate previo): crear la fila ahora.
				Partida p = new Partida(sesion.getJugador().getId(), heroe.getId(), sesion.getFaseActual(),
						heroe.getPuntosGolpe());
				p.setPmActual(pmActualHeroe());
				// Guardar el estado actual del enemigo (sigue vivo tras la huida)
				p.setTipoEnemigo(enemigo.getTipo());
				p.setHpEnemigo(enemigo.getPuntosGolpe());
				p.setPmEnemigo(enemigo.getPm());
				PartidaDAO.insertar(p);
				sesion.setPartida(p);
			} else {
				Partida p = sesion.getPartida();
				p.setFaseActual(sesion.getFaseActual());
				p.setHpActual(heroe.getPuntosGolpe());
				p.setPmActual(pmActualHeroe());
				p.setEstado(modelo.Partida.Estado.EN_CURSO);
				// Guardar el estado actual del enemigo (sigue vivo tras la huida)
				p.setTipoEnemigo(enemigo.getTipo());
				p.setHpEnemigo(enemigo.getPuntosGolpe());
				p.setPmEnemigo(enemigo.getPm());
				PartidaDAO.actualizar(p);
			}
			PersonajeDAO.actualizarHp(heroe.getId(), heroe.getPuntosGolpe());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Detiene la música, actualiza el estado final de la partida en BD y navega a
	 * la pantalla de resultado.
	 */
	private void navegarAResultado(boolean victoria) {
		detenerMusica();
		try {
			if (sesion.getPartida() != null) {
				sesion.getPartida()
						.setEstado(victoria ? modelo.Partida.Estado.COMPLETADA : modelo.Partida.Estado.DERROTA);
				sesion.getPartida().setHpActual(sesion.getHeroe().getPuntosGolpe());
				PartidaDAO.actualizar(sesion.getPartida());
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/Resultado.fxml"));
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
			magico.setPm(magico.getPm() + pmRecuperado); // setPm clampea al máximo automáticamente
			ratioPmFinal = magico.getPmMax() > 0 ? (double) magico.getPm() / magico.getPmMax() : 0.0;
		}

		// Guardar partida con los valores ya recuperados
		guardarAlHuir();

		lblDescansoHp.setText(
				"+" + hpRecuperado + " HP  →  " + heroe.getPuntosGolpe() + " / " + heroe.getPuntosGolpeMax() + " HP");

		if (esMagico) {
			Magico magico = (Magico) heroe;
			lblDescansoPm.setText("+" + pmRecuperado + " PM  →  " + magico.getPm() + " / " + magico.getPmMax() + " PM");
			filaDescansopm.setVisible(true);
			filaDescansopm.setManaged(true);
		}

		// Mostrar overlay
		overlayDescanso.setMouseTransparent(false);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(500), overlayDescanso);
		fadeIn.setToValue(1.0);
		fadeIn.play();

		// Animaciones de barras
		final double hpTarget = ratioHpFinal;
		final double pmTarget = ratioPmFinal;

		Timeline barrasAnim = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(barraDescansoHp.progressProperty(), 0),
						new KeyValue(barraDescansoPm.progressProperty(), 0)),
				new KeyFrame(Duration.seconds(1.5), new KeyValue(barraDescansoHp.progressProperty(), hpTarget),
						new KeyValue(barraDescansoPm.progressProperty(), pmTarget)));
		barrasAnim.play();

		// Barra de progreso general
		Timeline progreso = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(barCargaDescanso.progressProperty(), 0)),
				new KeyFrame(Duration.seconds(3.5), new KeyValue(barCargaDescanso.progressProperty(), 1)));
		progreso.setOnFinished(e -> navegarAMenu());
		progreso.play();
	}

	/**
	 * Detiene la música y navega al menú principal. Se usa al huir del combate (la
	 * partida ya ha sido guardada antes).
	 */
	private void navegarAMenu() {
		detenerMusica();
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
			Stage stage = (Stage) btnAtacar.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ── UI helpers ────────────────────────────────────────────────────────────

	/**
	 * Refresca la barra de vida del héroe y el label numérico de HP.
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
	 */
	private void actualizarBarraPm() {
		if (!(sesion.getHeroe() instanceof Magico)) {
			return;
		}
		Magico m = (Magico) sesion.getHeroe();
		double pct = m.getPorcentajePm();
		barraPoderMagico.setProgress(pct);
		lblPmHeroe.setText(m.getPm() + " / " + m.getPmMax() + " PM");
		String color = pct > 0.3 ? "#7c6fcd" : "#4a3d8f";
		barraPoderMagico.setStyle("-fx-accent: " + color + ";");
	}

	/**
	 * Refresca la barra de Energía del Guerrero y el label numérico.
	 */
	private void actualizarBarraEnergia() {
		if (!(sesion.getHeroe() instanceof Guerrero)) {
			return;
		}
		Guerrero g = (Guerrero) sesion.getHeroe();
		barraEnergia.setProgress(g.getPorcentajeEnergia());
		lblEnergiaHeroe.setText(g.getEnergia() + " / " + g.getEnergiaMax() + " EN");
	}

	// Sistema de drops

	/**
	 * Comprueba si el enemigo derrotado suelta un objeto y, en caso afirmativo, lo
	 * añade al inventario del héroe y muestra un mensaje en el log de combate.
	 */
	private void procesarDrop(Enemigo enemigo) {
		TipoDrop drop = enemigo.generarDrop();
		if (drop == null) {
			return;
		}

		String msg;
		switch (drop) {
		case POCION_VIDA:
			pocionesRestantes++;
			msg = String.format("💊  ¡%s ha soltado una Poción de Curación!  [Pociones: ×%d]", enemigo.getNombre(),
					pocionesRestantes);
			break;
		case POCION_MAGICA:
			pocionesMagicasRestantes++;
			msg = String.format("🔮  ¡%s ha soltado una Poción Mágica!  [Pociones mágicas: ×%d]", enemigo.getNombre(),
					pocionesMagicasRestantes);
			break;
		default:
			return;
		}

		agregarLog(msg);

		// Refrescar el botón de objetos para que refleje el nuevo stock
		btnObjetos.setDisable(false);
		actualizarEtiquetasPociones();
	}

	/**
	 * Actualiza los textos de los botones de poción dentro del submenú de objetos
	 * para reflejar el stock actual.
	 */
	private void actualizarEtiquetasPociones() {
		if (btnPocionCuracion != null) {
			if (pocionesRestantes > 0) {
				btnPocionCuracion.setText("🧪 POCIÓN DE CURACIÓN  ×" + pocionesRestantes);
				btnPocionCuracion.setDisable(false);
			} else {
				btnPocionCuracion.setText("🧪 POCIÓN DE CURACIÓN  (agotadas)");
				btnPocionCuracion.setDisable(true);
			}
		}
		if (btnPocionMagica != null) {
			if (pocionesMagicasRestantes > 0) {
				btnPocionMagica.setText("🔮 POCIÓN MÁGICA  ×" + pocionesMagicasRestantes);
				btnPocionMagica.setDisable(false);
			} else {
				btnPocionMagica.setText("🔮 POCIÓN MÁGICA  (agotadas)");
				btnPocionMagica.setDisable(true);
			}
		}
	}

	/**
	 * Regenera la energía del Guerrero al final de cada turno y refresca la barra.
	 */
	private void regenerarEnergiaGuerrero() {
		if (!(sesion.getHeroe() instanceof Guerrero)) {
			return;
		}
		((Guerrero) sesion.getHeroe()).regenerarEnergia();
		actualizarBarraEnergia();
	}

	/**
	 * Refresca la barra de vida del enemigo y el label numérico de HP.
	 */
	private void actualizarBarraEnemigo() {
		Enemigo e = motor.getEnemigo();
		double pct = e.getPorcentajeVida();
		barraVidaEnemigo.setProgress(pct);
		lblHpEnemigo.setText(e.getPuntosGolpe() + " / " + e.getPuntosGolpeMax() + " HP");
		colorearBarra(barraVidaEnemigo, pct);
	}

	/**
	 * Refresca la barra de PM del enemigo mágico (Saga, Dragón) y el label
	 * numérico.
	 */
	private void actualizarBarraPmEnemigo() {
		Enemigo enemigo = motor.getEnemigo();
		double pct = enemigo.getPorcentajePm();
		barraPmEnemigo.setProgress(pct);
		lblPmEnemigo.setText(enemigo.getPm() + " / " + enemigo.getPmMax() + " PM");
		// Oscurece el morado cuando los PM están bajos para dar señal visual
		String color = pct > 0.3 ? "#7c6fcd" : "#4a3d8f";
		barraPmEnemigo.setStyle("-fx-accent: " + color + ";");
	}

	/**
	 * Aplica un color dinámico a una barra de progreso según el porcentaje:
	 */
	private void colorearBarra(ProgressBar barra, double pct) {
		String color;
		if (pct > 0.5) {
			color = "#4caf50"; // verde: más de la mitad de vida
		} else if (pct > 0.25) {
			color = "#ff9800"; // naranja: vida baja
		} else {
			color = "#e05555"; // rojo: vida crítica
		}
		barra.setStyle("-fx-accent: " + color + ";");
	}

	/**
	 * Añade una línea al log de combate seguida de un salto de línea.
	 */
	private void agregarLog(String mensaje) {
		txtLog.appendText(mensaje + "\n");
	}

	/**
	 * Aplica un efecto de "temblor" horizontal al nodo indicado, simulando que
	 * recibe un golpe.
	 */
	private void animarGolpe(Node objetivo) {
		if (objetivo == null) {
			return;
		}

		final int PIXELS_SACUDIDA = 8;
		final double MS_POR_PASO = 50;

		SequentialTransition sacudida = new SequentialTransition(desplazar(objetivo, -PIXELS_SACUDIDA, MS_POR_PASO),
				desplazar(objetivo, PIXELS_SACUDIDA * 2, MS_POR_PASO),
				desplazar(objetivo, -PIXELS_SACUDIDA * 2, MS_POR_PASO),
				desplazar(objetivo, PIXELS_SACUDIDA * 2, MS_POR_PASO),
				desplazar(objetivo, -PIXELS_SACUDIDA, MS_POR_PASO) // volver al centro
		);
		sacudida.play();
	}

	/**
	 * Crea una transición que desplaza el nodo del eje
	 */
	private TranslateTransition desplazar(Node nodo, double deltaX, double duracionMs) {
		TranslateTransition paso = new TranslateTransition(Duration.millis(duracionMs), nodo);
		paso.setByX(deltaX);
		return paso;
	}

	/**
	 * Carga un clip en memoria para reproducirlo al pasar el cursor sobre los
	 * botones de combate.
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
	 * Carga el clip en memoria para reproducirlo al ejecutar ataques normales
	 * (héroe y enemigo).
	 */
	private void inicializarSonidoAtaque() {
		try {
			URL url = getClass().getResource("/recursos/audio/ataque.mp3");
			if (url != null) {
				sonidoAtaque = new AudioClip(url.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reproduce el clip de audio asociado a una habilidad
	 */
	private void reproducirSonidoHabilidad(Habilidad habilidad) {
		String ruta = habilidad.getRutaAudio();
		if (ruta == null) {
			return;
		}
		try {
			AudioClip clip = cacheSonidosHabilidad.computeIfAbsent(ruta, r -> {
				URL url = getClass().getResource(r);
				return url != null ? new AudioClip(url.toString()) : null;
			});
			if (clip != null) {
				clip.play();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Registra el sonido de hover en un botón dado.
	 */
	private void agregarSonidoHover(Button btn) {
		if (btn == null) {
			return;
		}
		btn.setOnMouseEntered(e -> {
			if (sonidoHover != null) {
				sonidoHover.play();
			}
		});
	}

	/**
	 * Registra el sonido de hover en todos los botones estáticos de la pantalla de
	 * combate.
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
	 * Inicia la música de fondo del combate. Detiene cualquier música previa antes
	 * de arrancar la nueva.
	 */
	private void iniciarMusica(boolean esFaseFinal) {
		// Detener cualquier música anterior (importante al cambiar de fase)
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
		try {
			String archivo = esFaseFinal ? "/recursos/audio/finalBoss.mp3" : "/recursos/audio/Battle.mp3";
			URL recurso = getClass().getResource(archivo);
			if (recurso == null) {
				return;
			}
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
	 */
	private void detenerMusica() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.dispose(); // libera hilos nativos de audio
			mediaPlayer = null;
		}
	}
}
