package controlador;

import dao.CombateDAO;
import dao.JugadorDAO;
import dao.PartidaDAO;
import dao.PersonajeDAO;
import modelo.*;
import motor.MotorCombate;
import motor.MotorCombate.ResultadoCombate;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

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
    @FXML private Label       lblIconoHeroe;
    @FXML private Label       lblIconoEnemigo;

    // ── FXML: caja de diálogo y menú ─────────────────────────────────────────
    @FXML private Label       lblPrompt;
    @FXML private TextArea    txtLog;
    @FXML private Label       lblResultado;
    @FXML private VBox        menuBatalla;
    @FXML private Button      btnAtacar;
    @FXML private Button      btnObjetos;
    @FXML private Button      btnHabilidad;
    @FXML private Button      btnHuir;
    @FXML private Button      btnContinuar;

    // ── Estado ────────────────────────────────────────────────────────────────
    private GameSession  sesion;
    private MotorCombate motor;
    private boolean      combateTerminado = false;

    // ── Inventario simple del combate ────────────────────────────────────────
    /** Pociones disponibles en el combate actual (se reinicia cada fase). */
    private int pocionesRestantes = 3;
    private static final int CURACION_POCION = 30;

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
        pocionesRestantes = 3;        // 3 pociones por combate

        // ── Labels de fase
        lblFase.setText("⚔  FASE " + fase + (fase == 4 ? "  —  JEFE FINAL" : "  —  MAZMORRA"));

        // ── Héroe
        lblNombreHeroe.setText(heroe.getNombre() + " (" + heroe.getTipo() + ")");
        lblIconoHeroe.setText(heroe.getIcono());
        actualizarBarraHeroe();

        // ── Enemigo
        lblNombreEnemigo.setText(enemigo.getNombre() + " (" + enemigo.getTipo() + ")");
        lblIconoEnemigo.setText(enemigo.getIcono());
        actualizarBarraEnemigo();

        // ── Habilidad especial
        btnHabilidad.setText("✨ " + heroe.getNombreHabilidad().toUpperCase());
        btnHabilidad.setDisable(false);

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

        if (pocionesRestantes <= 0) {
            agregarLog("🎒 No te quedan objetos.");
            return;
        }

        // Confirmación sencilla del uso de la poción
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Objetos");
        alert.setHeaderText("🧪  Poción de curación  ×" + pocionesRestantes);
        alert.setContentText("Recupera " + CURACION_POCION + " HP. ¿Usar una poción?");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/application/vistas/estilos.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-oscuro");

        Optional<ButtonType> resp = alert.showAndWait();
        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            ejecutarTurno(AccionHeroe.POCION);
        }
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
    private enum AccionHeroe { ATAQUE, HABILIDAD, POCION }

    private void ejecutarTurno(AccionHeroe accion) {
        if (combateTerminado) return;

        switch (accion) {
            case ATAQUE:
            case HABILIDAD: {
                List<String> mensajes = motor.ejecutarTurnoHeroe(accion == AccionHeroe.HABILIDAD);
                mensajes.forEach(this::agregarLog);
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
                    if (!h.estaVivo()) {
                        // Forzamos derrota a través del motor para coherencia de estado
                        // (el motor ya se actualizará en la siguiente acción si llegase)
                        agregarLog("💀 " + h.getNombre() + " ha caído en combate...");
                        agregarLog("☠ Derrota. Fin de la aventura.");
                    }
                }

                actualizarTextoBotonObjetos();
                if (pocionesRestantes <= 0) btnObjetos.setDisable(true);
                break;
            }
        }
        agregarLog("");

        actualizarBarraHeroe();
        actualizarBarraEnemigo();

        // Determinar si el combate terminó (motor o muerte por contraataque)
        ResultadoCombate resultado = motor.getResultado();
        if (accion == AccionHeroe.POCION && !sesion.getHeroe().estaVivo()) {
            // si murió por contraataque tras la poción
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
        btnObjetos.setDisable(true);
        btnHuir.setDisable(true);

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

    private void actualizarBarraEnemigo() {
        Enemigo e = motor.getEnemigo();
        double pct = e.getPorcentajeVida();
        barraVidaEnemigo.setProgress(pct);
        colorearBarra(barraVidaEnemigo, pct);
    }

    private void colorearBarra(ProgressBar barra, double pct) {
        String color = pct > 0.5 ? "#4caf50" : pct > 0.25 ? "#ff9800" : "#e05555";
        barra.setStyle("-fx-accent: " + color + ";");
    }

    private void actualizarTextoBotonObjetos() {
        if (btnObjetos != null) {
            btnObjetos.setText("🎒  OBJETOS (" + pocionesRestantes + ")");
        }
    }

    private void agregarLog(String mensaje) {
        txtLog.appendText(mensaje + "\n");
    }
}
