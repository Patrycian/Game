package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX "Lost Realm".
 *
 * <p>Extiende {@link Application} y sobreescribe {@link #start(Stage)} para
 * cargar la primera pantalla (menú principal) e inicializar la ventana principal.
 * La resolución está fijada a 900 × 650 píxeles y la ventana no es redimensionable
 * para mantener la coherencia visual del diseño.</p>
 *
 * <p><b>Flujo general de pantallas:</b></p>
 * <pre>
 *   MenuPrincipal
 *     ├─► Nombre          (introduce el nick)
 *     │     └─► SeleccionHeroe  (elige clase: Mago / Guerrero / Clérigo)
 *     │               └─► Mazmorra  (combate por fases 1-4)
 *     │                         └─► Resultado  (victoria o derrota)
 *     ├─► CargarPartida   (retoma una partida EN_CURSO)
 *     │         └─► Mazmorra
 *     └─► Ranking         (Top 10 jugadores)
 * </pre>
 */
public class Main extends Application {

    /**
     * Método de inicio de JavaFX. Se llama automáticamente al lanzar la aplicación.
     *
     * <p>Carga el FXML del menú principal, crea la escena con tamaño fijo y
     * muestra la ventana.</p>
     *
     * @param stage ventana primaria proporcionada por el runtime de JavaFX
     * @throws Exception si el archivo FXML no se puede localizar o parsear
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Cargar la vista del menú principal desde el archivo FXML
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/application/vistas/MenuPrincipal.fxml")
        );
        Parent root = loader.load();

        // Crear la escena con dimensiones fijas (sin barra de redimensionado)
        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Lost Realm");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método principal que lanza el ciclo de vida de JavaFX a través de
     * {@link Application#launch(String...)}.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
