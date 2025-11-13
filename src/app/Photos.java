package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Photos extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        go("Login.fxml");  
        stage.setTitle("Photos");
        stage.show();
    }

    public static void go(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Photos.class.getResource("/view/" + fxml));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getStage() { return stage; }

    public static void main(String[] args) { 
    	launch(args);
    }
}