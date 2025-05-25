package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * The entry point of the Quirx JavaFX application.
 * This class sets up and displays the initial sign-up screen.
 */
public class Main extends Application {
	
	/**
     * Default constructor for the Main class.
     * Initializes the JavaFX application.
     */
    public Main() {
        super();
    }

	
	/**
     * Starts the primary stage of the JavaFX application.
     *
     * @param primaryStage the primary stage for this application
     */
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("SignUp.fxml"));
			Scene scene = new Scene(root,1024,700);
			scene.getStylesheets().add(getClass().getResource("SignUp.css").toExternalForm());
			primaryStage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
			primaryStage.setTitle("Quirx");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			
			primaryStage.setOnCloseRequest(event -> {
				event.consume();
				
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Close Quirx");
				alert.setHeaderText(null);
				alert.setContentText("Are you sure you want to exit?");
				Optional<ButtonType> result = alert.showAndWait();
				
				if (result.isPresent() && result.get() == ButtonType.OK) {
					primaryStage.close();
				}
			});
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * The main method that launches the JavaFX application.
     *
     * @param args the command-line arguments
     */
	public static void main(String[] args) {
		launch(args);
	}
}