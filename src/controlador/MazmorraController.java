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
 * Controlador de la pantalla de combate en la mazmorra (estilo Pokémon).
 *
 * Gestiona los turnos, actualiza la UI y guarda la partida tras cada fase.
 * El menú de batalla ofrece 4 opciones: ATAQUE, OBJETOS, HABILIDAD y HUIDA.
 */
public class MazmorraController implements Initializable {

    // ── FXML: cabecera y escena ──────────────────────────────────────────────
    @FXML private Label       lblFase;
    @FXML private Label       lblNombreHeroe;
    @FXML private Label       lblHpHeroe;
    @FXML private ProgressBar barraVidaHeroe;
    @FXML private Label       lblNombreEnemigo;
    @FXML private ProgressBar barraVidaEnemigo;
    @FXML private Label       lblHpEnemigo;
    @FXML private ImageView   imgHeroe;
    @FXML private Label       lblIconoEnemigo;

    // ── FXML: caja de diálogo y menú ─────────────────────────────────────────
    @FXML private Label       lblPrompt;
    @FXML private TextArea    txtLog;
    @FXML private Label       lblResultado;
    @FXML private VBox        menuBatalla;
    @FXML private Button      btnAtacar;
    @FXML private Button      btnObjetos;
    @FXML private Button      btnHabilidad;
    @FXML private Button      btnMagia;          // solo visible para personajes Magico
    @FXML private Button      btnHuir;
    @FXML private Button      btnContinuar;

    // ── FXML: submenú de magia ────────────────────────────────────────────────
    @FXML private VBox        menuMagia;
    @FXML private VBox        contenedorHabilidades;

    // ── FXML: submenú de objetos ──────────────────────────────────────────────
    @FXML private VBox        menuObjetos;
    @FXML private Button      btnPocionCuracion;
    @FXML private Button      btnPocionMagica;

    // ── FXML: barra de PM (solo visible en personajes Magico) ─────────────────
    @FXML private HBox        filaPm;
    @FXML private HBox        filaNumPm;
    @FXML private ProgressBar barraPoderMagico;
    @FXML private Label       lblPmHeroe;

    // ── Estado ────────────────────────────────────────────────────────────────
    private GameSession  sesion;
    private MotorCombate motor;
    private boolean      combateTerminado = false;
    private MediaPlayer mediaPlayer;

    // ── Inventario simple del combate ────────────────────────────────────────
    /** Pociones de curación (se reinician cada fase). */
    private int pocionesRestantes = 3;
    private static final int CURACION_POCION = 30;

    /** Pociones mágicas (se reinician cada fase; los PM NO se reinician). */
    private int pocionesMagicasRestantes = 2;
    private static final int RESTAURACION_PM_POCION = 10;

    @Override
    public void initialize(URL url, ResourceBundle rb) { /* configuración en iniciarSesion */ }

    /** Punto de entrada: recibe la sesión del controlador anterior. */
    public void iniciarSesion(GameSession sesion) {
        this.sesion = sesion;
        prepararCombate();
    }

    // ── Preparación ───────────────────────────────────────────────────────────

