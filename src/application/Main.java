package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/application/vistas/MenuPrincipal.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Realm of Shadows – Juego de Mazmorras por Turnos");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
