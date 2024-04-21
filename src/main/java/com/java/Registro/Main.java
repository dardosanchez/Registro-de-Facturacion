package com.java.Registro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.Optional;

@SpringBootApplication
public class Main extends Application {

	public static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		launch();
	}

	private String ubicacionDisco="E";

	@Override
	public void start(Stage stage) throws Exception {

		if (ubicacionDisco != null) {
			// Inicializar la aplicación si el disco está conectado
			context = SpringApplication.run(Main.class);

			FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/Registro/Main.fxml"));
			fxml.setControllerFactory(context::getBean);

			Parent root = fxml.load();

			// Crear la escena
			Scene scene = new Scene(root);

			// Agregar el archivo de estilos CSS a la escena
			scene.getStylesheets().add(getClass().getResource("/com/java/Registro/Css/styles.css").toExternalForm());

			// Configurar el escenario principal
			stage.setTitle("Registro de Facturación");
			stage.setScene(scene);
			// Agregar icono a la ventana
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/java/Registro/Images/logo.png")));
			stage.setResizable(false);
			stage.show();
		} else {
			// Mostrar un mensaje indicando que el disco no está conectado
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Advertencia");
			alert.setHeaderText("Disco externo no conectado");
			alert.setContentText("Por favor, conecta el disco externo antes de ejecutar la aplicación.");

			ButtonType retryButton = new ButtonType("Reintentar");
			ButtonType exitButton = new ButtonType("Salir");

			alert.getButtonTypes().setAll(retryButton, exitButton);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == retryButton) {
				// Si el usuario presiona "Reintentar", se vuelve a verificar si el disco está conectado
				start(stage);
			} else {
				// Si el usuario presiona "Salir" o cierra el diálogo, se cierra la aplicación
				stage.close();
			}
		}
	}



	private static boolean discoConectado() {
		// Verificar si la ruta del disco externo está disponible
		File discoExterno = new File("E:\\");
		return discoExterno.exists() && discoExterno.isDirectory();
	}


}