    private void prepararCombate() {
        int fase = sesion.getFaseActual();
        Heroe   heroe   = sesion.getHeroe();
        Enemigo enemigo = MotorCombate.generarEnemigo(fase);

        heroe.reiniciarHabilidad();   // la habilidad especial se recarga entre fases
        motor = new MotorCombate(heroe, enemigo);
        combateTerminado = false;
        pocionesRestantes        = 3; // 3 pociones de curación por fase
        pocionesMagicasRestantes = 2; // 2 pociones mágicas por fase

        // ── Labels de fase
        lblFase.setText("⚔  FASE " + fase + (fase == 4 ? "  —  JEFE FINAL" : "  —  MAZMORRA"));

        // ── Héroe
        lblNombreHeroe.setText(heroe.getNombre() + " (" + heroe.getTipo() + ")");
        try {
            Image imgSrc = new Image(getClass().getResourceAsStream(heroe.getRutaImagen()));
            imgHeroe.setImage(imgSrc);
        } catch (Exception e) {
            // Si la imagen no carga, no bloquea el juego
            e.printStackTrace();
        }
        actualizarBarraHeroe();

        // ── Enemigo
        lblNombreEnemigo.setText(enemigo.getNombre() + " (" + enemigo.getTipo() + ")");
        lblIconoEnemigo.setText(enemigo.getIcono());
        actualizarBarraEnemigo();

        // ── Habilidad especial / Magia
        // Para héroes Mágicos se muestra el botón MAGIA (submenú) en lugar de HABILIDAD directo
        boolean esMagico = heroe instanceof Magico;
        btnHabilidad.setVisible(!esMagico);
        btnHabilidad.setManaged(!esMagico);
        btnMagia.setVisible(esMagico);
        btnMagia.setManaged(esMagico);

        if (esMagico) {
            btnMagia.setDisable(false);
            construirSubmenuMagia((Magico) heroe);
        } else {
            btnHabilidad.setText("✨ " + heroe.getNombreHabilidad().toUpperCase());
            btnHabilidad.setDisable(false);
        }

        // Aseguramos que los submenús empiecen ocultos
        if (menuMagia != null) {
            menuMagia.setVisible(false);
            menuMagia.setManaged(false);
        }
        if (menuObjetos != null) {
            menuObjetos.setVisible(false);
            menuObjetos.setManaged(false);
        }

        // ── Barra de PM: solo para personajes mágicos
        filaPm.setVisible(esMagico);
        filaPm.setManaged(esMagico);
        filaNumPm.setVisible(esMagico);
        filaNumPm.setManaged(esMagico);
        if (esMagico) actualizarBarraPm();

        // ── Objetos
        actualizarTextoBotonObjetos();

        // ── UI inicial
        txtLog.clear();
        agregarLog("¡Un " + enemigo.getNombre() + " salvaje apareció!");
        agregarLog("");
        agregarLog(heroe.getDescHabilidad());
        agregarLog("");

        if (lblPrompt != null) {
            lblPrompt.setText("¿Qué hará " + heroe.getNombre() + "?");
            lblPrompt.setVisible(true);
        }

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

        btnAtacar.setDisable(false);
        btnObjetos.setDisable(false);
        btnHuir.setDisable(false);
        
        iniciarMusica(sesion.getFaseActual() == 4);
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAtacar() {
        ejecutarTurno(AccionHeroe.ATAQUE);
    }

    @FXML
    private void handleHabilidad() {
        if (sesion.getHeroe().isHabilidadUsada()) {
            agregarLog("⚠ Habilidad ya utilizada en este combate.");
            return;
        }
        ejecutarTurno(AccionHeroe.HABILIDAD);
        btnHabilidad.setDisable(true);  // sólo puede usarse una vez
    }

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

    @FXML
    private void handleContinuar() {
        if (sesion.hayMasFases()) {
            sesion.avanzarFase();
            prepararCombate();
        } else {
            // ¡Victoria total!
            navegarAResultado(true);
        }
    }

    // ── Lógica de turno ───────────────────────────────────────────────────────

    /** Tipos de acción que puede ejecutar el héroe en su turno. */
    private enum AccionHeroe { ATAQUE, HABILIDAD, POCION, POCION_MAGICA }

    private void ejecutarTurno(AccionHeroe accion) {
        if (combateTerminado) return;

        switch (accion) {
            case ATAQUE:
            case HABILIDAD: {
                // Si es un personaje mágico usando su habilidad, consume PM
                if (accion == AccionHeroe.HABILIDAD && sesion.getHeroe() instanceof Magico) {
                    Magico m = (Magico) sesion.getHeroe();
                    int coste = Math.max(1, m.getPmMax() / 2);
                    m.gastarPm(coste);
                }
                List<String> mensajes = motor.ejecutarTurnoHeroe(accion == AccionHeroe.HABILIDAD);
                mensajes.forEach(this::agregarLog);

                // El enemigo siempre recibe el primer golpe (a menos que la habilidad
                // del clerigo cure al heroe, en cuyo caso no hay golpe al enemigo)
                if (!(sesion.getHeroe() instanceof Clerigo) || accion != AccionHeroe.HABILIDAD) {
                    animarGolpe(lblIconoEnemigo);
                }
                // Si el enemigo sobrevivio, contraataco -> el heroe recibe el golpe
                if (motor.getEnemigo().estaVivo()) {
                    PauseTransition espera = new PauseTransition(Duration.millis(350));
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
                int curado = h.getPuntosGolpe() - hpAntes;

                agregarLog("🧪 " + h.getNombre() + " usa una poción y recupera " +
                        curado + " HP. (HP: " + h.getPuntosGolpe() + "/" +
                        h.getPuntosGolpeMax() + ")");

                // El enemigo aprovecha el turno y contraataca (si sigue vivo)
                Enemigo enemigo = motor.getEnemigo();
                if (enemigo.estaVivo()) {
                    String ataqueEnemigo = enemigo.realizarAtaque(h);
                    agregarLog(ataqueEnemigo);
                    animarGolpe(imgHeroe);  // el heroe recibe el contraataque
                    if (!h.estaVivo()) {
                        // Forzamos derrota a través del motor para coherencia de estado
                        // (el motor ya se actualizará en la siguiente acción si llegase)
                        agregarLog("💀 " + h.getNombre() + " ha caído en combate...");
                        agregarLog("☠ Derrota. Fin de la aventura.");
                    }
                }

                actualizarTextoBotonObjetos();
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
                    int restaurado = m.getPm() - pmAntes;
                    agregarLog("🔮 " + h.getNombre() + " usa una poción mágica y recupera "
                            + restaurado + " PM. (PM: " + m.getPm() + "/" + m.getPmMax() + ")");
                } else {
                    agregarLog("🔮 " + h.getNombre() + " usa una poción mágica..."
                            + " ¡No tienes PM! La poción no hizo efecto.");
                }

                // El enemigo aprovecha el turno y contraataca (si sigue vivo)
                Enemigo enemigo2 = motor.getEnemigo();
                if (enemigo2.estaVivo()) {
                    String ataque2 = enemigo2.realizarAtaque(h);
                    String defensa2 = h.consumirMensajeDefensa();
                    agregarLog(defensa2 != null ? defensa2 : ataque2);
                    animarGolpe(imgHeroe);
                    if (!h.estaVivo()) {
                        agregarLog("💀 " + h.getNombre() + " ha caído en combate...");
                        agregarLog("☠ Derrota. Fin de la aventura.");
                    }
                }

                actualizarTextoBotonObjetos();
                if (pocionesRestantes <= 0 && pocionesMagicasRestantes <= 0)
                    btnObjetos.setDisable(true);
                break;
            }
        }
        agregarLog("");

        actualizarBarraHeroe();
        actualizarBarraEnemigo();
        if (sesion.getHeroe() instanceof Magico) actualizarBarraPm();

        // Determinar si el combate terminó (motor o muerte por contraataque)
        ResultadoCombate resultado = motor.getResultado();
        if ((accion == AccionHeroe.POCION || accion == AccionHeroe.POCION_MAGICA)
                && !sesion.getHeroe().estaVivo()) {
            // si murió por contraataque tras usar una poción
            resultado = ResultadoCombate.DERROTA;
        }

        if (resultado != ResultadoCombate.EN_CURSO) {
            combateTerminado = true;
            procesarFinCombate(resultado);
        }
    }

    private void procesarFinCombate(ResultadoCombate resultado) {
        btnAtacar.setDisable(true);
        btnHabilidad.setDisable(true);
        btnMagia.setDisable(true);
        btnObjetos.setDisable(true);
        btnHuir.setDisable(true);
        // Si algún submenú estaba abierto, volvemos al menú principal
        if (menuMagia    != null && menuMagia.isVisible())    handleVolverMenu();
        if (menuObjetos  != null && menuObjetos.isVisible())  handleVolverMenuObjetos();

        boolean victoria = resultado == ResultadoCombate.VICTORIA;

        // Registrar combate en BD
        try {
            guardarOActualizarPartida();
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

        if (victoria) {
            // +10 puntos por victoria
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
            // Derrota → actualizar estado en BD
            try {
                Partida p = sesion.getPartida();
                p.setEstado(modelo.Partida.Estado.DERROTA);
                p.setHpActual(0);
                PartidaDAO.actualizar(p);
            } catch (Exception e) { e.printStackTrace(); }

            lblResultado.setText("💀  DERROTA. Tu aventura ha terminado.");
            lblResultado.setStyle("-fx-text-fill: #e05555;");
            btnContinuar.setText("📜  Volver al Menú");
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
     * del personaje (p. ej. habilidad ya usada o no).
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
     * Construye dinámicamente los botones del submenú de magia a partir de las
     * habilidades registradas en el personaje {@link Magico}.
     * <ul>
     *   <li>La habilidad especial (la que coincide con {@code getNombreHabilidad()})
     *       es activable si aún no se ha usado en este combate.</li>
     *   <li>El resto de habilidades mágicas se muestran bloqueadas (son informativas
     *       o pasivas y no tienen acción de combate directa por ahora).</li>
     * </ul>
     */
    private void construirSubmenuMagia(Magico magico) {
        contenedorHabilidades.getChildren().clear();

        String  nombreEspecial    = magico.getNombreHabilidad();
        boolean habilidadYaUsada  = magico.isHabilidadUsada();

        for (String[] h : magico.getHabilidadesMagicas()) {
            String  nombre     = h[0];
            String  descripcion = h[1];
            boolean esEspecial = nombre.equals(nombreEspecial);

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

            if (esEspecial && !habilidadYaUsada) {
                // Habilidad especial disponible → la activa y vuelve al menú
                btn.setText("✨ " + nombre.toUpperCase());
                btn.setOnAction(e -> {
                    handleVolverMenu();
                    ejecutarTurno(AccionHeroe.HABILIDAD);
                });
            } else if (esEspecial) {
                // Ya usada este combate
                btn.setText("✨ " + nombre.toUpperCase() + "  (usada)");
                btn.setDisable(true);
            } else {
                // Habilidad adicional — se activa si tiene PM suficientes y no está ya activa
                int     coste      = magico.getCostePmHabilidad(nombre);
                boolean estaActiva = magico.isHabilidadAdicionalActiva(nombre);
                boolean tienePm    = magico.getPm() >= coste;

                if (estaActiva) {
                    // Ya activa: se muestra como info, no se puede relanzar
                    btn.setText("✅ " + nombre.toUpperCase() + "  (activa)");
                    btn.setDisable(true);
                } else if (coste > 0 && tienePm) {
                    // Implementada y con PM suficientes: activable
                    btn.setText("✨ " + nombre.toUpperCase() + "  (−" + coste + " PM)");
                    final String nombreFinal = nombre;
                    btn.setOnAction(e -> {
                        handleVolverMenu();
                        ejecutarHabilidadMagicaAdicional(nombreFinal);
                    });
                } else if (coste > 0) {
                    // Implementada pero PM insuficientes
                    btn.setText("✨ " + nombre.toUpperCase() + "  (PM insuf.)");
                    btn.setDisable(true);
                } else {
                    // Sin implementación de combate aún
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

    /** Cierra el submenú de objetos y vuelve al menú principal de batalla. */
    @FXML
    private void handleVolverMenuObjetos() {
        menuObjetos.setVisible(false);
        menuObjetos.setManaged(false);
        menuBatalla.setVisible(true);
        menuBatalla.setManaged(true);
    }

    /** Usa una poción de curación desde el submenú de objetos. */
    @FXML
    private void handleUsarPocionCuracion() {
        handleVolverMenuObjetos();
        ejecutarTurno(AccionHeroe.POCION);
    }

    /** Usa una poción mágica desde el submenú de objetos. */
    @FXML
    private void handleUsarPocionMagica() {
        handleVolverMenuObjetos();
        ejecutarTurno(AccionHeroe.POCION_MAGICA);
    }

    /**
     * Actualiza el texto y el estado (habilitado/deshabilitado) de los botones
     * del submenú de objetos según el stock actual.
     */
    private void actualizarSubmenuObjetos() {
        if (pocionesRestantes > 0) {
            btnPocionCuracion.setText("🧪 POCIÓN DE CURACIÓN  ×" + pocionesRestantes
                    + "  (+" + CURACION_POCION + " HP)");
            btnPocionCuracion.setDisable(false);
        } else {
            btnPocionCuracion.setText("🧪 POCIÓN DE CURACIÓN  (agotadas)");
            btnPocionCuracion.setDisable(true);
        }

        if (pocionesMagicasRestantes > 0) {
            btnPocionMagica.setText("🔮 POCIÓN MÁGICA  ×" + pocionesMagicasRestantes
                    + "  (+" + RESTAURACION_PM_POCION + " PM)");
            btnPocionMagica.setDisable(false);
        } else {
            btnPocionMagica.setText("🔮 POCIÓN MÁGICA  (agotadas)");
            btnPocionMagica.setDisable(true);
        }
    }

    /**
     * Ejecuta una habilidad mágica adicional (Escudo Arcano, Bendición Sagrada…).
     * Flujo: gasta PM → aplica efecto → el enemigo contraataca (turno completo).
     *
     * @param nombre nombre de la habilidad registrada en el catálogo de {@link Magico}
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

        // 2. Aplicar el efecto de la habilidad
        Personaje objetivo = motor.getEnemigo(); // algunas habilidades se aplican sobre sí mismo
        String efecto = magico.ejecutarHabilidadAdicional(nombre, objetivo);
        if (efecto == null) {
            // La habilidad no tuvo efecto (ya activa u otro motivo); reembolsar PM
            magico.restaurarPmParcial(coste);
            agregarLog("⚠ " + nombre + " no tuvo efecto.");
            return;
        }
        agregarLog(efecto);

        // 3. Contraataque del enemigo (ocupa el turno del héroe)
        List<String> contraataque = motor.ejecutarContraataqueEnemigo();
        contraataque.forEach(this::agregarLog);

        // Animación: el héroe recibe el golpe (si el enemigo atacó)
        if (!contraataque.isEmpty()) {
            PauseTransition espera = new PauseTransition(Duration.millis(200));
            espera.setOnFinished(ev -> animarGolpe(imgHeroe));
            espera.play();
        }

        agregarLog("");
        actualizarBarraHeroe();
        actualizarBarraEnemigo();
        actualizarBarraPm();

        // 4. Comprobar fin de combate
        ResultadoCombate resultado = motor.getResultado();
        if (resultado != ResultadoCombate.EN_CURSO) {
            combateTerminado = true;
            procesarFinCombate(resultado);
        }
    }

    // ── Guardar partida ───────────────────────────────────────────────────────

    private void guardarOActualizarPartida() throws Exception {
        Heroe heroe = sesion.getHeroe();

        if (sesion.getPartida() == null) {
            // Primera vez que se guarda: insertar
            Partida p = new Partida(
                sesion.getJugador().getId(),
                heroe.getId(),
                sesion.getFaseActual(),
                heroe.getPuntosGolpe()
            );
            PartidaDAO.insertar(p);
            sesion.setPartida(p);
        } else {
            // Actualizar la fila existente
            Partida p = sesion.getPartida();
            p.setFaseActual(sesion.getFaseActual());
            p.setHpActual(heroe.getPuntosGolpe());
            PartidaDAO.actualizar(p);
        }
        // Sincronizar HP del personaje en la tabla personajes
        PersonajeDAO.actualizarHp(heroe.getId(), heroe.getPuntosGolpe());
    }

    private void guardarPartida() {
        try {
            guardarOActualizarPartida();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    private void navegarAResultado(boolean victoria) {
    	detenerMusica();
        try {
            // Marcar partida como completada/derrota
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

    private void actualizarBarraHeroe() {
        Heroe h = sesion.getHeroe();
        double pct = h.getPorcentajeVida();
        barraVidaHeroe.setProgress(pct);
        lblHpHeroe.setText(h.getPuntosGolpe() + " / " + h.getPuntosGolpeMax() + " HP");
        colorearBarra(barraVidaHeroe, pct);
    }

    /**
     * Refresca la barra y el texto de PM del héroe mágico.
     * El color pasa de morado brillante → morado oscuro al bajar del 30 %.
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

    private void actualizarBarraEnemigo() {
        Enemigo e = motor.getEnemigo();
        double pct = e.getPorcentajeVida();
        barraVidaEnemigo.setProgress(pct);
        lblHpEnemigo.setText(e.getPuntosGolpe() + " / " + e.getPuntosGolpeMax() + " HP");
        colorearBarra(barraVidaEnemigo, pct);
    }

    private void colorearBarra(ProgressBar barra, double pct) {
        String color = pct > 0.5 ? "#4caf50" : pct > 0.25 ? "#ff9800" : "#e05555";
        barra.setStyle("-fx-accent: " + color + ";");
    }

    private void actualizarTextoBotonObjetos() {
        if (btnObjetos != null) {
            btnObjetos.setText("🎒  OBJETOS  🧪" + pocionesRestantes
                    + " 🔮" + pocionesMagicasRestantes);
        }
    }

    private void agregarLog(String mensaje) {
        txtLog.appendText(mensaje + "\n");
    }

    /**
     * Aplica un efecto de "temblor" horizontal al nodo, simulando que recibe un ataque.
     * El nodo se desplaza varias veces a izquierda/derecha y vuelve a su posicion original.
     */
    private void animarGolpe(Node objetivo) {
        if (objetivo == null) return;

        int    desplazamiento = 8;     // pixeles a cada lado
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
        centro.setByX(-desplazamiento);

        SequentialTransition secuencia =
                new SequentialTransition(izq1, der1, izq2, der2, centro);
        secuencia.play();
    }
    
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
    
    private void detenerMusica() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
 

