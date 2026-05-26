package controlador;

import controlador.util.Particulas;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

public class MazmorraController implements Initializable {

	@FXML
	private Label lblFase;

	@FXML
	private Label lblNombreHeroe;

	@FXML
	private Label lblHpHeroe;

	@FXML
	private ProgressBar barraVidaHeroe;

	@FXML
	private Label lblNombreEnemigo;

	@FXML
	private ProgressBar barraVidaEnemigo;

	@FXML
	private Label lblHpEnemigo;

	@FXML
	private ImageView imgHeroe;

	@FXML
	private ImageView imgEnemigo;

	@FXML
	private Label lblPrompt; // prompt caja diálogo

	@FXML
	private TextArea txtLog;

	@FXML
	private Label lblResultado;

	@FXML
	private VBox menuBatalla;

	@FXML
	private Button btnAtacar;

	@FXML
	private Button btnObjetos;

	@FXML
	private Button btnMagia;

	@FXML
	private Button btnHabilidades;

	@FXML
	private Button btnHuir;

	@FXML
	private Button btnContinuar;

	@FXML
	private VBox menuMagia;

	@FXML
	private VBox contenedorHabilidades; // se generan los botones de habilidades mágicas

	@FXML
	private VBox menuHabilidades;

	@FXML
	private VBox contenedorHabilidadesGuerrero;

	@FXML
	private VBox menuObjetos;

	@FXML
	private Button btnPocionCuracion;

	@FXML
	private Button btnPocionMagica;

	@FXML
	private HBox filaPm;

	@FXML
	private HBox filaNumPm;

	@FXML
	private ProgressBar barraPoderMagico;

	@FXML
	private Label lblPmHeroe;

	@FXML
	private HBox filaEnergia;

	@FXML
	private HBox filaNumEnergia;

	@FXML
	private ProgressBar barraEnergia;

	@FXML
	private Label lblEnergiaHeroe;

	@FXML
	private HBox filaPmEnemigo;

	@FXML
	private HBox filaNumPmEnemigo;

	@FXML
	private ProgressBar barraPmEnemigo;

	@FXML
	private Label lblPmEnemigo;

	@FXML
	private StackPane overlayHuida;

	@FXML
	private Label lblHuidaBase; // Línea base "Probabilidad base: 40%" del desglose.

	@FXML
	private Label lblHuidaVida; // Modificador de HP del héroe.

	@FXML
	private Label lblHuidaClase; // modificador clase heroe

	@FXML
	private Label lblHuidaEnemigo; // modificador tipo enemigo

	@FXML
	private Label lblHuidaProbFinal;

	@FXML
	private Button btnConfirmarHuida;

	@FXML
	private Button btnCancelarHuida;

	@FXML
	private StackPane overlayDescanso;

	@FXML
	private ProgressBar barraDescansoHp;

	@FXML
	private Label lblDescansoHp;

	@FXML
	private VBox filaDescansopm;

	@FXML
	private ProgressBar barraDescansoPm;

	@FXML
	private Label lblDescansoPm;

	@FXML
	private ProgressBar barCargaDescanso;

	// PANTALLA DE CARGA

	@FXML
	private StackPane pantallaEntrefase;

	@FXML
	private Pane panelParticulasCarga;

	@FXML
	private Label lblCargaIcono;

	@FXML
	private Label lblCargaFase;

	@FXML
	private Label lblCargaSubtitulo; // Label que indica la fase.

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

	private boolean combateTerminado = false;

	private MediaPlayer mediaPlayer;

	private AudioClip sonidoHover;

	private AudioClip sonidoAtaque;

	private final Map<String, AudioClip> cacheSonidosHabilidad = new HashMap<>();

	// INVENTARIO

	private int pocionesRestantes;

	private static final int CURACION_POCION = 30;

	private int pocionesMagicasRestantes;

	private static final int RESTAURACION_PM_POCION = 10;

	private static final int RECUPERACION_HUIDA_PCT = 25; //ph y pm recuperado tras huida

	// INITIALIZE ----------------

	@Override
	public void initialize(URL url, ResourceBundle rb) { 
		/* configurado en iniciarSesion */ }

	/**
	 * Punto de entrada principal del controlador.
	 */
	public void iniciarSesion(GameSession sesion) {
		this.sesion = sesion; //guardamos referencia sesión
		pocionesRestantes = 3;
		pocionesMagicasRestantes = 2;
		sonidoHover = cargarAudioClip("/recursos/audio/cursor.wav");
		sonidoAtaque = cargarAudioClip("/recursos/audio/ataque.mp3");
		configurarSonidoBotones();
		//Mostrar pantalla de carga antes de la fase actual
		mostrarPantallaCarga();
	}

