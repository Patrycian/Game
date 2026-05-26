package controlador;

import controlador.util.Particulas;
import dao.JugadorDAO;
import dao.PartidaDAO;
import dao.PersonajeDAO;
import modelo.*;

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
import java.util.ResourceBundle;

public class CargarPartidaController implements Initializable {

	@FXML
	private Pane panelParticulas;

	@FXML
	private VBox contenedorPartidas;

	@FXML
	private Label lblEstado;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		Particulas.generar(panelParticulas, 60, 11, 2);
		cargarPartidas();
	}

	/**
	 * Recupera todas las partidas EN_CURSO de la BD y construye una fila visual por
	 * cada una.
	 */
	private void cargarPartidas() {
		contenedorPartidas.getChildren().clear();
		try {
			List<Partida> partidas = PartidaDAO.listarPartidasActivas();
			if (partidas.isEmpty()) {
				lblEstado.setText("No hay partidas guardadas.");
				lblEstado.setVisible(true);
				return;
			}
			lblEstado.setVisible(false);

			for (Partida p : partidas) {
				Jugador jugador = JugadorDAO.buscarPorId(p.getIdJugador());
				Heroe heroe = PersonajeDAO.buscarPorId(p.getIdPersonaje());
				// Descartar entradas huérfanas (datos inconsistentes en BD)
				if (jugador == null || heroe == null) {
					continue;
				}

				HBox fila = crearFilaPartida(p, jugador, heroe);
				contenedorPartidas.getChildren().add(fila);
			}
		} catch (Exception e) {
			String causa = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			lblEstado.setText("⚠ Error BD: " + causa);
			lblEstado.setVisible(true);
			e.printStackTrace();
		}
	}

	/**
	 * Crea un panel horizontal (HBox) con la información de una partida guardada y
	 * un botón "Reanudar" para cargarla.
	 */
	private HBox crearFilaPartida(Partida p, Jugador jugador, Heroe heroe) {
		HBox fila = new HBox(16);
		fila.setStyle("-fx-background-color: #14141f; -fx-border-color: #2e2840; "
				+ "-fx-border-width: 1; -fx-border-radius: 2; -fx-background-radius: 2; "
				+ "-fx-padding: 12 20; -fx-cursor: hand;");

		Label lblInfo = new Label(String.format("%s  —  %s %s  —  Fase %d  —  HP %d", jugador.getNick(),
				heroe.getIcono(), heroe.getTipo(), p.getFaseActual(), p.getHpActual()));
		lblInfo.setStyle("-fx-font-family: Georgia; -fx-font-size: 14px; -fx-text-fill: #e8e0d0;");

		// Spacer que empuja el botón a la derecha
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button btnReanudar = new Button("▶  Reanudar");
		btnReanudar.setStyle("-fx-background-color: #1a1a26; -fx-border-color: #c8a84b; "
				+ "-fx-border-width: 1; -fx-text-fill: #f0d070; -fx-font-family: Georgia; "
				+ "-fx-cursor: hand; -fx-border-radius: 2; -fx-background-radius: 2;");
		btnReanudar.setOnAction(e -> reanudarPartida(p, jugador, heroe));

		fila.getChildren().addAll(lblInfo, spacer, btnReanudar);
		return fila;
	}

	/**
	 * Restaura la sesión de una partida guardada y navega a la pantalla de
	 * mazmorra.
	 */
	private void reanudarPartida(Partida partida, Jugador jugador, Heroe heroe) {
		// Restaurar HP y PM guardados al héroe
		heroe.setPuntosGolpe(partida.getHpActual());
		if (heroe instanceof Magico) {
			((Magico) heroe).setPm(partida.getPmActual());
		}

		GameSession sesion = new GameSession(jugador, heroe);
		sesion.setFaseActual(partida.getFaseActual());
		sesion.setPartida(partida);

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/Mazmorra.fxml"));
			Parent root = loader.load();
			MazmorraController siguiente = loader.getController();
			siguiente.iniciarSesion(sesion);
			Stage stage = (Stage) contenedorPartidas.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Vuelve al menú principal sin reanudar ninguna partida.
	 */
	@FXML
	private void handleVolver() {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
			Stage stage = (Stage) contenedorPartidas.getScene().getWindow();
			stage.setScene(new Scene(root, 900, 650));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
