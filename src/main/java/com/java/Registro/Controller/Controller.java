package com.java.Registro.Controller;

import com.java.Registro.Model.Servicio;
import com.java.Registro.Repository.ServiciosRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javafx.scene.image.ImageView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class Controller implements Initializable {

    @FXML
    private TextField totalTextField;

    @FXML
    private Label tituloSecundario1;


    @Value("${ubicacionDisco}")
    private String ubicacionDisco;

    @FXML
    private RadioButton facturaRadioButton;

    @FXML
    private RadioButton pagoRadioButton;

    private ToggleGroup toggleGroup;

    @FXML
    private Stage stage;

    @FXML
    private ComboBox<Servicio> servicioComboBox;

    private ObservableList<Servicio> serviciosList = FXCollections.observableArrayList();

    @FXML
    private ImageView pdfImageView;

    private File archivoSeleccionado;

    @Autowired
    private ServiciosRepository repoServicios;

    @FXML
    private Button guardarButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toggleGroup = new ToggleGroup();
        facturaRadioButton.setToggleGroup(toggleGroup);
        pagoRadioButton.setToggleGroup(toggleGroup);
        cargarServicios();
        generarCarpetas();
        tituloSecundario1.setVisible(false);
        totalTextField.setVisible(false);
    }



    private void generarCarpetas() {
        try {
            List<Servicio> servicios = repoServicios.findAll();

            for (Servicio servicio : servicios) {
                String carpetaDestino = ubicacionDisco + servicio.getNombre();

                // Crear la carpeta si no existe
                Files.createDirectories(Path.of(carpetaDestino));
                System.out.println("Carpeta generada en: " + carpetaDestino);
            }
        } catch (IOException e) {
            System.out.println("Error al generar las carpetas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarServicios() {
        serviciosList.clear();
        List<Servicio> servicios = repoServicios.findAll();
        serviciosList.addAll(servicios);
        servicioComboBox.setItems(serviciosList);
    }

    @FXML
    private void seleccionarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo PDF");

        // Establecer el filtro para archivos PDF
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Establecer el directorio inicial (opcional)
        File directorioInicial = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(directorioInicial);

        // Mostrar el cuadro de diálogo de selección de archivos
        File archivoPDFSeleccionado = fileChooser.showOpenDialog(stage);

        // Visualizar el PDF seleccionado en el ImageView
        if (archivoPDFSeleccionado != null) {
            try {
                // Guardar una referencia al archivo seleccionado
                archivoSeleccionado = archivoPDFSeleccionado;

                // Cargar el documento PDF
                PDDocument document = PDDocument.load(archivoPDFSeleccionado);
                PDFRenderer renderer = new PDFRenderer(document);

                // Renderizar la primera página del PDF como imagen
                BufferedImage image = renderer.renderImageWithDPI(0, 300);
                Image fxImage = SwingFXUtils.toFXImage(image, null);

                // Mostrar la imagen en el ImageView
                pdfImageView.setImage(fxImage);

                // Cerrar el documento PDF
                document.close();
                tituloSecundario1.setVisible(true);
                totalTextField.setVisible(true);
            } catch (IOException e) {
                System.out.println("Error al cargar el archivo PDF: " + e.getMessage());
            }
        } else {
            System.out.println("No se seleccionó ningún archivo PDF.");
        }
    }

    @FXML
    private void guardarArchivo() {

        if (archivoSeleccionado != null) {

            generarCarpetas();
            guardarArchivoEnDisco(archivoSeleccionado);

            // Aquí estableces el texto del total después de guardar el archivo
            totalTextField.setText("Total: $100.00"); // Reemplaza "$100.00" con el valor real del total
        } else {
            System.out.println("No se ha seleccionado ningún archivo.");
        }
    }

    private void guardarArchivoEnDisco(File file) {
        LocalDate fechaActual = LocalDate.now();
        String mesAnio = DateTimeFormatter.ofPattern("MM-yyyy").format(fechaActual);

        // Obtener el servicio seleccionado en el ComboBox
        Servicio servicioSeleccionado = servicioComboBox.getValue();

        if (servicioSeleccionado != null) {
            RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
            String tipoArchivo = selectedRadioButton.getText();

            try {
                String nombreArchivo = tipoArchivo + " " + servicioSeleccionado.getNombre() + "-" + mesAnio + ".pdf";

                String carpetaDestino = ubicacionDisco + servicioSeleccionado.getNombre();

                // Crear la carpeta si no existe
                Files.createDirectories(Path.of(carpetaDestino));

                // Agregar el mes y año a la carpeta si es necesario
                carpetaDestino = Paths.get(carpetaDestino, mesAnio).toString();
                Files.createDirectories(Path.of(carpetaDestino));

                // Copiar el archivo en la carpeta destino con el nuevo nombre
                Path origen = file.toPath();
                Path destino = Path.of(carpetaDestino, nombreArchivo);
                Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Archivo guardado en: " + destino);
                limpiarElementos();
                mostrarMensajeExito();
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo en disco para el servicio " + servicioSeleccionado.getNombre() + ": " + e.getMessage());
                mostrarMensajeError("Error al guardar el archivo en disco para el servicio " + servicioSeleccionado.getNombre());
                e.printStackTrace();
            }
        } else {
            System.out.println("No se ha seleccionado ningún servicio.");
        }
    }


    private void limpiarElementos() {

        // Limpiar los RadioButtons
        toggleGroup.selectToggle(null);

        // Limpiar la imagen del ImageView
        pdfImageView.setImage(null);

        // Limpiar el texto del TextField
        totalTextField.clear();

        tituloSecundario1.setVisible(false);
        totalTextField.setVisible(false);
    }


    private void mostrarMensajeExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("El archivo se guardó correctamente.");
        alert.showAndWait();

        // Obtener la ventana padre
        Stage stage = (Stage) guardarButton.getScene().getWindow();

        // Cerrar la ventana padre
        stage.close();
    }

    private void mostrarMensajeError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Se produjo un error al guardar el archivo: " + mensaje);
        alert.showAndWait();

        // Obtener la ventana padre
        Stage stage = (Stage) guardarButton.getScene().getWindow();

        // Cerrar la ventana padre
        stage.close();
    }
}