	/**
	 * Configura toda la interfaz para el combate de la fase actual. Se llama al
	 * inicio de cada fase.
	 */
	private void prepararCombate() {
		int fase = sesion.getFaseActual();
		Heroe heroe = sesion.getHeroe();

		Partida partidaActual = sesion.getPartida();
		Enemigo enemigo;
		
		if (partidaActual != null && partidaActual.getTipoEnemigo() != null && partidaActual.getHpEnemigo() > 0) {
			enemigo = MotorCombate.generarEnemigoDeTipo(partidaActual.getTipoEnemigo()); //si existe se regenera
			enemigo.setPuntosGolpe(partidaActual.getHpEnemigo());
			enemigo.setPm(partidaActual.getPmEnemigo()); 
		} else {
			enemigo = MotorCombate.generarEnemigo(fase);//aleatorio
		}

		heroe.reiniciarHabilidad(); // resetea habilidades especiales

		motor = new MotorCombate(heroe, enemigo);
		combateTerminado = false;

		// Labels de fase
		lblFase.setText("⚔  FASE " + fase + (fase == 4 ? "  —  JEFE FINAL" : "  —  MAZMORRA"));

		// Héroe: nombre, imagen y barra de vida
		lblNombreHeroe.setText(heroe.getNombre() + " (" + heroe.getTipo() + ")");
		cargarSprite(imgHeroe, heroe.getRutaImagen());
		actualizarBarraHeroe();

		// Enemigo: nombre, imagen y barra de vida
		lblNombreEnemigo.setText(enemigo.getNombre() + " (" + enemigo.getTipo() + ")");
		cargarSprite(imgEnemigo, enemigo.getRutaImagen());
		actualizarBarraEnemigo();

		// Barra de PM del enemigo 
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

		// Música
		iniciarMusica(sesion.getFaseActual() == 4); //inicia música, usa pista jefe si fase == 4
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
	 * Maneja la pulsación del botón "OBJETOS". Abre el submenú de objetos si el
	 * inventario tiene al menos un objeto disponible. Si el inventario está vacío
	 * muestra un mensaje en el log y no abre el submenú.
	 */
	@FXML
	private void handleObjetos() {
		if (combateTerminado) { //si el combate terminó, ignoramos
			return;
		}
		if (pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0) {
			agregarLog("🎒 No te quedan objetos.");
			return;
		}
		actualizarSubmenuObjetos();
		abrirSubmenu(menuObjetos);
	}

	/**
	 * Maneja el botón "HUIR": calcula la probabilidad de huida y muestra el overlay
	 * in-game con el desglose de modificadores.
	 */
	@FXML
	private void handleHuir() {
		if (combateTerminado) { //evita abrir el overlay si el combate terminó
			return;
		}

		Heroe heroe = sesion.getHeroe();
		Enemigo enemigo = motor.getEnemigo();

		// Modificador por HP

		int pctVida = (int) ((heroe.getPuntosGolpe() * 100.0) / heroe.getPuntosGolpeMax()); //calcula el % de vida héroe actual
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
		overlayHuida.setVisible(false); //ocultamos
		overlayHuida.setManaged(false); //elimina el overlay del flujo del layout
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
	 * Habilita o deshabilita todos los botones de acción de combate.Se llama con false al inicio
	 * de cada turno para evitar acciones dobles.
	 */
	private void setEstadoBotonesCombate(boolean activos) {
		boolean deshabilitar = !activos;
		btnAtacar.setDisable(deshabilitar);
		btnMagia.setDisable(deshabilitar);
		btnHabilidades.setDisable(deshabilitar);
		btnHuir.setDisable(deshabilitar);

		if (!activos) { //si se deshabilita todo, el inventario se bloquea
			btnObjetos.setDisable(true);
			return;
		}

		// Objetos
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
			setEstadoBotonesCombate(true);
		}
	}

	/**
	 * Procesa un turno completo según la acción elegida por el jugador.
	 */
	private void ejecutarTurno(AccionHeroe accion) {
		if (combateTerminado) { // evita que acciones tardías procesen turno
			return;
		}
		setEstadoBotonesCombate(false);

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
		// :: pasa el método como ref
		motor.ejecutarAtaqueBasico().forEach(this::agregarLog); //ejecuta el ataque en motor y añade cada línea de resultado al log
		actualizarBarraEnemigo(); //refleja el daño inflingido
		if (motor.getEnemigo().tienePmMax()) {
			actualizarBarraPmEnemigo();
		}
		animarGolpe(imgEnemigo);

		if (motor.haTerminado()) { //si derrotamos al enemigo
			agregarLog("");
			actualizarTodasLasBarras();
			combateTerminado = true;
			procesarFinCombate(motor.getResultado()); 
			return;
		}
		// Pasamos a fase 2
		ejecutarFase2Enemiga(true);
	}

	/**
	 * Maneja habilidad especial elegida desde el submenú.
	 */
	private void ejecutarTurnoHabilidad(Habilidad habilidad) {
		if (combateTerminado) {
			return;
		}
		setEstadoBotonesCombate(false);

		Heroe heroe = sesion.getHeroe();

		// Verificamos recursos
		if (!habilidad.puedeUsarse(heroe)) {
			agregarLog("⚠ No puedes usar " + habilidad.getNombre() + "  — " + motivoNoDisponible(heroe, habilidad));
			setEstadoBotonesCombate(true);
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

		// Verificamos si el combate ha terminado
		motor.verificarResultado().forEach(this::agregarLog);

		if (motor.haTerminado()) {
			agregarLog("");
			combateTerminado = true;
			procesarFinCombate(motor.getResultado());
			return;
		}

		// Las habilidades especiales no regeneran Energía del Guerrero
		ejecutarFase2Enemiga(false);
	}

	/**
	 * Devuelve una cadena explicando el por qué una habilidad no está disponible.
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
		return "(no disponible)"; //Por defecto
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

		ejecutarFase2Enemiga(false);
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
	 * Ejecuta la Fase 2 del turno: tras una pausa, el enemigo reacciona,
	 * se actualizan todas las barras y se cierra el turno.
	 */
	private void ejecutarFase2Enemiga(boolean regenerarEnergia) {
		pausarYEjecutar(Duration.millis(750), () -> { //pausa antes del ataque
			
			if (sonidoAtaque != null) {
				sonidoAtaque.play();
			}
			
			motor.ejecutarReaccionEnemigo().forEach(this::agregarLog); //this:: pasamos método como ref
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
	 * Crea una pausa no bloqueante y ejecuta accionAlTerminar cuando finaliza.
	 */
	private void pausarYEjecutar(Duration duracion, Runnable accionAlTerminar) {
		PauseTransition pausa = new PauseTransition(duracion);
		pausa.setOnFinished(e -> accionAlTerminar.run());
		pausa.play();
	}

	/**
	 * Procesa el fin del combate. Desactiva los botones, registra el combate en BD,
	 * actualiza la puntuación y muestra el botón de continuar.
	 */
	private void procesarFinCombate(ResultadoCombate resultado) {
		// Deshabilitar todos los botones de acción
		btnAtacar.setDisable(true);
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

		boolean victoria;
		
		if (resultado == ResultadoCombate.VICTORIA) {
		    victoria = true;
		} else {
		    victoria = false;
		}

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
	 * Abre el submenú de magia, reconstruyendo los botones según el estado actual.
	 */
	@FXML
	private void handleMagia() {
		if (combateTerminado) { //no abrir si combate ya terminó
			return;
		}
		construirBotonesHabilidades(sesion.getHeroe(), contenedorHabilidades);
		abrirSubmenu(menuMagia);
	}

	/**
	 * Cierra el submenú de magia y vuelve al menú principal de batalla.
	 */
	@FXML
	private void handleVolverMenu() {
		cerrarSubmenu(menuMagia);
	}

	/**
	 * Oculta el submenú indicado y vuelve a mostrar el menú principal de batalla.
	 * Centraliza el comportamiento común de los tres handlers "volver".
	 */
	private void cerrarSubmenu(VBox submenu) {
		submenu.setVisible(false);
		submenu.setManaged(false);
		menuBatalla.setVisible(true);
		menuBatalla.setManaged(true);
	}

	/**
	 * Oculta el menú principal de batalla y muestra el submenú indicado.
	 */
	private void abrirSubmenu(VBox submenu) {
		menuBatalla.setVisible(false);
		menuBatalla.setManaged(false);
		submenu.setVisible(true);
		submenu.setManaged(true);
	}

	/**
	 * Construye dinámicamente los botones de habilidades.
	 */
	private void construirBotonesHabilidades(Heroe heroe, VBox contenedor) {
		contenedor.getChildren().clear(); //eliminamos botones para reconstruirlos

		for (Habilidad habilidad : heroe.getHabilidades()) {
			Button btn = new Button();
			btn.getStyleClass().add("btn-batalla-barra");
			btn.setMaxWidth(Double.MAX_VALUE);
			btn.setPrefHeight(36);
			btn.setMinHeight(32);
			VBox.setVgrow(btn, javafx.scene.layout.Priority.ALWAYS);
			agregarSonidoHover(btn);

			Tooltip tip = new Tooltip(habilidad.getDescripcion()); //texto que aparece en elemento
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
		cerrarSubmenu(menuObjetos);
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
		abrirSubmenu(menuHabilidades);
	}

	/**
	 * Cierra el submenú de habilidades del Guerrero y vuelve al menú principal.
	 */
	@FXML
	private void handleVolverMenuHabilidades() {
		cerrarSubmenu(menuHabilidades);
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

		Particulas.generar(panelParticulasCarga, 60, 42, 2);
		barCarga.setProgress(0);
		pantallaEntrefase.setVisible(true);
		pantallaEntrefase.setManaged(true);

		// animar la barra de progreso
		Timeline tl = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(barCarga.progressProperty(), 0.0)),
				new KeyFrame(Duration.seconds(2.5), new KeyValue(barCarga.progressProperty(), 1.0)));
		tl.setOnFinished(ev -> { //al terminar animación oculta overlay e inicia combate
			pantallaEntrefase.setVisible(false);
			pantallaEntrefase.setManaged(false);
			prepararCombate(); 
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
	 * Devuelve los PM actuales del héroe si es "Magico", o 0 si no usa magia.
	 * Centraliza la comprobación para no duplicarla en cada método de guardado.
	 */
	private int pmActualHeroe() {
		Heroe h = sesion.getHeroe();
		return (h instanceof Magico) ? ((Magico) h).getPm() : 0;
	}

	/**
	 * Crea una fila de partida en BD si la sesión todavía no tiene una asociada. 
	 */
	private void crearPartidaSiHaceFalta() throws SQLException {
		if (sesion.getPartida() != null) {
			return;
		}
		Heroe heroe = sesion.getHeroe();
		Enemigo enemigo = motor.getEnemigo();
		Partida p = new Partida(sesion.getJugador().getId(), heroe.getId(), sesion.getFaseActual(),
				heroe.getPuntosGolpe());
		p.setPmActual(pmActualHeroe());
		p.setTipoEnemigo(enemigo.getTipo());
		p.setHpEnemigo(enemigo.getPuntosGolpe());
		p.setPmEnemigo(enemigo.getPm());
		PartidaDAO.insertar(p);
		sesion.setPartida(p);
	}

	/**
	 * Garantiza que la partida exista en BD para poder anclarle un combate por FK,
	 * y sincroniza el HP del personaje. No actualiza el resto del estado.
	 */
	private void asegurarPartidaCreada() {
		try {
			crearPartidaSiHaceFalta();
			Heroe heroe = sesion.getHeroe();
			PersonajeDAO.actualizarHp(heroe.getId(), heroe.getPuntosGolpe());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Guarda la partida en BD tras una huida exitosa: si no existe la crea, y en
	 * cualquier caso vuelca el estado actual (fase, HP/PM del héroe y del enemigo,
	 * estado EN_CURSO) para que el jugador pueda reanudar.
	 */
	private void guardarAlHuir() {
		try {
			crearPartidaSiHaceFalta();
			Heroe heroe = sesion.getHeroe();
			Enemigo enemigo = motor.getEnemigo();
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
	 * Refresca la barra de PM del héroe mágico y el label numérico. El color de la
	 * barra lo define la clase CSS {@code barra-pm-pokemon}.
	 */
	private void actualizarBarraPm() {
		if (!(sesion.getHeroe() instanceof Magico)) {
			return;
		}
		Magico m = (Magico) sesion.getHeroe();
		barraPoderMagico.setProgress(m.getPorcentajePm());
		lblPmHeroe.setText(m.getPm() + " / " + m.getPmMax() + " PM");
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
		actualizarSubmenuObjetos();
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
		barraPmEnemigo.setProgress(enemigo.getPorcentajePm());
		lblPmEnemigo.setText(enemigo.getPm() + " / " + enemigo.getPmMax() + " PM");
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
	 * Carga un clip de audio desde el classpath.
	 */
	private AudioClip cargarAudioClip(String ruta) {
		try {
			URL url = getClass().getResource(ruta);
			if (url != null) {
				return new AudioClip(url.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Carga una imagen o Sprites de personajes.
	 */
	private void cargarSprite(ImageView destino, String ruta) {
		try {
			destino.setImage(new Image(getClass().getResourceAsStream(ruta)));
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
