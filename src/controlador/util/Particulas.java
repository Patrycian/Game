package controlador.util;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

public final class Particulas {

	private static final String COLOR = "#c8a84b";
	private static final double ANCHO_AREA = 900;
	private static final double ALTO_AREA = 650;

	private Particulas() {
	}

	/**
	 * Genera partículas doradas con parpadeo continuo dentro del Pane indicado.
	 */
	public static void generar(Pane panel, int cantidad, long semilla, double duracionMinSeg) {
		if (panel == null) {
			return;
		}
		panel.getChildren().clear();
		Random rnd = new Random(semilla);
		for (int i = 0; i < cantidad; i++) {
			double x = rnd.nextDouble() * ANCHO_AREA;
			double y = rnd.nextDouble() * ALTO_AREA;
			double radio = 0.5 + rnd.nextDouble() * 1.2;
			double opacidad = 0.2 + rnd.nextDouble() * 0.5;
			Circle estrella = new Circle(x, y, radio, Color.web(COLOR, opacidad));

			FadeTransition ft = new FadeTransition(Duration.seconds(duracionMinSeg + rnd.nextDouble() * 3), estrella);
			ft.setFromValue(opacidad * 0.3);
			ft.setToValue(opacidad);
			ft.setAutoReverse(true);
			ft.setCycleCount(Animation.INDEFINITE);
			ft.setDelay(Duration.seconds(rnd.nextDouble() * 4));
			ft.play();

			panel.getChildren().add(estrella);
		}
	}
}
