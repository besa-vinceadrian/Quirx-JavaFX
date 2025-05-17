package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class Main extends Application {
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
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}