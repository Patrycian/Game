package controlador;

import dao.CombateDAO;
import dao.JugadorDAO;
import dao.PartidaDAO;
import dao.PersonajeDAO;
import modelo.*;
import motor.MotorCombate;
import motor.MotorCombate.ResultadoCombate;

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
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de combate en la mazmorra.
 * Gestiona los turnos, actualiza la UI y guarda la partida tras cada fase.
 */
public class MazmorraController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private Label      lblFase;
    @FXML private Label      lblNombreHeroe;
    @FXML private Label      lblHpHeroe;
    @FXML private ProgressBar barraVidaHeroe;
    @FXML private Label      lblNombreEnemigo;
    @FXML private Label      lblHpEnemigo;
    @FXML private ProgressBar barraVidaEnemigo;
    @FXML private Label      lblIconoHeroe;
    @FXML private Label      lblIconoEnemigo;
    @FXML private TextArea   txtLog;
    @FXML private Button     btnAtacar;
    @FXML private Button     btnHabilidad;
    @FXML private Button     btnGuardarSalir;
    @FXML private Label      lblResultado;
    @FXML private Button     btnContinuar;

    // ── Estado ────────────────────────────────────────────────────────────────
    private GameSession  sesion;
    private MotorCombate motor;
    private boolean      combateTerminado = false;

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
        btnHabilidad.setText("✨ " + heroe.getNombreHabilidad());
        btnHabilidad.setDisable(false);

        // ── UI inicial
        txtLog.clear();
        agregarLog("╔══════════════════════════════════╗");
        agregarLog("   FASE " + fase + ": " + heroe.getNombre() + " vs " + enemigo.getNombre());
        agregarLog("╚══════════════════════════════════╝");
        agregarLog(heroe.getDescHabilidad());
        agregarLog("");

        lblResultado.setVisible(false);
        btnContinuar.setVisible(false);
        btnAtacar.setDisable(false);
        btnGuardarSalir.setDisable(false);
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAtacar() {
        ejecutarTurno(false);
    }

    @FXML
    private void handleHabilidad() {
        if (sesion.getHeroe().isHabilidadUsada()) {
            agregarLog("⚠ Habilidad ya utilizada en este combate.");
            return;
        }
        ejecutarTurno(true);
        btnHabilidad.setDisable(true);  // sólo puede usarse una vez
    }

    @FXML
    private void handleGuardarSalir() {
        guardarPartida();
        navegarAMenu();
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

    private void ejecutarTurno(boolean usarHabilidad) {
        if (combateTerminado) return;

        List<String> mensajes = motor.ejecutarTurnoHeroe(usarHabilidad);
        mensajes.forEach(this::agregarLog);
        agregarLog("");

        actualizarBarraHeroe();
        actualizarBarraEnemigo();

        ResultadoCombate resultado = motor.getResultado();
        if (resultado != ResultadoCombate.EN_CURSO) {
            combateTerminado = true;
            procesarFinCombate(resultado);
        }
    }

    private void procesarFinCombate(ResultadoCombate resultado) {
        btnAtacar.setDisable(true);
        btnHabilidad.setDisable(true);
        btnGuardarSalir.setDisable(true);

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

        lblResultado.setVisible(true);
        btnContinuar.setVisible(true);
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
        lblHpEnemigo.setText(e.getPuntosGolpe() + " / " + e.getPuntosGolpeMax() + " HP");
        colorearBarra(barraVidaEnemigo, pct);
    }

    private void colorearBarra(ProgressBar barra, double pct) {
        String color = pct > 0.5 ? "#4caf50" : pct > 0.25 ? "#ff9800" : "#e05555";
        barra.setStyle("-fx-accent: " + color + ";");
    }

    private void agregarLog(String mensaje) {
        txtLog.appendText(mensaje + "\n");
    }
}
