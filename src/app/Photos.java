package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class of the Photos app.
 * 
 * <p>
 * This class initializes the primary stage, loads the login screen, and
 * provides static methods for switching scenes and opening popup windows. It
 * also makes sure that all user data is saved when the app is closed.
 * </p>
 *
 * <p>
 * The {@link #main(String[])} method runs JavaFX, and {@link #start(Stage)}
 * sets the initial window.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class Photos extends Application {

	/** The main stage for the app. */
	private static Stage stage;

	/**
	 * Initializes the primary app stage, loads the login screen, sets the window
	 * title, and handles save data on close.
	 *
	 * @param primaryStage the main stage from JavaFX
	 * @throws Exception if the initial FXML cannot be loaded
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		go("Login.fxml");
		stage.setTitle("Photos");

		// save all users when the app is closed
		stage.setOnCloseRequest(e -> {
			try {
				model.UserStorage.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		});
		stage.show();

	}

	/**
	 * Switches stages to display the specified FXML file.
	 * 
	 * @param fxml the FXML file name located under <code>/view/</code>
	 */
	public static void go(String fxml) {
		try {
			FXMLLoader loader = new FXMLLoader(Photos.class.getResource("/view/" + fxml));
			Scene scene = new Scene(loader.load());
			stage.setScene(scene);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens a popup window for the given FXML file. Used for photo viewer windows
	 * and other popup dialogs.
	 *
	 * @param fxml the FXML file name located under <code>/view/</code>
	 */
	public static void popup(String fxml) {
		try {
			FXMLLoader loader = new FXMLLoader(Photos.class.getResource("/view/" + fxml));
			Stage popup = new Stage();
			popup.setScene(new Scene(loader.load()));
			popup.setTitle("Photo Viewer");
			popup.initOwner(stage);
			popup.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the main stage for the app.
	 *
	 * @return the main stage
	 */
	public static Stage getStage() {
		return stage;
	}

	/**
	 * Launches the photo app.
	 *
	 * @param args not used just launches app.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}