package app;

import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.UserStorage;

public class Photos extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        UserStorage.getAllUsers();

        UserStorage.ensureAdminAndStock();

        go("login.fxml");

        stage.setTitle("Photos");
        stage.show();
    }

   
    public static void go(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Photos.class.getResource("/view/" + fxml)
            );
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

            if (loader.getController() instanceof LoginController lc) {
                lc.setStage(stage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void popup(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Photos.class.getResource("/view/" + fxml)
            );
            Stage popup = new Stage();
            popup.setScene(new Scene(loader.load()));
            popup.setTitle("Photo Viewer");
            popup.initOwner(stage);
            popup.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
