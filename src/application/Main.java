package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage stage) throws Exception { //ventana
		// Cargar la vista del menú principal desde el archivo FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/vistas/MenuPrincipal.fxml"));
		Parent root = loader.load();

		// Creamos escena
		Scene scene = new Scene(root, 900, 650); // resolución fija
		stage.setTitle("Lost Realm");
		stage.setResizable(false);// ventana no redimensionable ¿sí/no?
		stage.setScene(scene); //Asigna la escena creada a la ventana
		stage.show(); //Muestra ventana al usuario
	}

    //lanza el programa
	public static void main(String[] args) {
		launch(args);
	}
}
